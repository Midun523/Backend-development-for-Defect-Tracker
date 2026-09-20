-- ==============================================================================
-- Flyway Migration V2: Master Data (Severity, Priority, DefectType, ReleaseType) & Workflow
-- ==============================================================================

-- 1. Severities
CREATE TABLE IF NOT EXISTS severities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(30),
    description VARCHAR(255)
);

-- 2. Priorities
CREATE TABLE IF NOT EXISTS priorities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(30),
    description VARCHAR(255)
);

-- 3. Defect Types
CREATE TABLE IF NOT EXISTS defect_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 4. Release Types
CREATE TABLE IF NOT EXISTS release_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

-- 5. Status Types
CREATE TABLE IF NOT EXISTS status_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    color VARCHAR(30),
    type VARCHAR(50),
    description VARCHAR(255),
    is_default BOOLEAN DEFAULT FALSE NOT NULL,
    order_index INT DEFAULT 0 NOT NULL
);

-- 6. Status Transitions (Directed edges from_status -> to_status)
CREATE TABLE IF NOT EXISTS status_transitions (
    id BIGSERIAL PRIMARY KEY,
    from_status_id BIGINT NOT NULL REFERENCES status_types(id) ON DELETE CASCADE,
    to_status_id BIGINT NOT NULL REFERENCES status_types(id) ON DELETE CASCADE
);

-- 7. Workflow Positions (Canvas node positions)
CREATE TABLE IF NOT EXISTS workflow_positions (
    id BIGSERIAL PRIMARY KEY,
    status_type_id BIGINT NOT NULL REFERENCES status_types(id) ON DELETE CASCADE,
    project_id BIGINT,
    position_x DOUBLE PRECISION NOT NULL,
    position_y DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- Seed Data: Initial Master Reference Data & Default Workflow Graph
-- ==============================================================================

-- Seed Severities
INSERT INTO severities (name, color, description) VALUES
('Low', '#10B981', 'Cosmetic or minor issue with minimal business impact'),
('Medium', '#3B82F6', 'Non-critical issue with acceptable workaround'),
('High', '#F59E0B', 'Significant feature failure affecting core functionality'),
('Critical', '#EF4444', 'System crash, data loss, or total blocker')
ON CONFLICT (name) DO NOTHING;

-- Seed Priorities
INSERT INTO priorities (name, color, description) VALUES
('Low', '#10B981', 'Can be resolved in upcoming release cycles'),
('Medium', '#3B82F6', 'Normal priority fixing order'),
('High', '#F97316', 'Must be resolved in current sprint/release'),
('Urgent', '#DC2626', 'Requires immediate hotfix intervention')
ON CONFLICT (name) DO NOTHING;

-- Seed Defect Types
INSERT INTO defect_types (name, description) VALUES
('Functional', 'Software behavior deviates from specifications'),
('UI/UX', 'Layout, styling, typography, or alignment issue'),
('Performance', 'Slow response times or high resource utilization'),
('Security', 'Vulnerability or authorization flaw'),
('Integration', 'Third-party or inter-service communication failure'),
('Usability', 'Difficulty in navigation or counter-intuitive workflow')
ON CONFLICT (name) DO NOTHING;

-- Seed Release Types
INSERT INTO release_types (name, description) VALUES
('Major', 'Major release introducing significant features or architecture updates'),
('Minor', 'Minor release with incremental feature additions'),
('Patch', 'Maintenance release containing bug fixes'),
('Hotfix', 'Urgent production fix for critical defects')
ON CONFLICT (name) DO NOTHING;

-- Seed Status Types
INSERT INTO status_types (name, color, type, description, is_default, order_index) VALUES
('New', '#6366F1', 'OPEN', 'Newly logged defect', TRUE, 1),
('Open', '#3B82F6', 'OPEN', 'Accepted and assigned defect', FALSE, 2),
('In Progress', '#F59E0B', 'IN_PROGRESS', 'Under active investigation/fixing', FALSE, 3),
('Fixed', '#10B981', 'RESOLVED', 'Fix applied and ready for QA testing', FALSE, 4),
('Reopened', '#EC4899', 'OPEN', 'Defect recurred or fix failed', FALSE, 5),
('Closed', '#6B7280', 'CLOSED', 'Verified and closed defect', FALSE, 6),
('Rejected', '#EF4444', 'CLOSED', 'Not a bug or won''t fix', FALSE, 7)
ON CONFLICT (name) DO NOTHING;

-- Seed Default Workflow Transitions (Edges)
INSERT INTO status_transitions (from_status_id, to_status_id)
SELECT s1.id, s2.id FROM status_types s1, status_types s2 WHERE s1.name = 'New' AND s2.name = 'Open';

INSERT INTO status_transitions (from_status_id, to_status_id)
SELECT s1.id, s2.id FROM status_types s1, status_types s2 WHERE s1.name = 'New' AND s2.name = 'Rejected';

INSERT INTO status_transitions (from_status_id, to_status_id)
SELECT s1.id, s2.id FROM status_types s1, status_types s2 WHERE s1.name = 'Open' AND s2.name = 'In Progress';

INSERT INTO status_transitions (from_status_id, to_status_id)
SELECT s1.id, s2.id FROM status_types s1, status_types s2 WHERE s1.name = 'Open' AND s2.name = 'Rejected';

INSERT INTO status_transitions (from_status_id, to_status_id)
SELECT s1.id, s2.id FROM status_types s1, status_types s2 WHERE s1.name = 'In Progress' AND s2.name = 'Fixed';

INSERT INTO status_transitions (from_status_id, to_status_id)
SELECT s1.id, s2.id FROM status_types s1, status_types s2 WHERE s1.name = 'Fixed' AND s2.name = 'Closed';

INSERT INTO status_transitions (from_status_id, to_status_id)
SELECT s1.id, s2.id FROM status_types s1, status_types s2 WHERE s1.name = 'Fixed' AND s2.name = 'Reopened';

INSERT INTO status_transitions (from_status_id, to_status_id)
SELECT s1.id, s2.id FROM status_types s1, status_types s2 WHERE s1.name = 'Reopened' AND s2.name = 'In Progress';

INSERT INTO status_transitions (from_status_id, to_status_id)
SELECT s1.id, s2.id FROM status_types s1, status_types s2 WHERE s1.name = 'Closed' AND s2.name = 'Reopened';

-- Seed Workflow Positions
INSERT INTO workflow_positions (status_type_id, position_x, position_y)
SELECT s.id, 100.0, 150.0 FROM status_types s WHERE s.name = 'New';
INSERT INTO workflow_positions (status_type_id, position_x, position_y)
SELECT s.id, 300.0, 150.0 FROM status_types s WHERE s.name = 'Open';
INSERT INTO workflow_positions (status_type_id, position_x, position_y)
SELECT s.id, 500.0, 150.0 FROM status_types s WHERE s.name = 'In Progress';
INSERT INTO workflow_positions (status_type_id, position_x, position_y)
SELECT s.id, 700.0, 150.0 FROM status_types s WHERE s.name = 'Fixed';
INSERT INTO workflow_positions (status_type_id, position_x, position_y)
SELECT s.id, 500.0, 320.0 FROM status_types s WHERE s.name = 'Reopened';
INSERT INTO workflow_positions (status_type_id, position_x, position_y)
SELECT s.id, 900.0, 150.0 FROM status_types s WHERE s.name = 'Closed';
INSERT INTO workflow_positions (status_type_id, position_x, position_y)
SELECT s.id, 300.0, 320.0 FROM status_types s WHERE s.name = 'Rejected';
