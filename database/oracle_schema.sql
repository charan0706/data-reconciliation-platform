-- =====================================================
-- Data Reconciliation Platform - Oracle Database Schema
-- Version: 1.0.0
-- =====================================================

-- Create sequences
CREATE SEQUENCE seq_users START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_source_systems START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_reconciliation_configs START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_attribute_mappings START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_reconciliation_runs START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_discrepancies START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_incidents START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_incident_comments START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_incident_history START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_run_logs START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_audit_logs START WITH 1 INCREMENT BY 1;

-- =====================================================
-- Users Table
-- =====================================================
CREATE TABLE users (
    id NUMBER PRIMARY KEY,
    username VARCHAR2(100) NOT NULL UNIQUE,
    email VARCHAR2(255) NOT NULL UNIQUE,
    password_hash VARCHAR2(255) NOT NULL,
    first_name VARCHAR2(100),
    last_name VARCHAR2(100),
    department VARCHAR2(100),
    is_locked NUMBER(1) DEFAULT 0,
    failed_login_attempts NUMBER DEFAULT 0,
    is_active NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR2(100),
    updated_by VARCHAR2(100),
    version NUMBER DEFAULT 0
);

CREATE TABLE user_roles (
    user_id NUMBER NOT NULL,
    role VARCHAR2(50) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role)
);

-- =====================================================
-- Source Systems Table
-- =====================================================
CREATE TABLE source_systems (
    id NUMBER PRIMARY KEY,
    system_code VARCHAR2(50) NOT NULL UNIQUE,
    system_name VARCHAR2(200) NOT NULL,
    description VARCHAR2(1000),
    system_type VARCHAR2(50) NOT NULL,
    connection_string VARCHAR2(1000),
    host VARCHAR2(255),
    port NUMBER,
    database_name VARCHAR2(255),
    schema_name VARCHAR2(255),
    username VARCHAR2(255),
    encrypted_password VARCHAR2(500),
    file_path VARCHAR2(1000),
    api_url VARCHAR2(1000),
    api_key VARCHAR2(500),
    additional_config CLOB,
    data_owner VARCHAR2(200),
    contact_email VARCHAR2(255),
    is_source NUMBER(1) DEFAULT 1,
    is_target NUMBER(1) DEFAULT 1,
    test_connection_query VARCHAR2(500),
    is_active NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR2(100),
    updated_by VARCHAR2(100),
    version NUMBER DEFAULT 0
);

-- =====================================================
-- Reconciliation Configurations Table
-- =====================================================
CREATE TABLE reconciliation_configs (
    id NUMBER PRIMARY KEY,
    config_code VARCHAR2(50) NOT NULL UNIQUE,
    config_name VARCHAR2(200) NOT NULL,
    description VARCHAR2(1000),
    source_system_id NUMBER NOT NULL,
    target_system_id NUMBER NOT NULL,
    source_query CLOB,
    target_query CLOB,
    source_file_pattern VARCHAR2(500),
    target_file_pattern VARCHAR2(500),
    primary_key_attributes VARCHAR2(500),
    schedule_frequency VARCHAR2(50),
    cron_expression VARCHAR2(100),
    is_scheduled NUMBER(1) DEFAULT 0,
    schedule_enabled NUMBER(1) DEFAULT 1,
    tolerance_percentage NUMBER(10,4),
    date_tolerance_minutes NUMBER,
    ignore_case NUMBER(1) DEFAULT 0,
    trim_whitespace NUMBER(1) DEFAULT 1,
    null_equals_empty NUMBER(1) DEFAULT 0,
    max_discrepancies NUMBER DEFAULT 10000,
    batch_size NUMBER DEFAULT 1000,
    notification_emails VARCHAR2(1000),
    auto_create_incidents NUMBER(1) DEFAULT 1,
    owner_id NUMBER,
    is_active NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR2(100),
    updated_by VARCHAR2(100),
    version NUMBER DEFAULT 0,
    CONSTRAINT fk_config_source_system FOREIGN KEY (source_system_id) REFERENCES source_systems(id),
    CONSTRAINT fk_config_target_system FOREIGN KEY (target_system_id) REFERENCES source_systems(id),
    CONSTRAINT fk_config_owner FOREIGN KEY (owner_id) REFERENCES users(id)
);

