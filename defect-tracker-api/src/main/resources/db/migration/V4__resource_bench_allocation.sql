-- ==============================================================================
-- Flyway Migration V4: Resource / Bench Project Allocations
-- ==============================================================================

CREATE TABLE IF NOT EXISTS project_allocations (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    start_date DATE,
    end_date DATE,
    allocation_percentage INT DEFAULT 100,
    role VARCHAR(50),
    status VARCHAR(30) DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_project_allocations_proj ON project_allocations(project_id);
CREATE INDEX IF NOT EXISTS idx_project_allocations_emp ON project_allocations(employee_id);
