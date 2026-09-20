-- ==============================================================================
-- Flyway Migration V3: Client Details, Projects, Modules, SubModules, and Allocations
-- ==============================================================================

-- 1. Client Details
CREATE TABLE IF NOT EXISTS client_details (
    id BIGSERIAL PRIMARY KEY,
    client_name VARCHAR(150) NOT NULL,
    phone_number VARCHAR(30),
    email VARCHAR(150),
    country VARCHAR(100),
    state VARCHAR(100),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Projects
CREATE TABLE IF NOT EXISTS projects (
    id BIGSERIAL PRIMARY KEY,
    project_id VARCHAR(50) UNIQUE,
    name VARCHAR(150) NOT NULL,
    prefix VARCHAR(20),
    project_type VARCHAR(50),
    status VARCHAR(30) DEFAULT 'ACTIVE' NOT NULL,
    start_date DATE,
    end_date DATE,
    manager_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    client_id BIGINT REFERENCES client_details(id) ON DELETE SET NULL,
    client_name VARCHAR(150),
    client_country VARCHAR(100),
    client_state VARCHAR(100),
    client_email VARCHAR(150),
    client_phone VARCHAR(30),
    address VARCHAR(500),
    description VARCHAR(2000),
    progress DOUBLE PRECISION DEFAULT 0.0,
    kloc DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Modules
CREATE TABLE IF NOT EXISTS modules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    project_id BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    leader_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. SubModules
CREATE TABLE IF NOT EXISTS sub_modules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    module_id BIGINT NOT NULL REFERENCES modules(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Module Leader Allocations
CREATE TABLE IF NOT EXISTS module_leader_allocations (
    id BIGSERIAL PRIMARY KEY,
    module_id BIGINT NOT NULL REFERENCES modules(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    allocated_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. SubModule Developer Allocations
CREATE TABLE IF NOT EXISTS sub_module_dev_allocations (
    id BIGSERIAL PRIMARY KEY,
    sub_module_id BIGINT NOT NULL REFERENCES sub_modules(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    assigned_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
