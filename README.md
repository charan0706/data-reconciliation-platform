# Data Reconciliation Platform

An enterprise-grade automated data reconciliation and quality validation platform built with Java 17, Spring Boot, Oracle Database, and Angular.

## 🎯 Overview

This platform automates the reconciliation process across multiple source systems to identify data mismatches and quality issues. It reduces manual reconciliation effort by 70-85% while ensuring accuracy and consistency of enterprise data.

### Key Features

- **Automated Data Extraction**: Extract data from databases, file systems, APIs, and SFTP sources
- **Flexible Mapping Logic**: Configure attribute-level mappings with various comparison types
- **Comprehensive Comparison Engine**: Record-level and attribute-level comparison with configurable tolerances
- **Discrepancy Management**: Flag discrepancies with severity levels and detailed reporting
- **Maker-Checker Workflow**: Incident management with full maker-checker approval process
- **Scheduling Support**: On-demand and scheduled reconciliations (hourly, daily, weekly, monthly)
- **Audit Trail**: Complete audit logging for regulatory compliance
- **Dashboard & Reports**: Real-time dashboards and comprehensive reporting

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                         Angular Frontend                              │
│   Dashboard │ Systems │ Reconciliation │ Incidents │ Reports         │
└─────────────────────────────────────────────────────────────────────┘
                                   │
                                   │ REST API
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Spring Boot Backend                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │ Controllers  │  │  Services    │  │ Schedulers   │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
│                                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │ Reconciliation│ │   Incident   │  │   Audit      │              │
│  │    Engine    │  │   Service    │  │   Service    │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
└─────────────────────────────────────────────────────────────────────┘
                                   │
                                   │ JPA/JDBC
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      Oracle Database                                  │
│   Users │ Systems │ Configs │ Runs │ Discrepancies │ Incidents      │
└─────────────────────────────────────────────────────────────────────┘
```

## 📁 Project Structure

```
data-reconciliation-platform/
├── backend/                    # Spring Boot Backend
│   ├── src/main/java/com/reconciliation/
│   │   ├── config/            # Configuration classes
│   │   ├── controller/        # REST Controllers
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── entity/            # JPA Entities
│   │   ├── enums/             # Enumerations
│   │   ├── exception/         # Custom Exceptions
│   │   ├── repository/        # JPA Repositories
│   │   ├── scheduler/         # Scheduled Jobs
│   │   ├── service/           # Business Services
│   │   └── util/              # Utility Classes
│   └── src/main/resources/
│       └── application.yml    # Application Configuration
│
├── frontend/                   # Angular Frontend
│   ├── src/app/
│   │   ├── core/              # Core Services & Guards
│   │   ├── shared/            # Shared Components
│   │   ├── features/          # Feature Modules
│   │   │   ├── dashboard/
│   │   │   ├── systems/
│   │   │   ├── reconciliation/
│   │   │   ├── incidents/
│   │   │   └── reports/
│   │   └── layouts/           # Layout Components
│   └── src/environments/
│
├── database/                   # Database Scripts
│   └── oracle_schema.sql      # Oracle DDL Script
│
└── docs/                       # Documentation
```

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- Node.js 18+ and npm
- Oracle Database 19c+ (or use H2 for development)

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/data-reconciliation-platform.git
   cd data-reconciliation-platform
   ```

2. **Configure Database**
   
   For Oracle (Production):
   ```yaml
   # backend/src/main/resources/application.yml
   spring:
     datasource:
       url: jdbc:oracle:thin:@//localhost:1521/ORCL
       username: reconciliation_user
       password: your_password
   ```
   
   For H2 (Development): Default configuration uses H2 in-memory database

3. **Run Database Scripts** (Oracle only)
   ```bash
   sqlplus reconciliation_user/password@ORCL @database/oracle_schema.sql
   ```

4. **Build and Run Backend**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```
   
   Backend will be available at: `http://localhost:8080/api`
   
   Swagger UI: `http://localhost:8080/api/swagger-ui.html`

### Frontend Setup

1. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Run Development Server**
   ```bash
   npm start
   ```
   
   Frontend will be available at: `http://localhost:4200`

