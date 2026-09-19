-- ==============================================================================
-- Flyway Migration V3: Clients, Projects, Modules, SubModules, and Developer Assignments
-- ==============================================================================

-- 1. Clients (one per project)
CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    email VARCHAR(150),
    phone VARCHAR(50),
    address VARCHAR(255),
    contact_person VARCHAR(100),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 2. Projects
CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE' NOT NULL, -- ACTIVE, COMPLETED, ON_HOLD
    start_date DATE,
    end_date DATE,
    client_id BIGINT UNIQUE REFERENCES clients(id) ON DELETE SET NULL,
    project_manager_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    kloc NUMERIC(10, 2) DEFAULT 0.00 NOT NULL, -- Kilo lines of code
    source_control_url VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 3. Project Modules
CREATE TABLE IF NOT EXISTS project_modules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    module_leader_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_project_module_name UNIQUE (project_id, name)
);

-- 4. SubModules
CREATE TABLE IF NOT EXISTS submodules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(255),
    module_id BIGINT NOT NULL REFERENCES project_modules(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_module_submodule_name UNIQUE (module_id, name)
);

-- 5. SubModule Developer Assignments (one or more developers per submodule)
CREATE TABLE IF NOT EXISTS submodule_developer_assignments (
    id BIGSERIAL PRIMARY KEY,
    submodule_id BIGINT NOT NULL REFERENCES submodules(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_submodule_dev UNIQUE (submodule_id, employee_id)
);

-- ==============================================================================
-- Seed Sample Client & Project
-- ==============================================================================

INSERT INTO clients (name, email, phone, contact_person) VALUES
('Acme Corporation', 'client@acmecorp.com', '+1-555-0199', 'Jane Doe')
ON CONFLICT (name) DO NOTHING;

INSERT INTO projects (name, description, status, start_date, client_id, project_manager_id, kloc, is_active)
SELECT 
    'Defect Tracker Platform',
    'Enterprise Defect and QA Management System',
    'ACTIVE',
    CURRENT_DATE,
    c.id,
    e.id,
    150.00,
    TRUE
FROM clients c, employees e
WHERE c.name = 'Acme Corporation' AND e.email = 'admin@defecttracker.com'
ON CONFLICT (name) DO NOTHING;
