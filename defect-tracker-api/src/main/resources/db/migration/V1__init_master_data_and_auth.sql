-- ==============================================================================
-- Flyway Migration V1: Master Data, Roles, Permissions, Users, Employees & Auth
-- ==============================================================================

-- 1. Designations
CREATE TABLE IF NOT EXISTS designations (
    id BIGSERIAL PRIMARY KEY,
    designation_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    total_employees INT DEFAULT 0 NOT NULL
);

-- 2. Roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL UNIQUE,
    type VARCHAR(100),
    description VARCHAR(500)
);

-- 3. Permissions
CREATE TABLE IF NOT EXISTS permissions (
    permission_id BIGSERIAL PRIMARY KEY,
    action VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500)
);

-- 4. Role Permissions
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT NOT NULL REFERENCES permissions(permission_id) ON DELETE CASCADE,
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
);

-- 5. Application Users (Credentials & Auth)
CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(30),
    gender VARCHAR(20),
    user_status VARCHAR(30) DEFAULT 'ACTIVE' NOT NULL,
    user_type VARCHAR(50) DEFAULT 'CompanyStaff' NOT NULL,
    designation_id BIGINT REFERENCES designations(id) ON DELETE SET NULL,
    reset_count INT DEFAULT 0,
    user_token VARCHAR(500),
    forgot_password_token VARCHAR(255),
    forgot_password_token_expiry TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. User Roles Join Table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- 7. Employees (Profile & Resource Info)
