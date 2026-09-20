# Defect Tracker Pro - Backend API (Spring Boot 3 + PostgreSQL)

Enterprise backend REST API for the Defect Tracker Pro application built with **Java 21**, **Spring Boot 3.3.4**, **Spring Data JPA**, **Spring Security with JWT**, **PostgreSQL**, **Swagger OpenAPI 3**, **Jakarta Mail (SMTP)**, and **Pluggable File Attachment Storage**.

---

## 🚀 Quick Start

### 1. Prerequisites
- **Java 21 LTS**
- **PostgreSQL 14+** (running locally on port `5432`)

### 2. Setup PostgreSQL Database
Create the database in PostgreSQL:
```sql
CREATE DATABASE defect_tracker_db;
```

### 2. Configuration & Environment Variables

> [!IMPORTANT]
> Spring Boot does **NOT** read `.env` files. Secrets must be passed as environment variables or provided via a local properties file.

The application requires the following environment variables:
- `DB_PASSWORD`: PostgreSQL database password (no default).
- `JWT_SECRET`: Hex or string secret for signing JWTs (must be at least 32 bytes / 256 bits).
- `SEED_ADMIN_PASSWORD`: Password for the initial seeded admin account (must be at least 12 characters long and not a known weak password).
- `DB_URL` *(Optional)*: JDBC URL (default: `jdbc:postgresql://localhost:5432/defect_tracker_db`).
- `DB_USERNAME` *(Optional)*: Database username (default: `postgres`).

You can run locally in one of two ways:

#### Option A: Set Environment Variables in Shell
```powershell
$env:DB_PASSWORD = "your_postgres_password"
$env:JWT_SECRET = "9a4f2c8d3e7b1a5f6e8d2c4b7a9f1e3c5d7b9a2f4e6d8c1b3a5f7e9d2c4b6a8f"
$env:SEED_ADMIN_PASSWORD = "SuperSecurePassword2026!"
./gradlew bootRun
```

#### Option B: Use a git-ignored `application-local.properties`
Create `src/main/resources/application-local.properties` (ignored by git):
```properties
spring.datasource.password=your_postgres_password
app.jwt.secret=9a4f2c8d3e7b1a5f6e8d2c4b7a9f1e3c5d7b9a2f4e6d8c1b3a5f7e9d2c4b6a8f
app.seed.admin.password=SuperSecurePassword2026!
```
And run with the `local` profile:
```powershell
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 3. Run the Backend API
In this folder (`defect-tracker-api`), run:
```powershell
./gradlew bootRun
```
The server will start on port **`8087`** (matches frontend `.env` config `VITE_BASE_URL=http://localhost:8087`).

---

## 🔑 Default Credentials & Out-of-the-Box Seed Data

When the application boots with an empty database, it automatically seeds:
- **Super Admin Account**:
  - **Email**: `admin@defecttracker.com`
  - **Username / User ID**: `US0001`
  - **Password**: Configured at startup via `SEED_ADMIN_PASSWORD` (forced password change on first login)
  - **Role**: `Super Admin` (assigned `ALL_PERMISSIONS`)
- **Master Data**:
  - **67 System Permissions**
  - **6 System Roles** (Super Admin, Project Manager, Tech Lead, Developer, QA Lead, QA Tester)
  - **7 Designations** (Project Manager, Senior Software Engineer, Lead QA, DevOps, UI/UX, etc.)
  - **4 Priority Levels** (Low, Medium, High, Critical)
  - **4 Severity Levels** (Low, Medium, High, Critical)
  - **7 Defect Types** (UI, Functionality, Performance, Security, Backend/API, Database, Logic)
  - **4 Release Types** (Major, Minor, Patch, Hotfix)
  - **7 Status Types & Workflow Transitions** (New → Open → In Progress → Resolved → Closed / Reopened)
  - **Default Email Templates & SMTP Config**
  - **Sample Project (`PRJ001`), Module, Submodule, Test Case, and Defect**

---

## 📚 Interactive API Documentation (Swagger UI)

