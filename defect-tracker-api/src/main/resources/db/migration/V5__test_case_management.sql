-- ==============================================================================
-- Flyway Migration V5: Test Case Management
-- ==============================================================================

CREATE TABLE IF NOT EXISTS test_cases (
    id BIGSERIAL PRIMARY KEY,
    test_case_code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    steps TEXT NOT NULL,
    expected_result TEXT NOT NULL,
    sub_module_id BIGINT NOT NULL REFERENCES submodules(id) ON DELETE CASCADE,
    severity_id BIGINT REFERENCES severities(id) ON DELETE SET NULL,
    defect_type_id BIGINT REFERENCES defect_types(id) ON DELETE SET NULL,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_test_cases_submodule ON test_cases(sub_module_id);
CREATE INDEX IF NOT EXISTS idx_test_cases_code ON test_cases(test_case_code);
