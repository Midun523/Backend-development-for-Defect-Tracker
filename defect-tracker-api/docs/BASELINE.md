# Baseline Report — R0-B

**Date**: 2026-09-21  
**Branch**: current working tree (pre-task)  
**JDK**: OpenJDK 21.0.11 (Temurin)  
**PostgreSQL**: 18.4  
**Spring Boot**: 3.3.4  

---

## 1. Build Results

### `./gradlew clean build -x test`

```
BUILD SUCCESSFUL in 2m
6 actionable tasks: 5 executed, 1 up-to-date
```

**Errors**: None  
**Warnings**: None  
**Compilation**: Clean — zero warnings, zero errors.

### `./gradlew test`

```
> Task :test

DefectTrackerApiApplicationTests > contextLoads() FAILED
    java.lang.IllegalStateException
        Caused by: org.flywaydb.core.internal.command.DbMigrate$FlywayMigrateException
            Caused by: org.flywaydb.core.internal.sqlscript.FlywaySqlScriptException
                Caused by: org.h2.jdbc.JdbcSQLSyntaxErrorException

1 test completed, 1 failed
BUILD FAILED
```

**Root cause**: `DefectTrackerApiApplicationTests` uses H2 in-memory for tests (`@TestPropertySource`), but
Flyway migrations V1–V8 contain PostgreSQL-specific syntax (`ON CONFLICT ... DO NOTHING`). H2 does not
support this syntax, so the Flyway migration fails at V1 line 106, crashing the Spring context before any
test can run.

---

## 2. Application Startup (PostgreSQL — clean database)

Database `defect_tracker_db` was dropped and recreated fresh before boot.

### Flyway

All 8 migrations applied successfully:

| Version | Description                     | Result  |
|---------|---------------------------------|---------|
| V1      | init master data and auth       | ✅ Applied |
| V2      | master data and workflow        | ✅ Applied |
| V3      | project and structure           | ✅ Applied |
| V4      | resource bench allocation       | ✅ Applied |
| V5      | test case management            | ✅ Applied |
| V6      | release management              | ✅ Applied |
| V7      | defect management and metrics   | ✅ Applied |
| V8      | email notifications and config  | ✅ Applied |

Successfully applied 8 migrations, now at version v8 (execution time 00:00.206s).

### Hibernate Schema Validation

**PASSED** — no `SchemaManagementException` or validation errors. The entity model matches the Flyway-created schema.

### Startup WARN/ERROR Lines

| # | Level | Source                            | Message |
|---|-------|-----------------------------------|---------|
| 1 | WARN  | Flyway `o.f.c.internal.database`  | PostgreSQL 18.4 is newer than this version of Flyway and support has not been tested. Latest supported: 16. |
| 2 | WARN  | Hibernate `orm.deprecation`       | `HHH90000025`: `PostgreSQLDialect` does not need to be specified explicitly (remove `spring.jpa.database-platform`). |
| 3 | WARN  | Spring Security                   | Global AuthenticationManager configured with an AuthenticationProvider bean. `UserDetailsService` beans will not be used for username/password login. |

No ERROR lines during startup.

### ⚠️ Security Violation in Startup Logs

```
INFO com.defecttracker.util.DataInitializer :
  Initialized Super Admin: admin@defecttracker.com / admin123
```

**The seeded admin password is logged in plaintext.** See `DataInitializer.java` line 200.
This violates Hard Rule 4 ("No secret, password, token or reset link in … logs").

---

## 3. Smoke Tests

### 3a) Login as Seeded Admin

```
POST /api/v1/auth/login
Content-Type: application/json

{"username":"US0001","password":"admin123"}
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "statusCode": 200,
  "message": "Login successful",
  "statusMessage": "Login successful",
  "data": {
    "token": "<REDACTED_JWT>",
    "refreshToken": "<REDACTED_JWT>",
    "type": "Bearer",
    "userId": 1,
    "employeeId": 1,
    "companyStaffId": 1,
    "email": "admin@defecttracker.com",
    "firstName": "Super",
    "lastName": "Admin",
    "userType": "CompanyStaff",
    "roles": ["Super Admin"],
    "globalPermissions": ["PROJECT_UPDATE", "DEFECT_COMMENT_CREATE", "...70 total..."],
    "projectAccessList": []
  }
}
```

**Observations**:
- Login via `username` field works (`US0001`).
- Sending only `email` without `username` returns `400` because `@NotBlank` on `username` is mandatory.
- The frontend sends `{ username: email, password }` — so it works by putting the email/username in the `username` field.

---

### 3b) Endpoint with Garbage Token — Expected 401

```
GET /api/v1/employee
Authorization: Bearer garbage.token.here
```

**Response**: `403 Forbidden` ❌ (Expected: `401 Unauthorized`)

```json
{
  "timestamp": "2026-09-20T22:04:10.438+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/v1/employee"
}
```