Once running, access Swagger UI in your browser:
👉 **[http://localhost:8087/swagger-ui/index.html](http://localhost:8087/swagger-ui/index.html)**
👉 OpenAPI JSON Spec: **[http://localhost:8087/v3/api-docs](http://localhost:8087/v3/api-docs)**

---

## 🔌 API Endpoint Summary

| Module | Method | Endpoint | Description |
|---|---|---|---|
| **Auth** | `POST` | `/api/v1/auth/login` | Authenticate & get JWT tokens |
| | `POST` | `/api/v1/auth/refresh-token` | Refresh expired access token |
| | `POST` | `/api/v1/auth/change-password` | Change user password |
| | `POST` | `/api/v1/auth/forget-password` | Request password reset token |
| | `POST` | `/api/v1/auth/reset-password` | Reset password using token |
| | `GET` | `/api/v1/user/me/permissions` | Get user permissions |
| | `GET` | `/api/v1/user/me/projects` | Get user accessible projects |
| **Employees** | `POST` | `/api/v1/employee` | Create employee with user record |
| | `GET` | `/api/v1/employee` | Search & paginated employee list |
| | `GET` | `/api/v1/employee/{id}` | Get employee by ID |
| | `PUT` | `/api/v1/employee/{id}` | Update employee details |
| | `DELETE` | `/api/v1/employee/{id}` | Delete employee |
| | `PATCH` | `/api/v1/employee/{id}/status` | Update employee status |
| | `GET` | `/api/v1/bench` | Get available bench employees |
| **Projects** | `POST` | `/api/v1/project` | Create project |
| | `GET` | `/api/v1/project` | Search & list projects |
| | `GET` | `/api/v1/project/{id}` | Get project by ID |
| | `PUT` | `/api/v1/project/{id}` | Update project |
| | `DELETE` | `/api/v1/project/{id}` | Delete project |
| | `PATCH` | `/api/v1/project/{id}/project-kilo-of-code` | Update project KLOC |
| **Allocations** | `POST` | `/api/v1/project-allocation` | Allocate employee to project |
| | `GET` | `/api/v1/project-allocation/{projectId}/employee` | Get allocated employees |
| | `GET` | `/api/v1/project-allocation/{projectId}/employee_history` | Allocation history |
| | `DELETE` | `/api/v1/project-allocation/employee/{id}` | Deallocate employee |
| | `PATCH` | `/api/v1/project-allocation/employee/{id}/extend` | Extend allocation end date |
| **Modules** | `POST` | `/api/v1/project/{projectId}/module` | Create module |
| | `GET` | `/api/v1/project/{projectId}/module` | Get project modules |
| | `POST` | `/api/v1/module/{moduleId}/sub-module` | Create submodule |
| | `GET` | `/api/v1/module/{moduleId}/sub-module` | Get submodules |
| | `POST` | `/api/v1/sub-module/{id}/employee` | Assign developer to submodule |
| | `POST` | `/api/v1/allocate-module-leader` | Allocate module leader |
| **Test Cases** | `POST` | `/api/v1/sub-module/{subModuleId}/test-case` | Create test case |
| | `GET` | `/api/v1/sub-module/{subModuleId}/test-case` | Filter test cases |
| | `POST` | `/api/v1/test-case/bulk` | Bulk import test cases |
| **Releases** | `POST` | `/api/v1/release` | Create release |
| | `GET` | `/api/v1/release` | Get all releases |
| | `GET` | `/api/v1/project/{projectId}/release/active` | Get active release |
| | `GET` | `/api/v1/release/{releaseId}/test-case` | Get release test cases |
| | `POST` | `/api/v1/release/{releaseId}/test-case/{testcaseId}/employee` | Assign QA to testcase |
| | `PATCH` | `/api/v1/release/{releaseId}/test-case/{id}/status` | Update execution status |
| **Defects** | `POST` | `/api/v1/defect` | Log defect (JSON or multipart) |
| | `GET` | `/api/v1/defect` | Filter & paginated defects |
| | `GET` | `/api/v1/defect/{id}` | Get defect by ID |
| | `PUT` | `/api/v1/defect/{id}` | Update defect |
| | `PATCH` | `/api/v1/defect/{id}/status` | Transition status & history log |
| | `POST` | `/api/v1/defects/bulk-reassign` | Bulk reassign defects |
| | `GET` | `/api/v1/defect/{id}/comment` | Get comments |
| | `POST` | `/api/v1/defect/{id}/comment` | Add comment |
| **Dashboard** | `GET` | `/api/v1/project/{projectId}/release/{releaseId}/dashboard` | Summary KPIs |
| | `GET` | `/api/v1/project/{projectId}/defect/severity-breakdown` | Severity breakdown |
| | `GET` | `/api/v1/project/{projectId}/defect-density` | Defect density |
| | `GET` | `/api/v1/project/{projectId}/dashboard/reopened` | Reopened defects |
| **Email** | `GET` | `/api/v1/email/config` | SMTP configurations |
| | `POST` | `/api/v1/email/config` | Create SMTP config |
| | `PATCH` | `/api/v1/email/config/{id}/enable` | Enable default SMTP |
| | `GET` | `/api/v1/email/template` | Email templates |
| | `GET` | `/api/v1/email/log` | Sent email logs |
| **WhatsApp** | `POST` | `/api/v1/whatsapp/send` | Send WhatsApp notification |
| | `GET` | `/api/v1/whatsapp/status` | Check bridge status |

---

## 🧪 Testing the Build

Tests run against a dedicated local PostgreSQL database (default `defect_tracker_test`).

### Test Database Setup
1. Create the test database in PostgreSQL:
   ```sql
   CREATE DATABASE defect_tracker_test;
   ```
2. Set the `TEST_DB_PASSWORD` environment variable (required — fail-fast if unset):
   ```powershell
   $env:TEST_DB_PASSWORD = "your_postgres_password"
   ```
   *(Optional)* If using a custom URL or username:
   ```powershell
   $env:TEST_DB_URL = "jdbc:postgresql://localhost:5432/defect_tracker_test"
   $env:TEST_DB_USERNAME = "postgres"
   ```

### Running Tests
To run the automated tests using Gradle:
```powershell
./gradlew test
```
Or for a clean test run:
```powershell
./gradlew clean test
```
