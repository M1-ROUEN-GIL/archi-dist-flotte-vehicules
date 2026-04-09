-- services/location-service/db/migrations/01-location.sql

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS timescaledb;

CREATE TABLE IF NOT EXISTS location_readings (
                                                 id           UUID             DEFAULT uuid_generate_v4(),
    vehicle_id   UUID             NOT NULL,
    driver_id    UUID,
    latitude     DOUBLE PRECISION NOT NULL,
    longitude    DOUBLE PRECISION NOT NULL,
    speed_kmh    REAL             NOT NULL DEFAULT 0,
    heading_deg  REAL             NOT NULL DEFAULT 0,
    accuracy_m   REAL             NOT NULL DEFAULT 0,
    altitude_m   REAL,
    source       VARCHAR(20)      NOT NULL DEFAULT 'GPS_DEVICE',
    time         TIMESTAMPTZ      NOT NULL DEFAULT NOW()
    );

-- C'est cette ligne que TypeORM ne peut pas faire
SELECT create_hypertable('location_readings', 'time', if_not_exists => TRUE);

CREATE INDEX IF NOT EXISTS idx_location_vehicle_time
    ON location_readings (vehicle_id, time DESC);

-- Compression après 7 jours (optionnel pour le dev)
-- SELECT add_compression_policy('location_readings', INTERVAL '7 days');

-- Rétention 90 jours (optionnel pour le dev)
-- SELECT add_retention_policy('location_readings', INTERVAL '90 days');