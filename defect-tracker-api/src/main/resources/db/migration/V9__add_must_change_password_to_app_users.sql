-- ==============================================================================
-- Add must_change_password flag to app_users
-- ==============================================================================

ALTER TABLE app_users ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;
