-- =============================================================
-- SERVICE VÉHICULES
-- Base de données indépendante — pas de FK vers d'autres services
-- =============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE vehicle_status AS ENUM (
  'available',
  'on_delivery',
  'in_maintenance',
  'out_of_service'
);

CREATE TYPE fuel_type AS ENUM (
  'gasoline',
  'diesel',
  'electric',
  'hybrid'
);

CREATE TABLE vehicles (
  id            UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
  plate_number  VARCHAR(20)    UNIQUE NOT NULL,
  brand         VARCHAR(100)   NOT NULL,
  model         VARCHAR(100)   NOT NULL,
  fuel_type     fuel_type      NOT NULL,
  mileage_km    INTEGER        NOT NULL DEFAULT 0 CHECK (mileage_km >= 0),
  status        vehicle_status NOT NULL DEFAULT 'available',
  vin           VARCHAR(17)    UNIQUE,
  payload_capacity_kg INTEGER        NOT NULL DEFAULT 1000 CHECK (payload_capacity_kg > 0),
  cargo_volume_m3     NUMERIC(5,2)   NOT NULL DEFAULT 10.0 CHECK (cargo_volume_m3 > 0),
  metadata      JSONB          NOT NULL DEFAULT '{}',
  created_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
  deleted_at    TIMESTAMPTZ
);

-- driver_id  : référence logique vers Service Conducteurs (pas de FK inter-service)
-- created_by : UUID de l'utilisateur Keycloak ayant créé l'assignation
CREATE TABLE vehicle_assignments (
  id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
  vehicle_id  UUID        NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
  driver_id   UUID        NOT NULL, -- ref logique → Service Conducteurs
  started_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  ended_at    TIMESTAMPTZ,
  notes       TEXT,
  created_by  UUID        NOT NULL, -- ref logique → Keycloak user
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_assignment_dates CHECK (ended_at IS NULL OR ended_at > started_at)
);

-- ── Index ──────────────────────────────────────────────────────
CREATE INDEX idx_vehicles_status     ON vehicles(status);
CREATE INDEX idx_vehicles_deleted_at ON vehicles(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX idx_assignments_vehicle ON vehicle_assignments(vehicle_id);
CREATE INDEX idx_assignments_driver  ON vehicle_assignments(driver_id);
CREATE INDEX idx_assignments_active  ON vehicle_assignments(vehicle_id) WHERE ended_at IS NULL;

-- ── Trigger updated_at ─────────────────────────────────────────
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_vehicles_updated_at
  BEFORE UPDATE ON vehicles
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
