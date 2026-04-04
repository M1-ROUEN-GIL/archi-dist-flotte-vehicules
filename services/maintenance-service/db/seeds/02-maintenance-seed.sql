-- =============================================================
-- DONNÉES DE TEST — maintenance-service
-- Références logiques : vehicle_id = ids dans 02-vehicle-seed.sql (véhicules)
--                       technician_id = ids dans 02-driver-seed.sql (conducteurs)
-- =============================================================

INSERT INTO maintenance_records (
  id,
  vehicle_id,
  type,
  status,
  priority,
  scheduled_date,
  completed_date,
  technician_id,
  description,
  cost_eur,
  mileage_at_service,
  next_service_km,
  parts_used,
  notes
)
VALUES
  -- Intervention terminée (révision) sur le Sprinter DH-001
  (
    '20000000-0000-4000-8000-000000000001',
    '10000000-0000-4000-8000-000000000001',
    'PREVENTIVE',
    'COMPLETED',
    'MEDIUM',
    '2025-11-10',
    '2025-11-12',
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    'Révision annuelle : vidange, filtres, contrôle freins',
    385.50,
    11800,
    13800,
    '[{"part":"filtre à huile","ref":"OEM-FH-01","qty":1},{"part":"huile 5W30","qty":6}]'::jsonb,
    'Véhicule conforme, prochaine révision dans 20 000 km.'
  ),
  -- Fiche planifiée (panne signalée) sur le Master DH-002
  (
    '20000000-0000-4000-8000-000000000002',
    '10000000-0000-4000-8000-000000000002',
    'CORRECTIVE',
    'SCHEDULED',
    'HIGH',
    '2026-05-15',
    NULL,
    'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
    'Diagnostic climatisation : faible performance',
    NULL,
    NULL,
    NULL,
    '[]'::jsonb,
    NULL
  ),
  -- En cours sur le Transit DH-003 (cohérent avec statut véhicule in_maintenance)
  (
    '20000000-0000-4000-8000-000000000003',
    '10000000-0000-4000-8000-000000000003',
    'CORRECTIVE',
    'IN_PROGRESS',
    'CRITICAL',
    '2026-04-01',
    NULL,
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    'Remplacement disques et plaquettes avant',
    NULL,
    34980,
    NULL,
    '[{"part":"plaquettes avant","qty":1},{"part":"disques avant","qty":2}]'::jsonb,
    'Intervention démarrée, pièces livrées.'
  ),
  -- Contrôle réglementaire planifié — e-Expert DH-004
  (
    '20000000-0000-4000-8000-000000000004',
    '10000000-0000-4000-8000-000000000004',
    'INSPECTION',
    'SCHEDULED',
    'LOW',
    '2026-06-01',
    NULL,
    NULL,
    'Contrôle périodique réglementaire',
    NULL,
    NULL,
    NULL,
    '[]'::jsonb,
    NULL
  ),
  -- En retard (non réalisée après la date) — Vito DH-005
  (
    '20000000-0000-4000-8000-000000000005',
    '10000000-0000-4000-8000-000000000005',
    'PREVENTIVE',
    'OVERDUE',
    'HIGH',
    '2026-03-01',
    NULL,
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    'Distribution courroie selon carnet constructeur',
    NULL,
    NULL,
    NULL,
    '[]'::jsonb,
    'À replanifier en urgence.'
  )
ON CONFLICT (id) DO NOTHING;