-- =====================================================
-- Attribute Mappings Table
-- =====================================================
CREATE TABLE attribute_mappings (
    id NUMBER PRIMARY KEY,
    reconciliation_config_id NUMBER NOT NULL,
    source_attribute VARCHAR2(200) NOT NULL,
    target_attribute VARCHAR2(200) NOT NULL,
    display_name VARCHAR2(200),
    data_type VARCHAR2(50),
    comparison_type VARCHAR2(50) NOT NULL,
    tolerance_value NUMBER(15,6),
    tolerance_type VARCHAR2(50),
    transformation_expression VARCHAR2(1000),
    source_transformation VARCHAR2(1000),
    target_transformation VARCHAR2(1000),
    is_key_attribute NUMBER(1) DEFAULT 0,
    is_required NUMBER(1) DEFAULT 1,
    is_enabled NUMBER(1) DEFAULT 1,
    sort_order NUMBER DEFAULT 0,
    mismatch_severity VARCHAR2(20) DEFAULT 'MEDIUM',
    custom_validation_rule VARCHAR2(1000),
    null_handling VARCHAR2(50),
    format_pattern VARCHAR2(200),
    is_active NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR2(100),
    updated_by VARCHAR2(100),
    version NUMBER DEFAULT 0,
    CONSTRAINT fk_mapping_config FOREIGN KEY (reconciliation_config_id) REFERENCES reconciliation_configs(id)
);

-- =====================================================
-- Reconciliation Runs Table
-- =====================================================
CREATE TABLE reconciliation_runs (
    id NUMBER PRIMARY KEY,
    run_id VARCHAR2(50) NOT NULL UNIQUE,
    reconciliation_config_id NUMBER NOT NULL,
    status VARCHAR2(50) NOT NULL,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    triggered_by VARCHAR2(100),
    is_scheduled_run NUMBER(1) DEFAULT 0,
    source_record_count NUMBER DEFAULT 0,
    target_record_count NUMBER DEFAULT 0,
    matched_record_count NUMBER DEFAULT 0,
    discrepancy_count NUMBER DEFAULT 0,
    missing_in_source_count NUMBER DEFAULT 0,
    missing_in_target_count NUMBER DEFAULT 0,
    attribute_mismatch_count NUMBER DEFAULT 0,
    error_message VARCHAR2(4000),
    error_stack_trace CLOB,
    execution_time_ms NUMBER,
    source_extraction_time_ms NUMBER,
    target_extraction_time_ms NUMBER,
    comparison_time_ms NUMBER,
    report_path VARCHAR2(1000),
    source_file_path VARCHAR2(1000),
    target_file_path VARCHAR2(1000),
    is_active NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR2(100),
    updated_by VARCHAR2(100),
    version NUMBER DEFAULT 0,
    CONSTRAINT fk_run_config FOREIGN KEY (reconciliation_config_id) REFERENCES reconciliation_configs(id)
);

-- =====================================================
-- Incidents Table
-- =====================================================
CREATE TABLE incidents (
    id NUMBER PRIMARY KEY,
    incident_number VARCHAR2(50) NOT NULL UNIQUE,
    title VARCHAR2(500) NOT NULL,
    description CLOB,
    status VARCHAR2(50) NOT NULL,
    severity VARCHAR2(20) NOT NULL,
    reconciliation_run_id NUMBER,
    reconciliation_config_id NUMBER,
    discrepancy_count NUMBER DEFAULT 0,
    affected_records NUMBER DEFAULT 0,
    assigned_to_id NUMBER,
    maker_id NUMBER,
    checker_id NUMBER,
    assigned_at TIMESTAMP,
    investigation_started_at TIMESTAMP,
    resolution_proposed_at TIMESTAMP,
    resolution_approved_at TIMESTAMP,
    closed_at TIMESTAMP,
    due_date TIMESTAMP,
    sla_breach NUMBER(1) DEFAULT 0,
    root_cause CLOB,
    proposed_resolution CLOB,
    resolution_notes CLOB,
    checker_comments CLOB,
    rejection_reason VARCHAR2(2000),
    rejection_count NUMBER DEFAULT 0,
    escalation_level NUMBER DEFAULT 0,
    escalated_at TIMESTAMP,
    escalated_to VARCHAR2(200),
    is_active NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR2(100),
    updated_by VARCHAR2(100),
    version NUMBER DEFAULT 0,
    CONSTRAINT fk_incident_run FOREIGN KEY (reconciliation_run_id) REFERENCES reconciliation_runs(id),
    CONSTRAINT fk_incident_config FOREIGN KEY (reconciliation_config_id) REFERENCES reconciliation_configs(id),
    CONSTRAINT fk_incident_assigned FOREIGN KEY (assigned_to_id) REFERENCES users(id),
    CONSTRAINT fk_incident_maker FOREIGN KEY (maker_id) REFERENCES users(id),
    CONSTRAINT fk_incident_checker FOREIGN KEY (checker_id) REFERENCES users(id)
);

