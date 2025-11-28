package com.reconciliation.enums;

/**
 * Defines the types of source/target systems that can be connected.
 */
public enum SystemType {
    DATABASE,
    FILE_SYSTEM,
    API_ENDPOINT,
    SFTP,
    S3_BUCKET,
    AZURE_BLOB,
    KAFKA,
    CUSTOM
}

