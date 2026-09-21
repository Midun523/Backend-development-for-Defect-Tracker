| Prompt | Done | Tests enabled | Notes |
| --- | --- | --- | --- |
| R0-A/B/C | yes | - | baseline measured |
| R1 | yes | - | 7 R1 tests added & green, V9 migration, secrets/logs clean |
| R2 | yes | FR_AUTH_04, FR_ORG_06, 12 R2StatusCodesTests | Security exception handling, 401/403/404/405/409/413/415/500 mappings, safe constraint names & correlation IDs, stale auth header support |
| R3 | yes | 16 R3TokensAndRecoveryTests | Token types (ACCESS/REFRESH/jti/iss), V10 migration (family_id & revoked_reason), session isolation, token rotation with 30s grace window, generic NotificationService delegation to EmailService, single-use 10-min reset tokens, neutral forgot-password, validate-reset-token endpoint, scheduled cleanup job, unified @StrongPassword policy |
