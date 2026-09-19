-- ==============================================================================
-- Flyway Migration V2: Master Data (Severity, Priority, DefectType, ReleaseType) & Workflow
-- ==============================================================================

-- 1. Severities (with sortable weight)
CREATE TABLE IF NOT EXISTS severities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    weight INT NOT NULL DEFAULT 1,
    description VARCHAR(255),
    color_code VARCHAR(30) DEFAULT '#3B82F6',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 2. Priorities (with sortable weight)
CREATE TABLE IF NOT EXISTS priorities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    weight INT NOT NULL DEFAULT 1,
    description VARCHAR(255),
    color_code VARCHAR(30) DEFAULT '#3B82F6',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 3. Defect Types
CREATE TABLE IF NOT EXISTS defect_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    color_code VARCHAR(30) DEFAULT '#6B7280',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 4. Release Types
CREATE TABLE IF NOT EXISTS release_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    color_code VARCHAR(30) DEFAULT '#6B7280',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 5. Status Types (Configurable status node with UI visual editor coordinates & stage flags)
CREATE TABLE IF NOT EXISTS status_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- 'OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'
    display_color VARCHAR(30) DEFAULT '#3B82F6' NOT NULL,
    is_default_initial BOOLEAN DEFAULT FALSE NOT NULL,
    is_open_stage BOOLEAN DEFAULT FALSE NOT NULL,
    is_resolved_stage BOOLEAN DEFAULT FALSE NOT NULL,
    position_x DOUBLE PRECISION DEFAULT 100.0,
    position_y DOUBLE PRECISION DEFAULT 100.0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 6. Workflow Transitions (Directed edges from_status -> to_status)
CREATE TABLE IF NOT EXISTS workflow_transitions (
    id BIGSERIAL PRIMARY KEY,
    from_status_id BIGINT NOT NULL REFERENCES status_types(id) ON DELETE CASCADE,
    to_status_id BIGINT NOT NULL REFERENCES status_types(id) ON DELETE CASCADE,
    name VARCHAR(100),
    description VARCHAR(255),
    requires_comment BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_workflow_edge UNIQUE (from_status_id, to_status_id)
);

-- ==============================================================================
-- Seed Data: Initial Master Reference Data & Default Workflow Graph
-- ==============================================================================

-- Seed Severities
INSERT INTO severities (name, weight, description, color_code) VALUES
('Low', 1, 'Cosmetic or minor issue with minimal business impact', '#10B981'),
('Medium', 2, 'Non-critical issue with acceptable workaround', '#3B82F6'),
('High', 3, 'Significant feature failure affecting core functionality', '#F59E0B'),
('Critical', 4, 'System crash, data loss, or total blocker', '#EF4444')
ON CONFLICT (name) DO NOTHING;

-- Seed Priorities
INSERT INTO priorities (name, weight, description, color_code) VALUES
('Low', 1, 'Can be resolved in upcoming release cycles', '#10B981'),
('Medium', 2, 'Normal priority fixing order', '#3B82F6'),
('High', 3, 'Must be resolved in current sprint/release', '#F97316'),
('Urgent', 4, 'Requires immediate hotfix intervention', '#DC2626')
ON CONFLICT (name) DO NOTHING;

-- Seed Defect Types
INSERT INTO defect_types (name, description, color_code) VALUES
('Functional', 'Software behavior deviates from specifications', '#3B82F6'),
('UI/UX', 'Layout, styling, typography, or alignment issue', '#8B5CF6'),
('Performance', 'Slow response times or high resource utilization', '#F59E0B'),
('Security', 'Vulnerability or authorization flaw', '#EF4444'),
('Integration', 'Third-party or inter-service communication failure', '#10B981'),
('Usability', 'Difficulty in navigation or counter-intuitive workflow', '#6B7280')
ON CONFLICT (name) DO NOTHING;

-- Seed Release Types
INSERT INTO release_types (name, description, color_code) VALUES
('Major', 'Major release introducing significant features or architecture updates', '#6366F1'),
('Minor', 'Minor release with incremental feature additions', '#3B82F6'),
('Patch', 'Maintenance release containing bug fixes', '#10B981'),
('Hotfix', 'Urgent production fix for critical defects', '#EF4444')
ON CONFLICT (name) DO NOTHING;

