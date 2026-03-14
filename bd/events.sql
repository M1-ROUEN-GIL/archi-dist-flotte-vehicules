-- =============================================================
-- SERVICE ÉVÉNEMENTS (PostgreSQL + Redis)
-- =============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE alert_type AS ENUM (
  'maintenance_due',   -- entretien à planifier
  'vehicle_breakdown', -- panne signalée
  'accident' -- accident
  'license_expiring'   -- permis conducteur bientôt expiré
  'geofence_violation' -- sortie de zone autorisée
  'speed_exceeded' -- dépassement de vitesse
);

CREATE TYPE alert_severity AS ENUM ('info', 'warning', 'high', 'critical');
CREATE TYPE alert_status   AS ENUM ('active', 'acknowledged', 'resolved', 'expired');

CREATE TABLE alerts (
  id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  type            alert_type     NOT NULL,
  severity        alert_severity NOT NULL,
  status          alert_status   NOT NULL DEFAULT 'active',
  vehicle_id      UUID,
  driver_id       UUID,
  message         TEXT           NOT NULL,
  metadata        JSONB          NOT NULL DEFAULT '{}',
  acknowledged_by UUID,
  acknowledged_at TIMESTAMPTZ,
  resolved_at     TIMESTAMPTZ,
  created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
  expires_at      TIMESTAMPTZ
);