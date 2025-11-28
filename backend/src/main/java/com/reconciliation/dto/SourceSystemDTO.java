package com.reconciliation.dto;

import com.reconciliation.enums.SystemType;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceSystemDTO {
    
    private Long id;
    
    @NotBlank(message = "System code is required")
    @Size(max = 50, message = "System code must be less than 50 characters")
    private String systemCode;
    
    @NotBlank(message = "System name is required")
    @Size(max = 200, message = "System name must be less than 200 characters")
    private String systemName;
    
    private String description;
    
    @NotNull(message = "System type is required")
    private SystemType systemType;
    
    private String connectionString;
    private String host;
    private Integer port;
    private String databaseName;
    private String schemaName;
    private String username;
    private String password;
    private String filePath;
    private String apiUrl;
    private String apiKey;
    private String additionalConfig;
    private String dataOwner;
    private String contactEmail;
    private Boolean isSource;
    private Boolean isTarget;
    private String testConnectionQuery;
    private Boolean isActive;
}