## 📖 API Documentation

### Systems API
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/systems` | GET | List all systems |
| `/v1/systems/{id}` | GET | Get system by ID |
| `/v1/systems` | POST | Create new system |
| `/v1/systems/{id}` | PUT | Update system |
| `/v1/systems/{id}` | DELETE | Deactivate system |
| `/v1/systems/{id}/test-connection` | POST | Test connection |

### Reconciliation API
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/reconciliation/configs` | GET | List all configurations |
| `/v1/reconciliation/configs/{id}` | GET | Get configuration |
| `/v1/reconciliation/configs` | POST | Create configuration |
| `/v1/reconciliation/configs/{configId}/run` | POST | Trigger reconciliation |
| `/v1/reconciliation/runs` | GET | List all runs |
| `/v1/reconciliation/runs/{runId}` | GET | Get run details |

### Incidents API
| Endpoint | Method | Description |
|----------|--------|-------------|
| `/v1/incidents` | GET | List all incidents |
| `/v1/incidents/{id}` | GET | Get incident details |
| `/v1/incidents/{id}/assign` | POST | Assign incident |
| `/v1/incidents/{id}/start-investigation` | POST | Start investigation |
| `/v1/incidents/{id}/submit-resolution` | POST | Submit resolution |
| `/v1/incidents/{id}/approve` | POST | Approve resolution |
| `/v1/incidents/{id}/reject` | POST | Reject resolution |

## 🔧 Configuration

### Reconciliation Configuration Options

```json
{
  "configCode": "RECON-001",
  "configName": "Core Banking Reconciliation",
  "sourceSystemId": 1,
  "targetSystemId": 2,
  "sourceQuery": "SELECT * FROM transactions WHERE date = :runDate",
  "targetQuery": "SELECT * FROM risk_transactions WHERE date = :runDate",
  "primaryKeyAttributes": "transaction_id,account_number",
  "scheduleFrequency": "DAILY",
  "cronExpression": "0 0 1 * * *",
  "tolerancePercentage": 0.01,
  "ignoreCase": true,
  "trimWhitespace": true,
  "autoCreateIncidents": true,
  "attributeMappings": [
    {
      "sourceAttribute": "amount",
      "targetAttribute": "transaction_amount",
      "comparisonType": "NUMERIC_TOLERANCE",
      "toleranceValue": 0.01,
      "mismatchSeverity": "HIGH"
    }
  ]
}
```

### Comparison Types

- `EXACT_MATCH` - Values must match exactly
- `CASE_INSENSITIVE` - String comparison ignoring case
- `NUMERIC_TOLERANCE` - Numeric comparison with tolerance
- `DATE_TOLERANCE` - Date comparison with tolerance
- `CONTAINS` - One value contains the other
- `REGEX_MATCH` - Regular expression match
- `IGNORE` - Skip attribute in comparison

## 📊 Workflow

### Reconciliation Flow
```
1. Extract Source Data → 2. Extract Target Data → 3. Apply Mappings
       ↓                         ↓                        ↓
4. Compare Records → 5. Identify Discrepancies → 6. Create Incidents
       ↓                         ↓                        ↓
7. Generate Report → 8. Send Notifications → 9. Log Audit Trail
```

### Maker-Checker Workflow
```
OPEN → ASSIGNED → UNDER_INVESTIGATION → PENDING_CHECKER_REVIEW
                                              ↓
                               CHECKER_REJECTED ←→ RESOLVED → CLOSED
```

## 🔐 Security

- JWT-based authentication
- Role-based access control (RBAC)
- Roles: ADMIN, DATA_OWNER, RISK_ANALYST, MAKER, CHECKER, VIEWER
- Encrypted password storage
- API rate limiting
- CORS configuration for frontend

## 📈 Success Metrics

| Metric | Target |
|--------|--------|
| Automation of data extraction | 100% |
| Mismatch identification accuracy | < 1% error rate |
| Dashboard creation for discrepancies | 100% coverage |
| On-demand & Scheduled support | Both supported |
| Complete audit trail | Full compliance |

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📧 Support

For support, email support@example.com or create an issue in the repository.

