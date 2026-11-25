BEGIN;
-- Export role_permissions first if custom grants must be retained.
DROP TABLE IF EXISTS role_permissions;
ALTER TABLE roles DROP COLUMN IF EXISTS permissions_initialized;
COMMIT;