CREATE TABLE IF NOT EXISTS employees (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE REFERENCES app_users(id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(30),
    gender VARCHAR(20),
    designation_id BIGINT REFERENCES designations(id) ON DELETE SET NULL,
    experience DOUBLE PRECISION DEFAULT 0.0,
    joined_date DATE,
    skills VARCHAR(1000),
    availability INT DEFAULT 100,
    status VARCHAR(30) DEFAULT 'active',
    department VARCHAR(100),
    manager VARCHAR(100),
    start_date DATE,
    end_date DATE,
    whatsapp_number VARCHAR(30),
    reset_count INT DEFAULT 0,
    user_token VARCHAR(500),
    forgot_password_token VARCHAR(255),
    forgot_password_token_expiry TIMESTAMP WITHOUT TIME ZONE,
    address VARCHAR(500),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Password History Records
CREATE TABLE IF NOT EXISTS password_records (
    id BIGSERIAL PRIMARY KEY,
    emp_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    previous_password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- ==============================================================================
-- Seed Data: Initial Designations, Roles, Permissions
-- ==============================================================================

-- Seed Designations
INSERT INTO designations (designation_name, description, total_employees) VALUES
('Project Manager', 'Oversees projects, schedules and resource management', 0),
('Technical Lead', 'Oversees architectural decisions and technical team leaders', 0),
('QA Lead', 'Oversees QA lifecycle, test strategies, and execution', 0),
('Senior QA Engineer', 'Designs comprehensive test suites and executes test plans', 0),
('QA Engineer', 'Performs manual and automated testing', 0),
('Senior Software Engineer', 'Senior developer handling core modules', 0),
('Software Engineer', 'Developer handling feature implementation and bug fixing', 0),
('Associate Software Engineer', 'Junior developer', 0)
ON CONFLICT (designation_name) DO NOTHING;

-- Seed Roles
INSERT INTO roles (role_name, type, description) VALUES
('Super Admin', 'System', 'Full administrative access across all projects and system settings'),
('Project Manager', 'Project', 'Manages projects, modules, and bench allocations'),
('QA Lead', 'QA', 'Leads QA efforts, releases, and test case reviews'),
('QA Engineer', 'QA', 'Executes test cases and logs defects'),
('Developer', 'Engineering', 'Fixes defects and implements modules'),
('Client', 'External', 'Client stakeholder role')
ON CONFLICT (role_name) DO NOTHING;

-- Seed Granular Permissions
INSERT INTO permissions (action, description) VALUES
('DESIGNATION_CREATE', 'Create Designation'),
('DESIGNATION_UPDATE', 'Update Designation'),
('DESIGNATION_READ', 'View Designation'),
('DESIGNATION_DELETE', 'Delete Designation'),
('ROLE_CREATE', 'Create Role'),
('ROLE_UPDATE', 'Update Role'),
('ROLE_READ', 'View Role'),
('ROLE_DELETE', 'Delete Role'),
('PERMISSION_READ', 'View Permissions'),
('ROLE_PERMISSION_ASSIGN', 'Assign Permissions to Role'),
('ROLE_PERMISSION_READ', 'View Role Permissions'),
('DEFECT_TYPE_CREATE', 'Create Defect Type'),
('DEFECT_TYPE_UPDATE', 'Update Defect Type'),
('DEFECT_TYPE_READ', 'View Defect Type'),
('DEFECT_TYPE_DELETE', 'Delete Defect Type'),
('RELEASE_TYPE_CREATE', 'Create Release Type'),
('RELEASE_TYPE_UPDATE', 'Update Release Type'),
('RELEASE_TYPE_READ', 'View Release Type'),
('RELEASE_TYPE_DELETE', 'Delete Release Type'),
('SEVERITY_CREATE', 'Create Severity'),
('SEVERITY_UPDATE', 'Update Severity'),
('SEVERITY_READ', 'View Severity'),
('SEVERITY_DELETE', 'Delete Severity'),
('PRIORITY_CREATE', 'Create Priority'),
('PRIORITY_UPDATE', 'Update Priority'),
('PRIORITY_READ', 'View Priority'),
('PRIORITY_DELETE', 'Delete Priority'),
('STATUS_TYPE_CREATE', 'Create Status Type'),
('STATUS_TYPE_UPDATE', 'Update Status Type'),
('STATUS_TYPE_READ', 'View Status Type'),
('STATUS_TYPE_DELETE', 'Delete Status Type'),
('WORKFLOW_CREATE', 'Create Workflow'),
('WORKFLOW_READ', 'View Workflow'),
('WORKFLOW_UPDATE', 'Update Workflow'),
('EMPLOYEE_CREATE', 'Create Employee'),
('EMPLOYEE_UPDATE', 'Update Employee'),
('EMPLOYEE_READ', 'View Employee'),
('EMPLOYEE_DELETE', 'Delete Employee'),
('BENCH_READ', 'View Bench'),
('BENCH_ALLOCATE', 'Allocate and deallocate employees on projects'),
('PROJECT_CREATE', 'Create Project'),
('PROJECT_UPDATE', 'Update Project'),
('PROJECT_READ', 'View Project'),
('PROJECT_DELETE', 'Delete Project'),
('MODULE_CREATE', 'Create Module'),
('MODULE_UPDATE', 'Update Module'),
('MODULE_READ', 'View Module'),
('MODULE_DELETE', 'Delete Module'),
('TEST_CASE_CREATE', 'Create Test Case'),
('TEST_CASE_UPDATE', 'Update Test Case'),
('TEST_CASE_READ', 'View Test Case'),
('TEST_CASE_DELETE', 'Delete Test Case'),
('RELEASE_CREATE', 'Create Release'),
('RELEASE_UPDATE', 'Update Release'),
('RELEASE_READ', 'View Release'),
('RELEASE_DELETE', 'Delete Release'),
('DEFECT_CREATE', 'Create Defect'),
('DEFECT_UPDATE', 'Update Defect'),
('DEFECT_READ', 'View Defect'),
('DEFECT_DELETE', 'Delete Defect'),
('DEFECT_ASSIGN_DEVELOPER', 'Assign Developer to Defect'),
('DEFECT_STATUS_CHANGE', 'Change Defect Status'),
('DEFECT_COMMENT_CREATE', 'Add Defect Comment'),
('DEFECT_COMMENT_READ', 'View Defect Comments'),
('EMAIL_CONFIG_READ', 'View Email Configurations'),
('EMAIL_CONFIG_CREATE', 'Create Email Configuration'),
('EMAIL_CONFIG_UPDATE', 'Update Email Configuration'),
('EMAIL_CONFIG_DELETE', 'Delete Email Configuration'),
('CONFIG_READ', 'View system configurations'),
('CONFIG_UPDATE', 'Update system configurations'),
('ALL_PERMISSIONS', 'Full System Administrator Access')
ON CONFLICT (action) DO NOTHING;

-- Grant all permissions to Super Admin
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'Super Admin'
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Seed Project Manager Permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'Project Manager'
  AND p.action IN (
      'PROJECT_READ', 'PROJECT_UPDATE',
      'MODULE_READ', 'MODULE_CREATE', 'MODULE_UPDATE', 'MODULE_DELETE',
      'TEST_CASE_READ', 'TEST_CASE_CREATE', 'TEST_CASE_UPDATE',
      'RELEASE_READ', 'RELEASE_CREATE', 'RELEASE_UPDATE',
      'DEFECT_READ', 'DEFECT_CREATE', 'DEFECT_UPDATE',
      'EMPLOYEE_READ',
      'BENCH_READ', 'BENCH_ALLOCATE',
      'WORKFLOW_READ'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Seed QA Lead Permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'QA Lead'
  AND p.action IN (
      'PROJECT_READ',
      'MODULE_READ',
      'TEST_CASE_READ', 'TEST_CASE_CREATE', 'TEST_CASE_UPDATE', 'TEST_CASE_DELETE',
      'RELEASE_READ', 'RELEASE_CREATE', 'RELEASE_UPDATE',
      'DEFECT_READ', 'DEFECT_CREATE', 'DEFECT_UPDATE',
      'EMPLOYEE_READ',
      'BENCH_READ',
      'WORKFLOW_READ'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Seed QA Engineer Permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'QA Engineer'
  AND p.action IN (
      'PROJECT_READ',
      'MODULE_READ',
      'TEST_CASE_READ', 'TEST_CASE_CREATE', 'TEST_CASE_UPDATE',
      'RELEASE_READ', 'RELEASE_UPDATE',
      'DEFECT_READ', 'DEFECT_CREATE', 'DEFECT_UPDATE',
      'WORKFLOW_READ'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Seed Developer Permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.permission_id
FROM roles r, permissions p
WHERE r.role_name = 'Developer'
  AND p.action IN (
      'PROJECT_READ',
      'MODULE_READ',
      'TEST_CASE_READ',
      'RELEASE_READ',
      'DEFECT_READ', 'DEFECT_UPDATE',
      'WORKFLOW_READ'
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;
