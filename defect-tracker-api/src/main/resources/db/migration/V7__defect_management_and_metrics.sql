-- ==============================================================================
-- Flyway Migration V7: Defect Management, Sequences, Immutable History & Comments
-- ==============================================================================

-- 1. Project-Scoped Defect Sequences
CREATE TABLE IF NOT EXISTS project_defect_sequences (
    project_id BIGINT PRIMARY KEY REFERENCES projects(id) ON DELETE CASCADE,
    current_seq BIGINT DEFAULT 0 NOT NULL
);

-- 2. Defects
CREATE TABLE IF NOT EXISTS defects (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    project_seq_num BIGINT NOT NULL,
    defect_code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    steps_to_reproduce TEXT NOT NULL,
    attachment_url VARCHAR(255),
    module_id BIGINT REFERENCES project_modules(id) ON DELETE SET NULL,
    sub_module_id BIGINT REFERENCES submodules(id) ON DELETE SET NULL,
    defect_type_id BIGINT REFERENCES defect_types(id) ON DELETE SET NULL,
    severity_id BIGINT REFERENCES severities(id) ON DELETE SET NULL,
    priority_id BIGINT REFERENCES priorities(id) ON DELETE SET NULL,
    status_id BIGINT NOT NULL REFERENCES status_types(id),
    assigned_to_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    reporter_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    release_id BIGINT REFERENCES releases(id) ON DELETE SET NULL,
    test_case_id BIGINT REFERENCES test_cases(id) ON DELETE SET NULL,
    reopen_counter INT DEFAULT 0 NOT NULL,
    first_assigned_at TIMESTAMP WITHOUT TIME ZONE,
    resolved_at TIMESTAMP WITHOUT TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_project_defect_seq UNIQUE (project_id, project_seq_num)
);

CREATE INDEX IF NOT EXISTS idx_defects_project ON defects(project_id);
CREATE INDEX IF NOT EXISTS idx_defects_status ON defects(status_id);
CREATE INDEX IF NOT EXISTS idx_defects_assigned ON defects(assigned_to_id);
CREATE INDEX IF NOT EXISTS idx_defects_release ON defects(release_id);

-- 3. Immutable Defect History Log (Appended on every status change)
CREATE TABLE IF NOT EXISTS defect_histories (
    id BIGSERIAL PRIMARY KEY,
    defect_id BIGINT NOT NULL REFERENCES defects(id) ON DELETE CASCADE,
    from_status_id BIGINT REFERENCES status_types(id),
    from_status_name VARCHAR(50),
    to_status_id BIGINT NOT NULL REFERENCES status_types(id),
    to_status_name VARCHAR(50) NOT NULL,
    changed_by_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    changed_by_name VARCHAR(150),
    note TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_defect_hist_defect ON defect_histories(defect_id);

-- 4. Threaded Comments
CREATE TABLE IF NOT EXISTS defect_comments (
    id BIGSERIAL PRIMARY KEY,
    defect_id BIGINT NOT NULL REFERENCES defects(id) ON DELETE CASCADE,
    parent_comment_id BIGINT REFERENCES defect_comments(id) ON DELETE CASCADE,
    author_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    author_name VARCHAR(150) NOT NULL,
    comment_text TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_defect_comments_defect ON defect_comments(defect_id);
