BEGIN;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS permissions_initialized boolean NOT NULL DEFAULT false;
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id bigint NOT NULL REFERENCES roles(id),
    permission varchar(64) NOT NULL,
    PRIMARY KEY (role_id, permission)
);
COMMIT;
