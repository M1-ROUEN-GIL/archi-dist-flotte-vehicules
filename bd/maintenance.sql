-- =============================================================
-- SERVICE MAINTENANCE
-- =============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE maintenance_type AS ENUM (
  'oil_change',
  'tire_rotation',
  'inspection',
  'repair',
  'recall',
  'other'
);

CREATE TYPE maintenance_status AS ENUM (
  'scheduled',
  'in_progress',
  'completed',
  'cancelled'
);

CREATE TYPE maintenance_priority AS ENUM ('low', 'medium', 'high', 'critical');

CREATE TABLE maintenance_records (
  id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  vehicle_id          UUID                 NOT NULL,
  type                maintenance_type     NOT NULL,
  status              maintenance_status   NOT NULL DEFAULT 'scheduled',
  priority            maintenance_priority NOT NULL DEFAULT 'medium',
  scheduled_date      DATE                 NOT NULL,
  completed_date      DATE,
  technician_id       UUID,
  description         TEXT,
  cost_eur            NUMERIC(10, 2)       CHECK (cost_eur >= 0),
  mileage_at_service  INTEGER              CHECK (mileage_at_service >= 0),
  next_service_km     INTEGER              CHECK (next_service_km >= 0),
  parts_used          JSONB                NOT NULL DEFAULT '[]',
  notes               TEXT,
  created_at          TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
  updated_at          TIMESTAMPTZ          NOT NULL DEFAULT NOW()
);