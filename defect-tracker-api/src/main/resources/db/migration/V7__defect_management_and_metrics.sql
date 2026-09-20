-- ==============================================================================
-- Flyway Migration V7: Defect Management, Comments, Histories, Logs & KLOC Metrics
-- ==============================================================================

-- 1. Defects
CREATE TABLE IF NOT EXISTS defects (
    id BIGSERIAL PRIMARY KEY,
    defect_id VARCHAR(50) UNIQUE,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    steps TEXT,
    priority_id BIGINT REFERENCES priorities(id) ON DELETE SET NULL,
    severity_id BIGINT REFERENCES severities(id) ON DELETE SET NULL,
    defect_status_id BIGINT REFERENCES status_types(id) ON DELETE SET NULL,
    defect_type_id BIGINT REFERENCES defect_types(id) ON DELETE SET NULL,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    release_id BIGINT REFERENCES releases(id) ON DELETE SET NULL,
    module_id BIGINT REFERENCES modules(id) ON DELETE SET NULL,
    sub_module_id BIGINT REFERENCES sub_modules(id) ON DELETE SET NULL,
    test_case_id BIGINT REFERENCES test_cases(id) ON DELETE SET NULL,
    re_open_count INT DEFAULT 0,
    test_case_required BOOLEAN DEFAULT FALSE,
    attachment VARCHAR(500),
    reported_by VARCHAR(100),
    assigned_to_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    assigned_by_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_defects_project ON defects(project_id);
CREATE INDEX IF NOT EXISTS idx_defects_status ON defects(defect_status_id);
CREATE INDEX IF NOT EXISTS idx_defects_assigned ON defects(assigned_to_id);
CREATE INDEX IF NOT EXISTS idx_defects_release ON defects(release_id);

-- 2. Link release_test_cases to defects
ALTER TABLE release_test_cases
    ADD CONSTRAINT fk_release_tc_defect FOREIGN KEY (linked_defect_id) REFERENCES defects(id) ON DELETE SET NULL;

-- 3. Defect Comments
CREATE TABLE IF NOT EXISTS defect_comments (
    id BIGSERIAL PRIMARY KEY,
    defect_id BIGINT NOT NULL REFERENCES defects(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    comment TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_defect_comments_defect ON defect_comments(defect_id);

-- 4. Defect Histories
CREATE TABLE IF NOT EXISTS defect_histories (
    id BIGSERIAL PRIMARY KEY,
    defect_id BIGINT NOT NULL REFERENCES defects(id) ON DELETE CASCADE,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(100),
    comment VARCHAR(1000),
    changed_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_defect_hist_defect ON defect_histories(defect_id);

-- 5. Defect Status Logs
CREATE TABLE IF NOT EXISTS defect_status_logs (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    release_id BIGINT REFERENCES releases(id) ON DELETE SET NULL,
    defect_id BIGINT NOT NULL REFERENCES defects(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    logged_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_defect_status_logs_project ON defect_status_logs(project_id);

-- 6. KLOC Metrics
CREATE TABLE IF NOT EXISTS kloc_metrics (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    defect_id BIGINT REFERENCES defects(id) ON DELETE SET NULL,
    backend_repo_url VARCHAR(500),
    frontend_repo_url VARCHAR(500),
    github_token VARCHAR(255),
    github_username VARCHAR(100),
    calculated_kloc DOUBLE PRECISION DEFAULT 0.0,
    total_lines_of_code BIGINT DEFAULT 0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
