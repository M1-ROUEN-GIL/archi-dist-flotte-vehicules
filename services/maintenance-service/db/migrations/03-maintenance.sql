-- =============================================================
-- SERVICE MAINTENANCE
-- Base de données indépendante — pas de FK vers d'autres services
-- =============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Valeurs alignées sur common.graphql (MaintenanceType) et les messages Kafka
-- PREVENTIVE : entretien planifié (vidange, rotation pneus, etc.)
-- CORRECTIVE  : réparation suite à une panne ou un incident
-- INSPECTION  : contrôle réglementaire ou de sécurité
-- RECALL      : rappel constructeur
-- OTHER       : toute intervention ne rentrant pas dans les catégories précédentes
CREATE TYPE maintenance_type AS ENUM (
  'preventive',
  'corrective',
  'inspection',
  'recall',
  'other'
);

CREATE TYPE maintenance_status AS ENUM (
  'scheduled',
  'in_progress',
  'completed',
  'cancelled',
  'overdue'       -- ajouté : intervention non réalisée après la date prévue
);

CREATE TYPE maintenance_priority AS ENUM ('low', 'medium', 'high', 'critical');

-- vehicle_id    : référence logique vers Service Véhicules (pas de FK inter-service)
-- technician_id : référence logique vers Service Conducteurs (pas de FK inter-service)
CREATE TABLE maintenance_records (
  id                 UUID                 PRIMARY KEY DEFAULT uuid_generate_v4(),
  vehicle_id         UUID                 NOT NULL, -- ref logique → Service Véhicules
  type               maintenance_type     NOT NULL,
  status             maintenance_status   NOT NULL DEFAULT 'scheduled',
  priority           maintenance_priority NOT NULL DEFAULT 'medium',
  scheduled_date     DATE                 NOT NULL,
  completed_date     DATE,
  technician_id      UUID,                          -- ref logique → Service Conducteurs
  description        TEXT,
  cost_eur           NUMERIC(10, 2)       CHECK (cost_eur >= 0),
  mileage_at_service INTEGER              CHECK (mileage_at_service >= 0),
  next_service_km    INTEGER              CHECK (next_service_km >= 0),
  parts_used         JSONB                NOT NULL DEFAULT '[]',
  notes              TEXT,
  created_at         TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
  updated_at         TIMESTAMPTZ          NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_completed_date CHECK (
    completed_date IS NULL OR completed_date >= scheduled_date
  )
);

-- ── Index ──────────────────────────────────────────────────────
CREATE INDEX idx_maintenance_vehicle  ON maintenance_records(vehicle_id);
CREATE INDEX idx_maintenance_status   ON maintenance_records(status);
CREATE INDEX idx_maintenance_scheduled ON maintenance_records(scheduled_date);
CREATE INDEX idx_maintenance_technician ON maintenance_records(technician_id)
  WHERE technician_id IS NOT NULL;

-- ── Trigger updated_at ─────────────────────────────────────────
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_maintenance_updated_at
  BEFORE UPDATE ON maintenance_records
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
