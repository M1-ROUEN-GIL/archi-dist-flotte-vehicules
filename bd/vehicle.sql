-- =============================================================
-- SERVICE VÉHICULES
-- =============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE vehicle_status AS ENUM (
  'available',
  'in_use',
  'maintenance',
  'out_of_service',
  'reserved'
);

CREATE TYPE fuel_type AS ENUM (
  'gasoline',
  'diesel',
  'electric',
  'hybrid',
  'lpg'
);

CREATE TABLE vehicles (
  id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  plate_number  VARCHAR(20)  UNIQUE NOT NULL,
  brand         VARCHAR(100) NOT NULL,
  model         VARCHAR(100) NOT NULL,
  fuel_type     fuel_type    NOT NULL,
  mileage_km    INTEGER      NOT NULL DEFAULT 0 CHECK (mileage_km >= 0),
  status        vehicle_status NOT NULL DEFAULT 'available',
  vin           VARCHAR(17)  UNIQUE,
  color         VARCHAR(50),
  seats         SMALLINT     DEFAULT 5 CHECK (seats BETWEEN 1 AND 20),
  metadata      JSONB        NOT NULL DEFAULT '{}',
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  deleted_at    TIMESTAMPTZ
);

CREATE TABLE vehicle_assignments (
  id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  vehicle_id   UUID        NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
  driver_id    UUID        NOT NULL,
  started_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  ended_at     TIMESTAMPTZ,
  notes        TEXT,
  created_by   UUID        NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);