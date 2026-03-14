-- =============================================================
-- SERVICE LOCALISATION (TimescaleDB)
-- =============================================================

CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE location_readings (
  time        TIMESTAMPTZ      NOT NULL,
  vehicle_id  UUID             NOT NULL,
  latitude    DOUBLE PRECISION NOT NULL CHECK (latitude  BETWEEN -90  AND 90),
  longitude   DOUBLE PRECISION NOT NULL CHECK (longitude BETWEEN -180 AND 180),
  speed_kmh   REAL             CHECK (speed_kmh >= 0),
  heading_deg REAL             CHECK (heading_deg BETWEEN 0 AND 360),
  accuracy_m  REAL             CHECK (accuracy_m >= 0),
  altitude_m  REAL,
  source      VARCHAR(20)      NOT NULL DEFAULT 'gps'
);

SELECT create_hypertable('location_readings', 'time');