-- =============================================================
-- SERVICE CONDUCTEURS (Driver Service)
-- =============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE license_category AS ENUM ('A', 'B', 'C', 'D', 'BE', 'CE');

CREATE TYPE driver_status AS ENUM (
  'active',
  'on_leave',
  'suspended',
  'inactive'
);

-- 1. Table principale des conducteurs
CREATE TABLE drivers (
                         id               UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
                         keycloak_user_id UUID          UNIQUE NOT NULL,
                         first_name       VARCHAR(100)  NOT NULL,
                         last_name        VARCHAR(100)  NOT NULL,
                         email            VARCHAR(255)  UNIQUE NOT NULL,
                         phone            VARCHAR(20),
                         employee_id      VARCHAR(50)   UNIQUE,
                         status           driver_status NOT NULL DEFAULT 'active',
                         created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                         updated_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                         deleted_at       TIMESTAMPTZ
);

-- 2. Table des permis (Données physiques)
CREATE TABLE driver_licenses (
                                 id              UUID             PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 driver_id       UUID             NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
                                 license_number  VARCHAR(50)      UNIQUE NOT NULL,
                                 category        license_category NOT NULL,
                                 issued_date     DATE             NOT NULL,
                                 expiry_date     DATE             NOT NULL,
                                 country         CHAR(2)          NOT NULL DEFAULT 'FR',
                                 created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
                                 CONSTRAINT chk_license_dates CHECK (expiry_date > issued_date)
);

-- 3. Vue dynamique pour inclure la validité en temps réel
CREATE OR REPLACE VIEW vw_driver_licenses AS
SELECT
    id,
    driver_id,
    license_number,
    category,
    issued_date,
    expiry_date,
    country,
    created_at,
    -- Le calcul se fait à la volée à chaque SELECT !
    (expiry_date > CURRENT_DATE) AS is_valid
FROM driver_licenses;

-- ── Index ──────────────────────────────────────────────────────
CREATE INDEX idx_drivers_status      ON drivers(status);
CREATE INDEX idx_drivers_deleted_at  ON drivers(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_drivers_keycloak    ON drivers(keycloak_user_id);
CREATE INDEX idx_licenses_driver     ON driver_licenses(driver_id);
CREATE INDEX idx_licenses_expiry     ON driver_licenses(expiry_date);

-- ── Trigger updated_at ─────────────────────────────────────────
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_drivers_updated_at
    BEFORE UPDATE ON drivers
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();