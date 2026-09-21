-- Flyway Migration V11: Project Sequences, Integrity, and Default Weights

-- 1. Project Sequences Table for Per-Project DEF, TC, REL sequence numbers
CREATE TABLE IF NOT EXISTS project_sequences (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    sequence_type VARCHAR(20) NOT NULL,
    current_value BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_project_seq UNIQUE (project_id, sequence_type)
);

-- Seed project_sequences for existing projects
INSERT INTO project_sequences (project_id, sequence_type, current_value)
SELECT p.id, 'DEF', COALESCE(MAX(CAST(NULLIF(SUBSTRING(d.defect_id FROM '[0-9]+'), '') AS BIGINT)), 0)
FROM projects p LEFT JOIN defects d ON d.project_id = p.id
GROUP BY p.id
ON CONFLICT (project_id, sequence_type) DO NOTHING;

INSERT INTO project_sequences (project_id, sequence_type, current_value)
SELECT p.id, 'TC', COALESCE(MAX(CAST(NULLIF(SUBSTRING(tc.testcase_no FROM '[0-9]+'), '') AS BIGINT)), 0)
FROM projects p
LEFT JOIN modules m ON m.project_id = p.id
LEFT JOIN sub_modules sm ON sm.module_id = m.id
LEFT JOIN test_cases tc ON tc.sub_module_id = sm.id
GROUP BY p.id
ON CONFLICT (project_id, sequence_type) DO NOTHING;

INSERT INTO project_sequences (project_id, sequence_type, current_value)
SELECT p.id, 'REL', COALESCE(MAX(CAST(NULLIF(SUBSTRING(r.release_no FROM '[0-9]+'), '') AS BIGINT)), 0)
FROM projects p LEFT JOIN releases r ON r.project_id = p.id
GROUP BY p.id
ON CONFLICT (project_id, sequence_type) DO NOTHING;

-- 2. Global PostgreSQL Sequences for project codes and employee codes
CREATE SEQUENCE IF NOT EXISTS project_code_seq;
SELECT setval('project_code_seq', GREATEST(COALESCE((SELECT MAX(CAST(NULLIF(SUBSTRING(project_id FROM '[0-9]+'), '') AS BIGINT)) FROM projects), 0) + 1, 1), false);

CREATE SEQUENCE IF NOT EXISTS employee_code_seq;
SELECT setval('employee_code_seq', GREATEST(COALESCE((SELECT MAX(CAST(NULLIF(SUBSTRING(user_id FROM '[0-9]+'), '') AS BIGINT)) FROM app_users), 0) + 1, 1), false);

-- 3. Add weight column to severities and backfill
ALTER TABLE severities ADD COLUMN IF NOT EXISTS weight INT DEFAULT 1;
UPDATE severities SET weight = 1 WHERE LOWER(name) = 'low';
UPDATE severities SET weight = 2 WHERE LOWER(name) = 'medium';
UPDATE severities SET weight = 3 WHERE LOWER(name) = 'high';
UPDATE severities SET weight = 4 WHERE LOWER(name) = 'critical';

-- 4. Unique constraints for per-project sequences
ALTER TABLE defects DROP CONSTRAINT IF EXISTS defects_defect_id_key;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_defects_project_defect') THEN
        ALTER TABLE defects ADD CONSTRAINT uk_defects_project_defect UNIQUE (project_id, defect_id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_releases_project_release') THEN
        ALTER TABLE releases ADD CONSTRAINT uk_releases_project_release UNIQUE (project_id, release_no);
    END IF;
END $$;