-- =====================================================
-- Discrepancies Table
-- =====================================================
CREATE TABLE discrepancies (
    id NUMBER PRIMARY KEY,
    reconciliation_run_id NUMBER NOT NULL,
    discrepancy_code VARCHAR2(50) NOT NULL,
    discrepancy_type VARCHAR2(50) NOT NULL,
    severity VARCHAR2(20) NOT NULL,
    record_key VARCHAR2(500) NOT NULL,
    attribute_name VARCHAR2(200),
    source_value VARCHAR2(4000),
    target_value VARCHAR2(4000),
    expected_value VARCHAR2(4000),
    actual_value VARCHAR2(4000),
    difference_amount NUMBER(20,6),
    difference_percentage NUMBER(10,4),
    source_record_json CLOB,
    target_record_json CLOB,
    description VARCHAR2(2000),
    business_impact VARCHAR2(1000),
    is_acknowledged NUMBER(1) DEFAULT 0,
    acknowledged_by VARCHAR2(100),
    is_false_positive NUMBER(1) DEFAULT 0,
    false_positive_reason VARCHAR2(1000),
    incident_id NUMBER,
    row_number NUMBER,
    batch_number NUMBER,
    is_active NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR2(100),
    updated_by VARCHAR2(100),
    version NUMBER DEFAULT 0,
    CONSTRAINT fk_discrepancy_run FOREIGN KEY (reconciliation_run_id) REFERENCES reconciliation_runs(id),
    CONSTRAINT fk_discrepancy_incident FOREIGN KEY (incident_id) REFERENCES incidents(id)
);

