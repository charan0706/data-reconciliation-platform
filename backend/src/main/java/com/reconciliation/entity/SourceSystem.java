package com.reconciliation.entity;

import com.reconciliation.enums.SystemType;
import lombok.*;

import javax.persistence.*;

/**
 * Represents a source or target system for data reconciliation.
 */
@Entity
@Table(name = "source_systems")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceSystem extends BaseEntity {

    @Column(name = "system_code", unique = true, nullable = false, length = 50)
    private String systemCode;

    @Column(name = "system_name", nullable = false, length = 200)
    private String systemName;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "system_type", nullable = false, length = 50)
    private SystemType systemType;

    @Column(name = "connection_string", length = 1000)
    private String connectionString;

    @Column(name = "host", length = 255)
    private String host;

    @Column(name = "port")
    private Integer port;

    @Column(name = "database_name", length = 255)
    private String databaseName;

    @Column(name = "schema_name", length = 255)
    private String schemaName;

    @Column(name = "username", length = 255)
    private String username;

    @Column(name = "encrypted_password", length = 500)
    private String encryptedPassword;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(name = "api_url", length = 1000)
    private String apiUrl;

    @Column(name = "api_key", length = 500)
    private String apiKey;

    @Column(name = "additional_config", columnDefinition = "CLOB")
    private String additionalConfig;

    @Column(name = "data_owner", length = 200)
    private String dataOwner;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "is_source")
    private Boolean isSource = true;

    @Column(name = "is_target")
    private Boolean isTarget = true;

    @Column(name = "test_connection_query", length = 500)
    private String testConnectionQuery;
}

