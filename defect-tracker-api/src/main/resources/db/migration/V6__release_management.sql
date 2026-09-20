-- ==============================================================================
-- Flyway Migration V6: Release Management, Release Test Cases & Allocation Logs
-- ==============================================================================

-- 1. Releases
CREATE TABLE IF NOT EXISTS releases (
    id BIGSERIAL PRIMARY KEY,
    release_no VARCHAR(50),
    name VARCHAR(150) NOT NULL,
    version VARCHAR(50) NOT NULL,
    description VARCHAR(2000),
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    release_type_id BIGINT REFERENCES release_types(id) ON DELETE SET NULL,
    status VARCHAR(30) DEFAULT 'PLANNED' NOT NULL,
    start_date DATE,
    release_date DATE,
    end_date DATE,
    kloc DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_releases_project ON releases(project_id);

-- 2. Release Test Cases
CREATE TABLE IF NOT EXISTS release_test_cases (
    id BIGSERIAL PRIMARY KEY,
    release_id BIGINT NOT NULL REFERENCES releases(id) ON DELETE CASCADE,
    test_case_id BIGINT NOT NULL REFERENCES test_cases(id) ON DELETE CASCADE,
    assigned_qa_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    linked_defect_id BIGINT,
    execution_status VARCHAR(30) DEFAULT 'NOT_RUN',
    execution_comment VARCHAR(1000),
    executed_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_release_test_case UNIQUE (release_id, test_case_id)
);

CREATE INDEX IF NOT EXISTS idx_release_tc_release ON release_test_cases(release_id);
CREATE INDEX IF NOT EXISTS idx_release_tc_assigned ON release_test_cases(assigned_qa_id);

-- 3. Test Case Allocation Logs
CREATE TABLE IF NOT EXISTS test_case_allocation_logs (
    id BIGSERIAL PRIMARY KEY,
    release_id BIGINT REFERENCES releases(id) ON DELETE CASCADE,
    test_case_id BIGINT REFERENCES test_cases(id) ON DELETE CASCADE,
    employee_id BIGINT REFERENCES employees(id) ON DELETE CASCADE,
    action VARCHAR(100),
    timestamp TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
