-- ==============================================================================
-- Flyway Migration V5: Test Case Management
-- ==============================================================================

CREATE TABLE IF NOT EXISTS test_cases (
    id BIGSERIAL PRIMARY KEY,
    testcase_no VARCHAR(50) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    details_steps TEXT,
    expected_result TEXT,
    sub_module_id BIGINT NOT NULL REFERENCES sub_modules(id) ON DELETE CASCADE,
    severity_id BIGINT REFERENCES severities(id) ON DELETE SET NULL,
    defect_type_id BIGINT REFERENCES defect_types(id) ON DELETE SET NULL,
    type VARCHAR(50),
    test_case_required BOOLEAN DEFAULT TRUE,
    execution_status VARCHAR(30) DEFAULT 'NOT_RUN',
    assigned_qa_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_test_cases_submodule ON test_cases(sub_module_id);
CREATE INDEX IF NOT EXISTS idx_test_cases_no ON test_cases(testcase_no);
