-- =============================================================
-- SEED : Données de test pour le service Conducteurs
-- =============================================================

-- 1. Insertion des conducteurs
INSERT INTO drivers (id, keycloak_user_id, first_name, last_name, email, phone, employee_id, status)
VALUES
    -- Chauffeur 1 : Jean Dupont (Actif)
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', 'Jean', 'Dupont', 'jean.dupont@dhl.com', '+33611223344', 'DHL-001', 'ACTIVE'),

    -- Chauffeur 2 : Marie Martin (En congé)
    ('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', '550e8400-e29b-41d4-a716-446655440000', 'Marie', 'Martin', 'marie.martin@dhl.com', '+33655667788', 'DHL-002', 'ON_LEAVE');

-- 2. Insertion des permis
INSERT INTO driver_licenses (driver_id, license_number, category, issued_date, expiry_date, country)
VALUES
    -- Jean a le permis B et le permis C (Poids lourd)
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'PERMIS-123456', 'B', '2015-06-15', '2030-06-15', 'FR'),
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', 'PERMIS-123456-C', 'C', '2018-09-20', '2028-09-20', 'FR'),

    -- Marie n'a que le permis B
    ('b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e', 'PERMIS-987654', 'B', '2012-04-10', '2027-04-10', 'FR');