**Root cause**: `JwtAuthenticationFilter.doFilterInternal()` catches token-parse exceptions and **swallows them**
(line 46: `catch (Exception ex)`), then continues the filter chain with no `Authentication` in
`SecurityContext`. Spring Security treats the request as anonymous and blocks it via `@PreAuthorize`, which
triggers `AccessDeniedException` → 403, not `AuthenticationException` → 401.

Additionally, `SecurityConfig.securityFilterChain()` does not configure
`.exceptionHandling(e -> e.authenticationEntryPoint(jwtAuthEntryPoint))`, so the custom
`JwtAuthEntryPoint` (which correctly returns 401) is **never wired**.

**Frontend impact**: `api.ts` line 139 refreshes tokens **only on HTTP 401**. Since the backend never
returns 401, **the frontend token-refresh flow is completely broken**. Expired tokens will cause
the user to see a generic error rather than being silently refreshed or redirected to login.

---

### 3c) Endpoint Without Required Permission — Expected 403

```
GET /api/v1/employee
(no Authorization header)
```

**Response**: `403 Forbidden`

```json
{
  "timestamp": "2026-09-20T22:04:19.922+00:00",
  "status": 403,
  "error": "Forbidden",
  "path": "/api/v1/employee"
}
```

**Observation**: Returning 403 for unauthenticated requests is **incorrect** (should be 401). However,
for the specific "no permission" check (authenticated user lacking a permission), the same 403 code
would be correct. Because of the 401/403 conflation, it is not possible to distinguish
"not authenticated" from "not authorized" — both return 403.

---

### 3d) GET Employee List — Password/Token Leak Check

```
GET /api/v1/employee
Authorization: Bearer <VALID_TOKEN>
```

**Response**: `200 OK`

**⚠️ CRITICAL SECURITY VIOLATION — password and tokens exposed in API response:**

The JSON response contains the full `User` JPA entity serialized, including:

| Field                    | Exposed Value                                | Risk    |
|--------------------------|----------------------------------------------|---------|
| `user.password`          | `$2a$10$H/egIsyqWw...` (bcrypt hash)         | HIGH    |
| `user.userToken`         | `null` (but field exists)                     | MEDIUM  |
| `user.forgotPasswordToken` | `null` (but field exists)                   | MEDIUM  |
| `user.forgotPasswordTokenExpiry` | `null` (but field exists)             | MEDIUM  |
| `user.resetCount`        | `0`                                           | LOW     |

The employee endpoint returns the **raw JPA `User` entity** (including nested `Designation` and `Role`
objects), violating Hard Rules 3 and 4.

---

### 3e) Create Defect with Invalid `projectId`

```
POST /api/v1/defect
Content-Type: application/json
Authorization: Bearer <VALID_TOKEN>

{"title":"Test","description":"Test","projectId":99999,...}
```

**Response**: `404 Not Found`

```json
{
  "status": "error",
  "statusCode": 404,
  "message": "Project not found with id: '99999'",
  "statusMessage": "Project not found with id: '99999'"
}
```

**Result**: Properly rejected — no defect silently assigned to a wrong project. ✅

---

### 3f) Dashboard Time-to-Find for Two Different Releases

```
GET /api/v1/project/1/release/1/dashboard/time-to-find
GET /api/v1/project/1/release/2/dashboard/time-to-find
```

**Both responses**: `200 OK`

```json
{"data": {"releaseId": 1, "projectId": 1, "averageDaysToFind": 2.4}}
{"data": {"releaseId": 2, "projectId": 1, "averageDaysToFind": 2.4}}
```

**⚠️ STUB DATA**: Both non-existent releases return **identical hardcoded values**
(`averageDaysToFind: 2.4`). The `DashboardServiceImpl` does not query the database — it returns
static placeholder data. Note that `releaseId` and `projectId` in the response are echoed from
the path parameters, not from any actual database lookup.

The same issue applies to `time-to-fixed` and other dashboard endpoints.

---

### 3g) POST /api/v1/defect/import with Any File

```
POST /api/v1/defect/import
Content-Type: application/json
Authorization: Bearer <VALID_TOKEN>
Body: {}
```

**Response**: `200 OK`

```json
{
  "status": "success",
  "statusCode": 200,
  "message": "Defects imported successfully",
  "statusMessage": "Defects imported successfully",
  "data": {
    "total": 0,
    "success": 0,
    "imported": 0,
    "failed": 0,
    "message": "File processed successfully"
  }
}
```

**⚠️ STUB ENDPOINT**: `DefectController.importDefectsFile()` (lines 239–253) **always claims success**
regardless of input. It never reads the uploaded file. The response body is hardcoded:
`{"imported":0, "success":0, "failed":0, "total":0, "message":"File processed successfully"}`.

---

## Summary of Issues Found

