-- ==============================================================================
-- Flyway Migration V1: Master Data, Roles, Permissions, Employees & Auth
-- ==============================================================================

-- 1. Designations
CREATE TABLE IF NOT EXISTS designations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_project_manager_eligible BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 2. Roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_admin BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 3. Permissions
CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    feature_area VARCHAR(50) NOT NULL,
    action VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 4. Role Permissions
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
);

-- 5. Employees
CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    designation_id BIGINT REFERENCES designations(id) ON DELETE SET NULL,
    role_id BIGINT NOT NULL REFERENCES roles(id),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    initial_capacity NUMERIC(5, 2) DEFAULT 100.00 NOT NULL,
    current_capacity NUMERIC(5, 2) DEFAULT 100.00 NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 6. Employee Permission Overrides (GRANT or REVOKE)
CREATE TABLE IF NOT EXISTS employee_permission_overrides (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    override_type VARCHAR(20) NOT NULL, -- 'GRANT' or 'REVOKE'
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_emp_permission UNIQUE (employee_id, permission_id)
);

-- 7. Password Reset Tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_used BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 8. Refresh Tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ==============================================================================
-- Seed Data: Initial Designations, Roles, Permissions & Admin Account
-- ==============================================================================

-- Seed Designations
INSERT INTO designations (name, description, is_project_manager_eligible) VALUES
('Project Manager', 'Oversees projects, schedules and resource management', TRUE),
('Technical Lead', 'Oversees architectural decisions and technical team leaders', TRUE),
('QA Lead', 'Oversees QA lifecycle, test strategies, and execution', FALSE),
('Senior QA Engineer', 'Designs comprehensive test suites and executes test plans', FALSE),
('QA Engineer', 'Performs manual and automated testing', FALSE),
('Senior Software Engineer', 'Senior developer handling core modules', FALSE),
('Software Engineer', 'Developer handling feature implementation and bug fixing', FALSE),
('Associate Software Engineer', 'Junior developer', FALSE)
ON CONFLICT (name) DO NOTHING;

-- Seed Roles
INSERT INTO roles (name, description, is_admin) VALUES
('ROLE_ADMIN', 'Full administrative access across all projects and system settings', TRUE),
('ROLE_PROJECT_MANAGER', 'Manages projects, modules, and bench allocations', FALSE),
('ROLE_QA_LEAD', 'Leads QA efforts, releases, and test case reviews', FALSE),
('ROLE_QA_ENGINEER', 'Executes test cases and logs defects', FALSE),
('ROLE_DEVELOPER', 'Fixes defects and implements modules', FALSE)
ON CONFLICT (name) DO NOTHING;

-- Seed Granular Permissions
INSERT INTO permissions (code, feature_area, action, description) VALUES
-- Project
('PROJECT:READ', 'PROJECT', 'READ', 'View project details'),
('PROJECT:CREATE', 'PROJECT', 'CREATE', 'Create projects'),
('PROJECT:UPDATE', 'PROJECT', 'UPDATE', 'Update projects'),
('PROJECT:DELETE', 'PROJECT', 'DELETE', 'Delete projects'),

-- Module
('MODULE:READ', 'MODULE', 'READ', 'View modules and submodules'),
('MODULE:CREATE', 'MODULE', 'CREATE', 'Create modules and submodules'),
('MODULE:UPDATE', 'MODULE', 'UPDATE', 'Update modules and assign developers'),
('MODULE:DELETE', 'MODULE', 'DELETE', 'Delete modules and submodules'),

-- Test Case
('TEST_CASE:READ', 'TEST_CASE', 'READ', 'View test cases'),
('TEST_CASE:CREATE', 'TEST_CASE', 'CREATE', 'Create test cases'),
('TEST_CASE:UPDATE', 'TEST_CASE', 'UPDATE', 'Update test cases'),
('TEST_CASE:DELETE', 'TEST_CASE', 'DELETE', 'Delete test cases'),

-- Release
('RELEASE:READ', 'RELEASE', 'READ', 'View releases and test executions'),
('RELEASE:CREATE', 'RELEASE', 'CREATE', 'Create releases'),
('RELEASE:UPDATE', 'RELEASE', 'UPDATE', 'Update releases and execution results'),
('RELEASE:DELETE', 'RELEASE', 'DELETE', 'Delete releases'),