-- Seed Status Types with visual node positions and stage semantics
INSERT INTO status_types (name, category, display_color, is_default_initial, is_open_stage, is_resolved_stage, position_x, position_y) VALUES
('New', 'OPEN', '#6366F1', TRUE, TRUE, FALSE, 100.0, 150.0),
('Open', 'OPEN', '#3B82F6', FALSE, TRUE, FALSE, 300.0, 150.0),
('In Progress', 'IN_PROGRESS', '#F59E0B', FALSE, FALSE, FALSE, 500.0, 150.0),
('Fixed', 'RESOLVED', '#10B981', FALSE, FALSE, TRUE, 700.0, 150.0),
('Reopened', 'OPEN', '#EC4899', FALSE, TRUE, FALSE, 500.0, 320.0),
('Closed', 'CLOSED', '#6B7280', FALSE, FALSE, TRUE, 900.0, 150.0),
('Rejected', 'CLOSED', '#EF4444', FALSE, FALSE, TRUE, 300.0, 320.0)
ON CONFLICT (name) DO NOTHING;

-- Seed Default Workflow Transitions (Edges)
INSERT INTO workflow_transitions (from_status_id, to_status_id, name, description)
SELECT s1.id, s2.id, 'Triage and Open', 'Move newly reported defect to open'
FROM status_types s1, status_types s2 WHERE s1.name = 'New' AND s2.name = 'Open'
ON CONFLICT (from_status_id, to_status_id) DO NOTHING;

INSERT INTO workflow_transitions (from_status_id, to_status_id, name, description)
SELECT s1.id, s2.id, 'Reject Defect', 'Defect is invalid, duplicate, or working as designed'
FROM status_types s1, status_types s2 WHERE s1.name = 'New' AND s2.name = 'Rejected'
ON CONFLICT (from_status_id, to_status_id) DO NOTHING;

INSERT INTO workflow_transitions (from_status_id, to_status_id, name, description)
SELECT s1.id, s2.id, 'Start Investigation', 'Developer begins working on fix'
FROM status_types s1, status_types s2 WHERE s1.name = 'Open' AND s2.name = 'In Progress'
ON CONFLICT (from_status_id, to_status_id) DO NOTHING;

INSERT INTO workflow_transitions (from_status_id, to_status_id, name, description)
SELECT s1.id, s2.id, 'Reject from Open', 'Reject defect during open analysis'
FROM status_types s1, status_types s2 WHERE s1.name = 'Open' AND s2.name = 'Rejected'
ON CONFLICT (from_status_id, to_status_id) DO NOTHING;

INSERT INTO workflow_transitions (from_status_id, to_status_id, name, description)
SELECT s1.id, s2.id, 'Mark Fixed', 'Developer deployed fix for verification'
FROM status_types s1, status_types s2 WHERE s1.name = 'In Progress' AND s2.name = 'Fixed'
ON CONFLICT (from_status_id, to_status_id) DO NOTHING;

INSERT INTO workflow_transitions (from_status_id, to_status_id, name, description)
SELECT s1.id, s2.id, 'Verify and Close', 'QA verified fix in test environment'
FROM status_types s1, status_types s2 WHERE s1.name = 'Fixed' AND s2.name = 'Closed'
ON CONFLICT (from_status_id, to_status_id) DO NOTHING;

INSERT INTO workflow_transitions (from_status_id, to_status_id, name, description)
SELECT s1.id, s2.id, 'Reopen Defect', 'QA verification failed; defect reopened'
FROM status_types s1, status_types s2 WHERE s1.name = 'Fixed' AND s2.name = 'Reopened'
ON CONFLICT (from_status_id, to_status_id) DO NOTHING;

INSERT INTO workflow_transitions (from_status_id, to_status_id, name, description)
SELECT s1.id, s2.id, 'Restart Work on Reopened Defect', 'Developer continues fix on reopened defect'
FROM status_types s1, status_types s2 WHERE s1.name = 'Reopened' AND s2.name = 'In Progress'
ON CONFLICT (from_status_id, to_status_id) DO NOTHING;

INSERT INTO workflow_transitions (from_status_id, to_status_id, name, description)
SELECT s1.id, s2.id, 'Reopen Closed Defect', 'Issue recurred after closure'
FROM status_types s1, status_types s2 WHERE s1.name = 'Closed' AND s2.name = 'Reopened'
ON CONFLICT (from_status_id, to_status_id) DO NOTHING;
