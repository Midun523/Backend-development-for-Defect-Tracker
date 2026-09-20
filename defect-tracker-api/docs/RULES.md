PROJECT: defect-tracker-api. Spring Boot 3.3.4, Java 21, PostgreSQL, Flyway (ddl-auto=validate), Gradle. Root package com.defecttracker.
SOURCES OF TRUTH: SPEC.md (requirements FR-*, NFR-*) and docs/COMPLIANCE.md (IDs T1-T9). Where SPEC.md and the code disagree, SPEC.md wins
unless I say otherwise. Report every disagreement; never silently pick.

FRONTEND CONSTRAINT (do not violate):
- The React frontend in Defect-Tracker-FE-Base-Final is FIXED. Its contract is src/utils/apiendpoint.ts. Never rename or remove a route it calls.
- It depends on: envelope {status,statusCode,message,statusMessage,data}; page {content,pageNumber,pageSize,totalElements,totalPages,first,last};
  login/refresh responses as read in src/services/authService.ts and src/lib/api.ts (it refreshes ONLY on HTTP 401 via ENDPOINTS.refreshToken).
- Parts of the frontend are mocked (see docs/frontend-blockers.md). Build the backend behaviour anyway; list what the UI cannot reach in that file.
- Do not edit anything under Defect-Tracker-FE-Base-Final unless the prompt is R-FE.

HARD RULES
1. Extend existing classes. Search before creating a class; never create a second class with the same simple name.
2. Never .orElse(null) a REQUIRED reference; use orElseThrow(ResourceNotFoundException). Optional references only when the field is truly optional in SPEC.md.
3. No controller method returns or accepts a JPA entity (after R4).
4. No secret, password, token or reset link in source, logs or API responses.
5. Entity change => a new Flyway migration (V9 and up). Never depend on ddl-auto.
6. Business logic depends on status CATEGORY / enum values, never on display names.
7. Every rule gets a test whose name contains its requirement ID. No fake success responses, no catch-and-ignore.
8. Do not modify or delete files a prompt did not name. If you must, STOP and ask.
9. Never run git commit, git push, git reset, git checkout or git rebase. I manage version control.

WORKFLOW FOR EVERY TASK
0) Read docs/PROGRESS.md. If a prompt's prerequisites are not marked done, stop and tell me.
a) Read the named files.  b) Output a PLAN (files to change/create, migrations, risks, frontend impact) and WAIT for my "proceed".
c) Implement in small steps.  d) Run ./gradlew clean test and paste the summary.
e) State every behaviour change, every frontend-visible change (grep apiendpoint.ts), and everything you did NOT do.  f) Do not start the next task.
