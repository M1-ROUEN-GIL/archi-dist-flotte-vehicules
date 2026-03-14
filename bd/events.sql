-- =============================================================
-- SERVICE ÉVÉNEMENTS
-- Base de données indépendante — pas de FK vers d'autres services
-- =============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE alert_type AS ENUM (
  'maintenance_due',    -- entretien à planifier
  'vehicle_breakdown',  -- panne signalée
  'accident',           -- accident
  'license_expiring',   -- permis conducteur bientôt expiré
  'geofence_violation', -- sortie de zone autorisée
  'speed_exceeded'      -- dépassement de vitesse
);

CREATE TYPE alert_severity AS ENUM ('info', 'warning', 'high', 'critical');

CREATE TYPE alert_status AS ENUM (
  'active',
  'acknowledged',
  'resolved',
  'expired'
);

-- vehicle_id      : référence logique vers Service Véhicules (pas de FK inter-service)
-- driver_id       : référence logique vers Service Conducteurs (pas de FK inter-service)
-- acknowledged_by : référence logique vers Keycloak user (pas de FK inter-service)
CREATE TABLE alerts (
  id              UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
  type            alert_type     NOT NULL,
  severity        alert_severity NOT NULL,
  status          alert_status   NOT NULL DEFAULT 'active',
  vehicle_id      UUID,          -- ref logique → Service Véhicules
  driver_id       UUID,          -- ref logique → Service Conducteurs
  message         TEXT           NOT NULL,
  metadata        JSONB          NOT NULL DEFAULT '{}',
  acknowledged_by UUID,          -- ref logique → Keycloak user
  acknowledged_at TIMESTAMPTZ,
  resolved_at     TIMESTAMPTZ,
  expires_at      TIMESTAMPTZ,
  created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_acknowledged CHECK (
    (acknowledged_by IS NULL AND acknowledged_at IS NULL) OR
    (acknowledged_by IS NOT NULL AND acknowledged_at IS NOT NULL)
  )
);

-- ── Index ──────────────────────────────────────────────────────
CREATE INDEX idx_alerts_status     ON alerts(status);
CREATE INDEX idx_alerts_severity   ON alerts(severity);
CREATE INDEX idx_alerts_vehicle    ON alerts(vehicle_id) WHERE vehicle_id IS NOT NULL;
CREATE INDEX idx_alerts_driver     ON alerts(driver_id)  WHERE driver_id IS NOT NULL;
CREATE INDEX idx_alerts_created_at ON alerts(created_at DESC);
CREATE INDEX idx_alerts_active     ON alerts(status) WHERE status = 'active';

-- ── Trigger updated_at ─────────────────────────────────────────
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_alerts_updated_at
  BEFORE UPDATE ON alerts
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