-- Defect
('DEFECT:READ', 'DEFECT', 'READ', 'View defects'),
('DEFECT:CREATE', 'DEFECT', 'CREATE', 'Create defects'),
('DEFECT:UPDATE', 'DEFECT', 'UPDATE', 'Update defect status, details, and assignments'),
('DEFECT:DELETE', 'DEFECT', 'DELETE', 'Delete defects'),

-- Employee
('EMPLOYEE:READ', 'EMPLOYEE', 'READ', 'View employee profiles'),
('EMPLOYEE:CREATE', 'EMPLOYEE', 'CREATE', 'Register new employees'),
('EMPLOYEE:UPDATE', 'EMPLOYEE', 'UPDATE', 'Update employee details and permissions'),
('EMPLOYEE:DELETE', 'EMPLOYEE', 'DELETE', 'Deactivate or delete employees'),

-- Bench / Allocation
('BENCH:READ', 'BENCH', 'READ', 'View bench availability and allocation history'),
('BENCH:ALLOCATE', 'BENCH', 'ALLOCATE', 'Allocate and deallocate employees on projects'),

-- Workflow
('WORKFLOW:READ', 'WORKFLOW', 'READ', 'View status workflow transitions'),
('WORKFLOW:UPDATE', 'WORKFLOW', 'UPDATE', 'Configure status transition graph'),

-- Master Data & Config
('CONFIG:READ', 'CONFIG', 'READ', 'View system configurations'),
('CONFIG:UPDATE', 'CONFIG', 'UPDATE', 'Update system configurations and email templates')
ON CONFLICT (code) DO NOTHING;

-- Grant all permissions to ROLE_ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Seed Project Manager Permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_PROJECT_MANAGER'
  AND p.code IN (
      'PROJECT:READ', 'PROJECT:UPDATE',
      'MODULE:READ', 'MODULE:CREATE', 'MODULE:UPDATE', 'MODULE:DELETE',
      'TEST_CASE:READ', 'TEST_CASE:CREATE', 'TEST_CASE:UPDATE',
      'RELEASE:READ', 'RELEASE:CREATE', 'RELEASE:UPDATE',
      'DEFECT:READ', 'DEFECT:CREATE', 'DEFECT:UPDATE',
      'EMPLOYEE:READ',
      'BENCH:READ', 'BENCH:ALLOCATE',
      'WORKFLOW:READ'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Seed QA Lead Permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_QA_LEAD'
  AND p.code IN (
      'PROJECT:READ',
      'MODULE:READ',
      'TEST_CASE:READ', 'TEST_CASE:CREATE', 'TEST_CASE:UPDATE', 'TEST_CASE:DELETE',
      'RELEASE:READ', 'RELEASE:CREATE', 'RELEASE:UPDATE',
      'DEFECT:READ', 'DEFECT:CREATE', 'DEFECT:UPDATE',
      'EMPLOYEE:READ',
      'BENCH:READ',
      'WORKFLOW:READ'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Seed QA Engineer Permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_QA_ENGINEER'
  AND p.code IN (
      'PROJECT:READ',
      'MODULE:READ',
      'TEST_CASE:READ', 'TEST_CASE:CREATE', 'TEST_CASE:UPDATE',
      'RELEASE:READ', 'RELEASE:UPDATE',
      'DEFECT:READ', 'DEFECT:CREATE', 'DEFECT:UPDATE',
      'WORKFLOW:READ'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Seed Developer Permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'ROLE_DEVELOPER'
  AND p.code IN (
      'PROJECT:READ',
      'MODULE:READ',
      'TEST_CASE:READ',
      'RELEASE:READ',
      'DEFECT:READ', 'DEFECT:UPDATE',
      'WORKFLOW:READ'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Seed Default Super Admin Employee (password: admin123, BCrypt hashed)
INSERT INTO employees (
    employee_id, first_name, last_name, email, password, phone,
    designation_id, role_id, is_active, initial_capacity, current_capacity
)
SELECT 
    'US0001', 'System', 'Administrator', 'admin@defecttracker.com',
    '$2a$10$7Q9j7mQ2BvZ93e6p6p1P5ehh8y1m7/s0jD8tK8Jj8Cv0xLdYt5e3y', '0000000000',
    d.id, r.id, TRUE, 100.00, 100.00
FROM designations d, roles r
WHERE d.name = 'Project Manager' AND r.name = 'ROLE_ADMIN'
ON CONFLICT (email) DO NOTHING;
