-- =============================================================
-- SERVICE LOCALISATION (TimescaleDB)
-- Base de données indépendante — pas de FK vers d'autres services
-- =============================================================

CREATE EXTENSION IF NOT EXISTS timescaledb;

-- vehicle_id : référence logique vers Service Véhicules (pas de FK inter-service)
CREATE TABLE location_readings (
  time        TIMESTAMPTZ      NOT NULL,
  vehicle_id  UUID             NOT NULL, -- ref logique → Service Véhicules
  latitude    DOUBLE PRECISION NOT NULL CHECK (latitude  BETWEEN -90  AND 90),
  longitude   DOUBLE PRECISION NOT NULL CHECK (longitude BETWEEN -180 AND 180),
  speed_kmh   REAL             CHECK (speed_kmh  >= 0),
  heading_deg REAL             CHECK (heading_deg BETWEEN 0 AND 360),
  accuracy_m  REAL             CHECK (accuracy_m  >= 0),
  altitude_m  REAL,
  source      VARCHAR(20)      NOT NULL DEFAULT 'gps',
  -- Clé primaire composée obligatoire pour TimescaleDB (compression + partitionnement)
  PRIMARY KEY (time, vehicle_id)
);

-- Convertir en hypertable partitionné par time (chunk de 1 jour)
SELECT create_hypertable('location_readings', 'time', chunk_time_interval => INTERVAL '1 day');

-- ── Index ──────────────────────────────────────────────────────
-- Index sur vehicle_id pour les requêtes "dernière position d'un véhicule"
CREATE INDEX idx_location_vehicle_time ON location_readings(vehicle_id, time DESC);

-- ── Compression (données > 7 jours compressées automatiquement) ─
ALTER TABLE location_readings SET (
  timescaledb.compress,
  timescaledb.compress_segmentby = 'vehicle_id',
  timescaledb.compress_orderby   = 'time DESC'
);

SELECT add_compression_policy('location_readings', INTERVAL '7 days');

-- ── Rétention (suppression automatique des données > 90 jours) ──
SELECT add_retention_policy('location_readings', INTERVAL '90 days');
