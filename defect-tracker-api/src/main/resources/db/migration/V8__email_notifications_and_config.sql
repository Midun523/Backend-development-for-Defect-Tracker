-- ==============================================================================
-- Flyway Migration V8: Email Notifications, Config, Templates, Routing & Logs
-- ==============================================================================

-- 1. Configurable SMTP Settings
CREATE TABLE IF NOT EXISTS email_configurations (
    id BIGSERIAL PRIMARY KEY,
    host VARCHAR(150) NOT NULL DEFAULT 'smtp.gmail.com',
    port INT NOT NULL DEFAULT 587,
    username VARCHAR(150),
    password VARCHAR(255),
    from_email VARCHAR(150) NOT NULL DEFAULT 'no-reply@defecttracker.com',
    from_name VARCHAR(150) DEFAULT 'DefectTracker Notification System',
    auth_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    starttls_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    is_enabled BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 2. Editable Email Templates with dynamic variables
CREATE TABLE IF NOT EXISTS email_templates (
    id BIGSERIAL PRIMARY KEY,
    notification_type VARCHAR(100) NOT NULL UNIQUE,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- 3. Role-to-Notification Type Matrix
CREATE TABLE IF NOT EXISTS email_role_recipients (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    notification_type VARCHAR(100) NOT NULL,
    is_enabled BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_role_notif UNIQUE (role_id, notification_type)
);

-- 4. Specific Employee Overrides (Opt-in or Opt-out)
CREATE TABLE IF NOT EXISTS email_employee_overrides (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    notification_type VARCHAR(100) NOT NULL,
    is_enabled BOOLEAN NOT NULL, -- TRUE: force notify, FALSE: mute notifications
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    CONSTRAINT uk_emp_notif UNIQUE (employee_id, notification_type)
);

-- 5. Sent Email Audit Log
CREATE TABLE IF NOT EXISTS email_logs (
    id BIGSERIAL PRIMARY KEY,
    recipient VARCHAR(150) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT,
    notification_type VARCHAR(100),
    status VARCHAR(30) NOT NULL, -- 'SENT', 'FAILED'
    error_message TEXT,
    sent_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_email_logs_type ON email_logs(notification_type);
CREATE INDEX IF NOT EXISTS idx_email_logs_sent ON email_logs(sent_at);

-- ==============================================================================
-- Seed Default Email Settings & Templates
-- ==============================================================================

INSERT INTO email_configurations (host, port, username, password, from_email, from_name, is_enabled)
VALUES ('smtp.mailtrap.io', 2525, 'smtp_user', 'smtp_pass', 'no-reply@defecttracker.com', 'DefectTracker Alerts', FALSE)
ON CONFLICT DO NOTHING;

INSERT INTO email_templates (notification_type, subject, body, description) VALUES
(
    'DEFECT_ASSIGNED',
    '[DefectTracker] Defect Assigned: ${defectCode} - ${defectTitle}',
    'Hello ${recipientName},\n\nYou have been assigned defect ${defectCode}: "${defectTitle}" in project "${projectName}".\n\nSeverity: ${severity}\nPriority: ${priority}\n\nPlease review and investigate.',
    'Triggered when a defect is assigned or reassigned to an employee'
),
(
    'DEFECT_STATUS_CHANGED',
    '[DefectTracker] Status Updated: ${defectCode} is now ${statusName}',
    'Hello ${recipientName},\n\nDefect ${defectCode} ("${defectTitle}") status has changed to "${statusName}".\n\nUpdated by: ${changedBy}\nNote: ${note}',
    'Triggered when a defect transition occurs'
),
(
    'PASSWORD_RESET',
    '[DefectTracker] Password Reset Request',
    'Hello ${recipientName},\n\nA password reset was requested for your account. Please use the following token or link to reset your password:\n\nToken: ${token}\n\nThis token will expire in 1 hour. If you did not request this, please ignore this email.',
    'Triggered on forgot password request'
),
(
    'RELEASE_DEPLOYED',
    '[DefectTracker] New Release: ${version} for ${projectName}',
    'Hello ${recipientName},\n\nA new release ${version} ("${releaseName}") has been created for project "${projectName}".\n\nDate: ${releaseDate}\nTotal Test Cases: ${totalTestCases}',
    'Triggered when a new release is prepared'
)
ON CONFLICT (notification_type) DO NOTHING;
