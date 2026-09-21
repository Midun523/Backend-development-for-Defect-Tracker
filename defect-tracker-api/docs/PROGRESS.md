| Prompt | Done | Tests enabled | Notes |
| --- | --- | --- | --- |
| R0-A/B/C | yes | - | baseline measured |
| R1 | yes | - | 7 R1 tests added & green, V9 migration, secrets/logs clean |
| R2 | yes | FR_AUTH_04, FR_ORG_06, 12 R2StatusCodesTests | Security exception handling, 401/403/404/405/409/413/415/500 mappings, safe constraint names & correlation IDs, stale auth header support |
| R3 | yes | 16 R3TokensAndRecoveryTests | Token types (ACCESS/REFRESH/jti/iss), V10 migration (family_id & revoked_reason), session isolation, token rotation with 30s grace window, generic NotificationService delegation to EmailService, single-use 10-min reset tokens, neutral forgot-password, validate-reset-token endpoint, scheduled cleanup job, unified @StrongPassword policy |
| R4(a) | yes | R4GroupATests, GlobalLeakTest, GroupAArchUnitTest | Commit d9c6931: Response DTOs & MapStruct mappers for Employee, Role, Permission, EmailConfig, EmailTemplate, KlocMetric. Safety net WRITE_ONLY annotations on entity credentials. GlobalLeakTest covers 95 GET endpoints with 0 leaks. Golden schema matching. |
| R4(b) | yes | R4GroupBTests, GlobalLeakTest, GroupBArchUnitTest | Response DTOs & MapStruct mappers for Project, Defect, Release, ProjectAllocation, DefectComment, DefectHistory, DefectStatusLog, TestCaseAllocationLog, ReleaseCounts, DefectImport. 26 golden GET schemas verified. Parity on write endpoints. Zero entity leaks. |
| Phase 1 | yes | B08, 10 Phase1IntegrityTests | Defect creation integrity, no first-project fallback, derived project agreement, active/allocated assignee check, per-project sequences (DEF/TC/REL), DB sequences for PRJ/US, no literal name defaults, @Transactional on all services, PageableUtils max 100, @Valid on controllers |

TODO before deploy: rotate JWT/DB/admin secrets, purge git history
