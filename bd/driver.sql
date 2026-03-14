-- =============================================================
-- SERVICE CONDUCTEURS
-- =============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TYPE license_category AS ENUM ('A', 'B', 'C', 'D');
CREATE TYPE driver_status     AS ENUM ('active', 'inactive');

CREATE TABLE drivers (
  id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  keycloak_user_id UUID         UNIQUE NOT NULL,
  first_name       VARCHAR(100) NOT NULL,
  last_name        VARCHAR(100) NOT NULL,
  email            VARCHAR(255) UNIQUE NOT NULL,
  phone            VARCHAR(20),
  employee_id      VARCHAR(50)  UNIQUE,
  status           driver_status NOT NULL DEFAULT 'active',
  metadata         JSONB        NOT NULL DEFAULT '{}',
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
  deleted_at       TIMESTAMPTZ
);

CREATE TABLE driver_licenses (
  id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  driver_id       UUID         NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
  license_number  VARCHAR(50)  UNIQUE NOT NULL,
  category        license_category NOT NULL,
  issued_date     DATE         NOT NULL,
  expiry_date     DATE         NOT NULL,
  country         CHAR(2)      NOT NULL DEFAULT 'FR',
  is_valid        BOOLEAN GENERATED ALWAYS AS (expiry_date > CURRENT_DATE) STORED,
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);