| # | Severity | Issue | Files |
|---|----------|-------|-------|
| 1 | 🔴 Critical | Password hash and token fields exposed in API responses (employee list, defect responses, etc.) | All controllers returning JPA entities |
| 2 | 🔴 Critical | Admin password logged in plaintext at startup | `DataInitializer.java:200` |
| 3 | 🟠 High | Invalid/expired tokens return 403 instead of 401 — breaks frontend token refresh | `JwtAuthenticationFilter.java:46`, `SecurityConfig.java` (missing entry point) |
| 4 | 🟠 High | `JwtAuthEntryPoint` exists but is never wired into `SecurityFilterChain` | `SecurityConfig.java` |
| 5 | 🟡 Medium | Dashboard endpoints return hardcoded stub data | `DashboardServiceImpl` |
| 6 | 🟡 Medium | Defect import endpoint is a complete stub (always returns success) | `DefectController.java:239-253` |
| 7 | 🟡 Medium | Only test (`contextLoads`) fails due to H2/PostgreSQL incompatibility | `DefectTrackerApiApplicationTests.java` |
| 8 | 🟢 Low | Flyway warns about PG18 support not tested | Informational |
| 9 | 🟢 Low | Explicit `PostgreSQLDialect` not needed | `application.properties` |

---

## 4. Corrections (R0-C Measured Baseline)

This section records the actual measured results from executing scenarios (b) and (c) against the application running with the dedicated test database.

### Correction 4b: Authenticated User Without Permission Calling `GET /api/v1/employee`

- **Scenario**: A user is created with a role that has **zero permissions** assigned. The user authenticates successfully via `POST /api/v1/auth/login` and calls `GET /api/v1/employee` using their JWT bearer token.
- **Measured HTTP Status**: **`500 Internal Server Error`**
- **Measured Response Body**:
```json
{
  "status": "error",
  "statusCode": 500,
  "message": "Access Denied",
  "statusMessage": "Access Denied"
}
```
- **Measured Exception / Trace**:
```
2026-09-21T04:08:59.245+05:30 ERROR 4632 --- [Test worker] c.d.exception.GlobalExceptionHandler : Unhandled server exception: 
org.springframework.security.authorization.AuthorizationDeniedException: Access Denied
    at org.springframework.security.authorization.method.ThrowingMethodAuthorizationDeniedHandler.handleDeniedInvocation(ThrowingMethodAuthorizationDeniedHandler.java:38)
    at org.springframework.security.authorization.method.PreAuthorizeAuthorizationManager.handleDeniedInvocation(PreAuthorizeAuthorizationManager.java:92)
    ...
```
- **Finding**: When `@PreAuthorize("@access.has('EMPLOYEE_READ')")` denies access, Spring Security throws `AuthorizationDeniedException` (a subclass of `AccessDeniedException`). Because `GlobalExceptionHandler` has no handler for `AccessDeniedException` or `AuthorizationDeniedException`, the exception falls through to `@ExceptionHandler(Exception.class)` (`handleGlobalException`), which logs an unhandled error and returns HTTP 500 instead of HTTP 403 Forbidden.

---

### Correction 4c: Defect Creation With Non-Existent `projectId` When Projects Exist

- **Scenario**: One project exists in the database (`id: 1`, `name: "First Existing Project"`, `projectId: "PRJ99"`). A client submits `POST /api/v1/defect` specifying a non-existent `projectId` (`99999`) and no `module`, `release`, or `subModule`.
- **Measured HTTP Status**: **`200 OK`** (payload envelope `statusCode: 201`, `"message": "Defect created successfully"`)
- **Measured Response Body**:
```json
{
  "status": "success",
  "statusCode": 201,
  "message": "Defect created successfully",
  "statusMessage": "Defect created successfully",
  "data": {
    "id": 1,
    "defectId": "DEF001",
    "title": "Defect with unknown project",
    "description": "Should be rejected when project 99999 does not exist",
    "project": {
      "id": 1,
      "projectId": "PRJ99",
      "name": "First Existing Project",
      "prefix": "FEP",
      "status": "ACTIVE",
      "kloc": 10.0
    }
  }
}
```
- **Measured Project Attachment**: The created defect was **silently attached to Project ID 1 (`"First Existing Project"`)**.
- **Finding**: In R0-B, testing with an invalid `projectId` against a completely empty database returned `404 Not Found` because no projects existed. However, inspection and live measurement of `DefectServiceImpl.createDefect` (lines 77–88) reveals a silent fallback:
```java
if (project == null) {
    if (release != null && release.getProject() != null) {
        project = release.getProject();
    } else if (subModule != null && subModule.getModule() != null && subModule.getModule().getProject() != null) {
        project = subModule.getModule().getProject();
    } else if (module != null && module.getProject() != null) {
        project = module.getProject();
    } else {
        project = projectRepository.findAll().stream().findFirst().orElseThrow(
                () -> new ResourceNotFoundException("Project", "id", request.getProjectId())
        );
    }
}
```
When at least one project exists in the database, `projectRepository.findAll().stream().findFirst()` captures the first project in the table and silently attaches the defect to it instead of rejecting the non-existent `projectId`.

