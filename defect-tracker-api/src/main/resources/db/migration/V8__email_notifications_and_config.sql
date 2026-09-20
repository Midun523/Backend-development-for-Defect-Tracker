-- ==============================================================================
-- Flyway Migration V8: Email Config, Templates, Preferences, Logs & Privilege Preferences
-- ==============================================================================

-- 1. Email SMTP Configs
CREATE TABLE IF NOT EXISTS email_configs (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    smtp_host VARCHAR(150) NOT NULL,
    smtp_port INT NOT NULL,
    username VARCHAR(150),
    password VARCHAR(255),
    from_email VARCHAR(150) NOT NULL,
    from_name VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    is_default BOOLEAN DEFAULT FALSE NOT NULL,
    use_tls BOOLEAN DEFAULT TRUE NOT NULL,
    use_ssl BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Email Templates
CREATE TABLE IF NOT EXISTS email_templates (
    id BIGSERIAL PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL UNIQUE,
    subject VARCHAR(255) NOT NULL,
    body_content TEXT NOT NULL,
    event_trigger VARCHAR(100),
    variables VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    is_default BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Email Role Preferences
CREATE TABLE IF NOT EXISTS email_role_preferences (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    email_template_id BIGINT NOT NULL REFERENCES email_templates(id) ON DELETE CASCADE,
    status VARCHAR(30) DEFAULT 'active' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Email User Preferences
CREATE TABLE IF NOT EXISTS email_user_preferences (
    id BIGSERIAL PRIMARY KEY,
    emp_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    email_template_id BIGINT NOT NULL REFERENCES email_templates(id) ON DELETE CASCADE,
    status VARCHAR(30) DEFAULT 'active' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Email Logs
CREATE TABLE IF NOT EXISTS email_logs (
    id BIGSERIAL PRIMARY KEY,
    recipient_email VARCHAR(150) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message VARCHAR(1000),
    sent_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Role Notification Settings
CREATE TABLE IF NOT EXISTS role_notification_settings (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    point_key VARCHAR(100) NOT NULL,
    is_email_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    is_system_enabled BOOLEAN DEFAULT TRUE NOT NULL
);

-- 7. User Extra Notification Rules
CREATE TABLE IF NOT EXISTS user_extra_rules (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    rule_key VARCHAR(100) NOT NULL,
    is_enabled BOOLEAN DEFAULT TRUE NOT NULL
);

-- 8. Privilege Templates
CREATE TABLE IF NOT EXISTS privilege_templates (
    id BIGSERIAL PRIMARY KEY,
    privileges_type VARCHAR(100) NOT NULL,
    subject VARCHAR(200),
    status VARCHAR(30) DEFAULT 'active' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. Role Privilege Preferences
CREATE TABLE IF NOT EXISTS role_privilege_preferences (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    privilege_template_id BIGINT NOT NULL REFERENCES privilege_templates(id) ON DELETE CASCADE,
    status VARCHAR(30) DEFAULT 'active' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 10. User Privilege Preferences
CREATE TABLE IF NOT EXISTS user_privilege_preferences (
    id BIGSERIAL PRIMARY KEY,
    emp_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    privilege_template_id BIGINT NOT NULL REFERENCES privilege_templates(id) ON DELETE CASCADE,
    status VARCHAR(30) DEFAULT 'active' NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
-- Seed Data: Initial Email Config & Default Email Templates
-- ==============================================================================

INSERT INTO email_configs (name, smtp_host, smtp_port, username, password, from_email, from_name, is_active, is_default, use_tls, use_ssl)
VALUES ('Default Local SMTP Server', 'smtp.gmail.com', 587, 'notifications@defecttracker.com', 'app-password-here', 'notifications@defecttracker.com', 'Defect Tracker Pro', TRUE, TRUE, TRUE, FALSE);

INSERT INTO email_templates (template_name, subject, body_content, event_trigger, variables, is_active, is_default)
VALUES
(
    'DEFECT_ASSIGNED',
    'New Defect Assigned: [{defectId}] in {projectName}',
    '<p>Hello <b>{name}</b>,</p><p>You have been assigned to defect <b>{defectId}</b> in project <b>{projectName}</b>.</p><p>Please log in to the Defect Tracker to review details and begin investigation.</p>',
    'DEFECT_ASSIGNED',
    'name,defectId,projectName',
    TRUE,
    TRUE
),
(
    'PASSWORD_RESET',
    'Password Reset Request - Defect Tracker Pro',
    '<p>Hello <b>{name}</b>,</p><p>Your password reset code is: <b>{token}</b>.</p><p>If you did not request this, please ignore this email.</p>',
    'PASSWORD_RESET',
    'name,token',
    TRUE,
    TRUE
)
ON CONFLICT (template_name) DO NOTHING;
