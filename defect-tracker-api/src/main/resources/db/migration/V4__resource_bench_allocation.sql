-- ==============================================================================
-- Flyway Migration V4: Resource / Bench Allocation & Immutable Allocation History
-- ==============================================================================

-- 1. Active Project Allocations
CREATE TABLE IF NOT EXISTS project_allocations (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    allocation_percentage NUMERIC(5, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    role_in_project VARCHAR(100) DEFAULT 'Developer',
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 2. Immutable Allocation History
-- Retains snapshot fields so records survive employee or project deletion
CREATE TABLE IF NOT EXISTS allocation_histories (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT,
    employee_code VARCHAR(50),
    employee_name VARCHAR(200) NOT NULL,
    project_id BIGINT,
    project_name VARCHAR(200) NOT NULL,
    allocation_percentage NUMERIC(5, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    action VARCHAR(50) NOT NULL, -- 'ALLOCATED', 'DEALLOCATED', 'MODIFIED', 'PROJECT_ENDED'
    duration_days BIGINT,
    note VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_alloc_hist_emp ON allocation_histories(employee_id);
CREATE INDEX IF NOT EXISTS idx_alloc_hist_prj ON allocation_histories(project_id);
