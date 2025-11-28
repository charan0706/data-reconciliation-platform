package com.reconciliation.service;

import com.reconciliation.entity.SourceSystem;
import com.reconciliation.enums.SystemType;
import com.reconciliation.exception.ReconciliationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for extracting data from various source systems.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataExtractionService {
    
    /**
     * Extract data from a source system.
     */
    public List<Map<String, Object>> extractData(SourceSystem system, String query, String filePattern) {
        log.info("Extracting data from system: {} ({})", system.getSystemCode(), system.getSystemType());
        
        switch (system.getSystemType()) {
            case DATABASE:
                return extractFromDatabase(system, query);
            case FILE_SYSTEM:
                return extractFromFileSystem(system, filePattern);
            case API_ENDPOINT:
                return extractFromApi(system);
            case SFTP:
                return extractFromSftp(system, filePattern);
            default:
                throw new ReconciliationException("Unsupported system type: " + system.getSystemType());
        }
    }
    
    /**
     * Extract data from a database using JDBC.
     */
    private List<Map<String, Object>> extractFromDatabase(SourceSystem system, String query) {
        if (query == null || query.isEmpty()) {
            throw new ReconciliationException("Query is required for database extraction");
        }
        
        try {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl(buildJdbcUrl(system));
            dataSource.setUsername(system.getUsername());
            dataSource.setPassword(system.getEncryptedPassword()); // Should decrypt in production
            
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
            log.info("Extracted {} records from database {}", results.size(), system.getSystemCode());
            
            return results;
        } catch (Exception e) {
            throw new ReconciliationException("Failed to extract data from database: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build JDBC URL from system configuration.
     */
    private String buildJdbcUrl(SourceSystem system) {
        if (system.getConnectionString() != null && !system.getConnectionString().isEmpty()) {
            return system.getConnectionString();
        }
        
        // Build Oracle JDBC URL
        return String.format("jdbc:oracle:thin:@//%s:%d/%s",
                system.getHost(),
                system.getPort() != null ? system.getPort() : 1521,
                system.getDatabaseName());
    }
    
    /**
     * Extract data from file system (CSV, Excel, JSON).
     */
    private List<Map<String, Object>> extractFromFileSystem(SourceSystem system, String filePattern) {
        String basePath = system.getFilePath();
        if (basePath == null || basePath.isEmpty()) {
            throw new ReconciliationException("File path is required for file system extraction");
        }
        
        try {
            Path directory = Paths.get(basePath);
            List<Map<String, Object>> allRecords = new ArrayList<>();
            
            // Find matching files
            String pattern = filePattern != null ? filePattern : "*.*";
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
            
            try (Stream<Path> paths = Files.walk(directory, 1)) {
                List<Path> files = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> matcher.matches(p.getFileName()))
                        .collect(Collectors.toList());
                
                for (Path file : files) {
                    String fileName = file.getFileName().toString().toLowerCase();
                    if (fileName.endsWith(".csv")) {
                        allRecords.addAll(readCsvFile(file));
                    } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                        allRecords.addAll(readExcelFile(file));
                    } else if (fileName.endsWith(".json")) {
                        allRecords.addAll(readJsonFile(file));
                    }
                }
            }
            
            log.info("Extracted {} records from file system {}", allRecords.size(), system.getSystemCode());
            return allRecords;
            
        } catch (IOException e) {
            throw new ReconciliationException("Failed to extract data from file system: " + e.getMessage(), e);
        }
    }
    
    /**
     * Read data from CSV file.
     */
    private List<Map<String, Object>> readCsvFile(Path file) throws IOException {
        List<Map<String, Object>> records = new ArrayList<>();
        
        try (Reader reader = Files.newBufferedReader(file);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withTrim())) {
            
            for (CSVRecord csvRecord : csvParser) {
                Map<String, Object> record = new LinkedHashMap<>();
                for (String header : csvParser.getHeaderNames()) {
                    record.put(header, csvRecord.get(header));
                }
                records.add(record);
            }
        }
        
        log.debug("Read {} records from CSV file: {}", records.size(), file.getFileName());
        return records;
    }
    
    /**
     * Read data from Excel file.
     */
    private List<Map<String, Object>> readExcelFile(Path file) throws IOException {
        List<Map<String, Object>> records = new ArrayList<>();
        
        try (InputStream is = Files.newInputStream(file);
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            
            if (headerRow == null) {
                return records;
            }
            
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell));
            }
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Map<String, Object> record = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    record.put(headers.get(j), cell != null ? getCellValue(cell) : null);
                }
                records.add(record);
            }
        }
        
        log.debug("Read {} records from Excel file: {}", records.size(), file.getFileName());
        return records;
    }
    
    /**
     * Read data from JSON file.
     */
    private List<Map<String, Object>> readJsonFile(Path file) throws IOException {
        // Simplified JSON reading - in production, use Jackson
        String content = Files.readString(file);
        // For now, return empty list - implement proper JSON parsing as needed
        log.warn("JSON file reading not fully implemented: {}", file.getFileName());
        return new ArrayList<>();
    }
    
    /**
     * Extract data from REST API.
     */
    private List<Map<String, Object>> extractFromApi(SourceSystem system) {
        // Simplified API extraction - implement proper REST client in production
        log.info("API extraction from: {}", system.getApiUrl());
        
        // In production, use RestTemplate or WebClient
        // For now, return empty list
        return new ArrayList<>();
    }
    
    /**
     * Extract data from SFTP.
     */
    private List<Map<String, Object>> extractFromSftp(SourceSystem system, String filePattern) {
        // Simplified SFTP extraction - implement proper SFTP client in production
        log.info("SFTP extraction from: {}:{}", system.getHost(), system.getPort());
        
        // In production, use JSch or Apache Commons VFS
        // For now, return empty list
        return new ArrayList<>();
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
    
    private Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue();
                }
                double value = cell.getNumericCellValue();
                if (value == Math.floor(value)) {
                    return (long) value;
                }
                return value;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return null;
            default:
                return null;
        }
    }
    
    /**
     * Generate sample data for testing purposes.
     */
    public List<Map<String, Object>> generateSampleData(int count, boolean includeErrors) {
        List<Map<String, Object>> data = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 1; i <= count; i++) {
            Map<String, Object> record = new LinkedHashMap<>();
            record.put("id", i);
            record.put("account_number", "ACC" + String.format("%06d", i));
            record.put("customer_name", "Customer " + i);
            record.put("balance", 1000.0 + random.nextDouble() * 9000);
            record.put("currency", "USD");
            record.put("status", random.nextBoolean() ? "ACTIVE" : "INACTIVE");
            record.put("created_date", "2024-01-" + String.format("%02d", (i % 28) + 1));
            
            // Introduce some variations for testing
            if (includeErrors && i % 10 == 0) {
                record.put("balance", ((Double) record.get("balance")) + random.nextDouble() * 100);
            }
            
            data.add(record);
        }
        
        return data;
    }
}

