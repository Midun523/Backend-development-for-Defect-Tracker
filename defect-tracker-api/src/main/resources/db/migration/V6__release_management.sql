-- ==============================================================================
-- Flyway Migration V6: Release Management & Test Case Execution Runs
-- ==============================================================================

-- 1. Releases
CREATE TABLE IF NOT EXISTS releases (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    version VARCHAR(50) NOT NULL,
    release_date DATE,
    release_type_id BIGINT REFERENCES release_types(id) ON DELETE SET NULL,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    status VARCHAR(50) DEFAULT 'ACTIVE' NOT NULL, -- ACTIVE, CLOSED, PLANNED, CANCELLED
    kloc NUMERIC(10, 2) DEFAULT 0.00 NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_project_release_version UNIQUE (project_id, version)
);

CREATE INDEX IF NOT EXISTS idx_releases_project ON releases(project_id);

-- 2. Release Test Cases (Linking test cases to release, QA allocation & execution results)
CREATE TABLE IF NOT EXISTS release_test_cases (
    id BIGSERIAL PRIMARY KEY,
    release_id BIGINT NOT NULL REFERENCES releases(id) ON DELETE CASCADE,
    test_case_id BIGINT NOT NULL REFERENCES test_cases(id) ON DELETE CASCADE,
    assigned_to_id BIGINT REFERENCES employees(id) ON DELETE SET NULL, -- QA allocation
    execution_status VARCHAR(50) DEFAULT 'PENDING' NOT NULL, -- PENDING, PASSED, FAILED, BLOCKED, SKIPPED
    executed_by_id BIGINT REFERENCES employees(id) ON DELETE SET NULL, -- executing employee
    executed_at TIMESTAMP WITHOUT TIME ZONE,
    execution_notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_release_test_case UNIQUE (release_id, test_case_id)
);

CREATE INDEX IF NOT EXISTS idx_release_tc_release ON release_test_cases(release_id);
CREATE INDEX IF NOT EXISTS idx_release_tc_assigned ON release_test_cases(assigned_to_id);
