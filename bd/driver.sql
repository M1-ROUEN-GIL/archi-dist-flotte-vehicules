-- =============================================================
-- SERVICE CONDUCTEURS
-- Base de données indépendante — FK uniquement en interne
-- =============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE license_category AS ENUM ('A', 'B', 'C', 'D', 'BE', 'CE');

CREATE TYPE driver_status AS ENUM (
  'active',
  'on_leave',
  'suspended',
  'inactive'
);

CREATE TABLE drivers (
  id               UUID          PRIMARY KEY DEFAULT uuid_generate_v4(),
  keycloak_user_id UUID          UNIQUE NOT NULL, -- ref logique → Keycloak
  first_name       VARCHAR(100)  NOT NULL,
  last_name        VARCHAR(100)  NOT NULL,
  email            VARCHAR(255)  UNIQUE NOT NULL,
  phone            VARCHAR(20),
  employee_id      VARCHAR(50)   UNIQUE,
  status           driver_status NOT NULL DEFAULT 'active',
  metadata         JSONB         NOT NULL DEFAULT '{}',
  created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  deleted_at       TIMESTAMPTZ
);

-- FK interne au service : driver_licenses → drivers (même base)
CREATE TABLE driver_licenses (
  id              UUID             PRIMARY KEY DEFAULT uuid_generate_v4(),
  driver_id       UUID             NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
  license_number  VARCHAR(50)      UNIQUE NOT NULL,
  category        license_category NOT NULL,
  issued_date     DATE             NOT NULL,
  expiry_date     DATE             NOT NULL,
  country         CHAR(2)          NOT NULL DEFAULT 'FR',
  -- Calculé automatiquement : valide si la date d'expiration est dans le futur
  is_valid        BOOLEAN GENERATED ALWAYS AS (expiry_date > CURRENT_DATE) STORED,
  created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_license_dates CHECK (expiry_date > issued_date)
);

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