-- =====================================================
-- Incident Comments Table
-- =====================================================
CREATE TABLE incident_comments (
    id NUMBER PRIMARY KEY,
    incident_id NUMBER NOT NULL,
    comment_text CLOB NOT NULL,
    user_id NUMBER,
    is_internal NUMBER(1) DEFAULT 0,
    attachment_path VARCHAR2(1000),
    is_active NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR2(100),
    updated_by VARCHAR2(100),
    version NUMBER DEFAULT 0,
    CONSTRAINT fk_comment_incident FOREIGN KEY (incident_id) REFERENCES incidents(id),
    CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =====================================================
-- Incident History Table
-- =====================================================
CREATE TABLE incident_history (
    id NUMBER PRIMARY KEY,
    incident_id NUMBER NOT NULL,
    from_status VARCHAR2(50),
    to_status VARCHAR2(50) NOT NULL,
    action VARCHAR2(100) NOT NULL,
    action_by VARCHAR2(100),
    comments VARCHAR2(2000),
    ip_address VARCHAR2(50),
    is_active NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR2(100),
    updated_by VARCHAR2(100),
    version NUMBER DEFAULT 0,
    CONSTRAINT fk_history_incident FOREIGN KEY (incident_id) REFERENCES incidents(id)
);

-- =====================================================
-- Run Logs Table
-- =====================================================
CREATE TABLE run_logs (
    id NUMBER PRIMARY KEY,
    reconciliation_run_id NUMBER NOT NULL,
    log_level VARCHAR2(20) NOT NULL,
    step_name VARCHAR2(100),
    message CLOB NOT NULL,
    details CLOB,
    timestamp TIMESTAMP NOT NULL,
    duration_ms NUMBER,
    records_processed NUMBER,
    error_code VARCHAR2(50),
    stack_trace CLOB,
    is_active NUMBER(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR2(100),
    updated_by VARCHAR2(100),
    version NUMBER DEFAULT 0,
    CONSTRAINT fk_log_run FOREIGN KEY (reconciliation_run_id) REFERENCES reconciliation_runs(id)
);

-- =====================================================
-- Audit Logs Table
-- =====================================================
CREATE TABLE audit_logs (
    id NUMBER PRIMARY KEY,
    event_type VARCHAR2(100) NOT NULL,
    entity_type VARCHAR2(100),
    entity_id NUMBER,
    action VARCHAR2(50) NOT NULL,
    username VARCHAR2(100),
    user_role VARCHAR2(50),
    ip_address VARCHAR2(50),
    user_agent VARCHAR2(500),
    old_value CLOB,
    new_value CLOB,
    description VARCHAR2(2000),
    timestamp TIMESTAMP NOT NULL,
    request_id VARCHAR2(100),
    session_id VARCHAR2(100),
    module VARCHAR2(100),
    success NUMBER(1) DEFAULT 1,
    error_message VARCHAR2(2000)
);

-- =====================================================
-- User-System Access Table (Many-to-Many)
-- =====================================================
CREATE TABLE user_systems (
    user_id NUMBER NOT NULL,
    system_id NUMBER NOT NULL,
    CONSTRAINT pk_user_systems PRIMARY KEY (user_id, system_id),
    CONSTRAINT fk_us_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_us_system FOREIGN KEY (system_id) REFERENCES source_systems(id)
);

-- =====================================================
-- Indexes for Performance
-- =====================================================
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_source_systems_code ON source_systems(system_code);
CREATE INDEX idx_source_systems_type ON source_systems(system_type);
CREATE INDEX idx_recon_configs_code ON reconciliation_configs(config_code);
CREATE INDEX idx_recon_configs_source ON reconciliation_configs(source_system_id);
CREATE INDEX idx_recon_configs_target ON reconciliation_configs(target_system_id);
CREATE INDEX idx_recon_runs_config ON reconciliation_runs(reconciliation_config_id);
CREATE INDEX idx_recon_runs_status ON reconciliation_runs(status);
CREATE INDEX idx_recon_runs_started ON reconciliation_runs(started_at);
CREATE INDEX idx_discrepancies_run ON discrepancies(reconciliation_run_id);
CREATE INDEX idx_discrepancies_type ON discrepancies(discrepancy_type);
CREATE INDEX idx_discrepancies_severity ON discrepancies(severity);
CREATE INDEX idx_discrepancies_key ON discrepancies(record_key);
CREATE INDEX idx_incidents_number ON incidents(incident_number);
CREATE INDEX idx_incidents_status ON incidents(status);
CREATE INDEX idx_incidents_severity ON incidents(severity);
CREATE INDEX idx_incidents_assigned ON incidents(assigned_to_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_user ON audit_logs(username);

-- =====================================================
-- Insert Sample Data
-- =====================================================

-- Insert Admin User
INSERT INTO users (id, username, email, password_hash, first_name, last_name, department)
VALUES (seq_users.NEXTVAL, 'admin', 'admin@example.com', 
        '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG', -- password: admin123
        'System', 'Administrator', 'IT');

INSERT INTO user_roles (user_id, role) VALUES (1, 'ADMIN');
INSERT INTO user_roles (user_id, role) VALUES (1, 'CHECKER');

-- Insert Sample Analyst
INSERT INTO users (id, username, email, password_hash, first_name, last_name, department)
VALUES (seq_users.NEXTVAL, 'analyst', 'analyst@example.com',
        '$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG',
        'Data', 'Analyst', 'Risk');

INSERT INTO user_roles (user_id, role) VALUES (2, 'RISK_ANALYST');
INSERT INTO user_roles (user_id, role) VALUES (2, 'MAKER');

-- Insert Sample Source Systems
INSERT INTO source_systems (id, system_code, system_name, description, system_type, host, port, database_name, schema_name, data_owner)
VALUES (seq_source_systems.NEXTVAL, 'CORE_BANKING', 'Core Banking System', 'Main banking transaction system', 'DATABASE', 'db-server-01', 1521, 'COREDB', 'BANKING', 'Treasury Team');

INSERT INTO source_systems (id, system_code, system_name, description, system_type, host, port, database_name, schema_name, data_owner)
VALUES (seq_source_systems.NEXTVAL, 'RISK_SYSTEM', 'Risk Management System', 'Enterprise risk management platform', 'DATABASE', 'db-server-02', 1521, 'RISKDB', 'RISK', 'Risk Team');

INSERT INTO source_systems (id, system_code, system_name, description, system_type, file_path, data_owner)
VALUES (seq_source_systems.NEXTVAL, 'EXTERNAL_FEED', 'External Data Feed', 'Daily external data files', 'FILE_SYSTEM', '/data/external/feeds', 'Data Team');

COMMIT;

