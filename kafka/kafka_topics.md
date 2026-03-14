# Kafka Topics — Système de Gestion de Flotte DHL

---

## Vue d'ensemble

| #  | Topic                        | Producteur              | Consommateurs                                     | Rétention |
|----|------------------------------|-------------------------|---------------------------------------------------|-----------|
| 1  | `flotte.vehicules.events`    | Service Véhicules       | Service Maintenance, Service Événements           | 7 jours   |
| 2  | `flotte.conducteurs.events`  | Service Conducteurs     | Service Véhicules, Service Événements             | 7 jours   |
| 3  | `flotte.assignments.events`  | Service Véhicules       | Service Conducteurs, Service Événements           | 30 jours  |
| 4  | `flotte.localisation.gps`    | Service Localisation    | Service Événements, Service Maintenance           | 2 jours   |
| 5  | `flotte.maintenance.events`  | Service Maintenance     | Service Véhicules, Service Événements             | 30 jours  |
| 6  | `flotte.alertes.events`      | Service Événements      | API Gateway / Frontend, Service Email (futur)     | 14 jours  |

---

## Topic 1 — `flotte.vehicules.events`

Gère le cycle de vie des véhicules de la flotte DHL (camionnettes, camions de livraison, utilitaires).

- **Producteur** : Service Véhicules
- **Consommateurs** : Service Maintenance, Service Événements
- **Partitions** : 6 | **Clé** : `vehicle_id`

**Types d'événements** : `VEHICLE_CREATED` · `VEHICLE_UPDATED` · `VEHICLE_STATUS_CHANGED` · `VEHICLE_DELETED`

```json
{
  "event_id": "uuid-v4",
  "event_type": "VEHICLE_STATUS_CHANGED",
  "event_version": "1.0",
  "timestamp": "2026-03-14T06:00:00Z",
  "payload": {
    "vehicle_id": "uuid-v4",
    "plate_number": "DH-076-LR",
    "brand": "Mercedes",
    "model": "Sprinter 314 CDI",
    "fuel_type": "DIESEL",
    "status": { "previous": "AVAILABLE", "current": "ON_DELIVERY" },
    "mileage_km": 87430
  },
  "metadata": { "correlation_id": "uuid-v4", "triggered_by": "uuid-dispatcher" }
}
```

---

## Topic 2 — `flotte.conducteurs.events`

Gère le cycle de vie des livreurs DHL : embauche, changement de statut, et validité du permis.

- **Producteur** : Service Conducteurs
- **Consommateurs** : Service Véhicules (libérer le véhicule si livreur suspendu), Service Événements (alerte permis expiré)
- **Partitions** : 4 | **Clé** : `driver_id`

**Types d'événements** : `DRIVER_CREATED` · `DRIVER_UPDATED` · `DRIVER_STATUS_CHANGED` · `LICENSE_EXPIRING` · `LICENSE_EXPIRED`

```json
{
  "event_id": "uuid-v4",
  "event_type": "LICENSE_EXPIRING",
  "event_version": "1.0",
  "timestamp": "2026-03-14T08:00:00Z",
  "payload": {
    "driver_id": "uuid-v4",
    "first_name": "Marc",
    "last_name": "Bernard",
    "employee_id": "DHL-76203",
    "status": "ACTIVE",
    "license": {
      "license_number": "123456789012",
      "category": "C",
      "expiry_date": "2026-04-01",
      "days_remaining": 18
    }
  },
  "metadata": { "correlation_id": "uuid-v4" }
}
```

---

## Topic 3 — `flotte.assignments.events`

Trace l'affectation des véhicules aux livreurs pour chaque tournée de livraison DHL.

- **Producteur** : Service Véhicules
- **Consommateurs** : Service Conducteurs (mise à jour statut livreur), Service Événements
- **Partitions** : 4 | **Clé** : `vehicle_id`

**Types d'événements** : `VEHICLE_ASSIGNED` · `VEHICLE_UNASSIGNED`

```json
{
  "event_id": "uuid-v4",
  "event_type": "VEHICLE_ASSIGNED",
  "event_version": "1.0",
  "timestamp": "2026-03-14T06:30:00Z",
  "payload": {
    "assignment_id": "uuid-v4",
    "vehicle_id": "uuid-v4",
    "driver_id": "uuid-v4",
    "started_at": "2026-03-14T06:30:00Z",
    "ended_at": null,
    "notes": "Tournée Rouen Nord — Secteur R12 — 63 colis"
  },
  "metadata": { "correlation_id": "uuid-v4", "triggered_by": "uuid-dispatcher" }
}
```

---

## Topic 4 — `flotte.localisation.gps`

Transporte les positions GPS en temps réel des véhicules DHL en tournée.  
Volume élevé (~1 message / 5 s par véhicule). Archivé dans TimescaleDB.

- **Producteur** : Service Localisation / boîtier GPS embarqué
- **Consommateurs** : Service Événements (géofencing zones de livraison), Service Maintenance (kilométrage)
- **Partitions** : 12 | **Clé** : `vehicle_id`

**Types d'événements** : `GPS_POSITION_UPDATED`

```json
{
  "event_id": "uuid-v4",
  "event_type": "GPS_POSITION_UPDATED",
  "event_version": "1.0",
  "timestamp": "2026-03-14T10:30:05Z",
  "payload": {
    "vehicle_id": "uuid-v4",
    "latitude": 49.4431,
    "longitude": 1.0993,
    "speed_kmh": 48.5,
    "heading_deg": 185.3,
    "accuracy_m": 4.2,
    "source": "GPS_DEVICE"
  },
  "metadata": { "correlation_id": "uuid-v4" }
}
```

---

## Topic 5 — `flotte.maintenance.events`

Annonce les entretiens planifiés ou terminés sur les véhicules de la flotte DHL.  
Déclenche automatiquement la remise en disponibilité du véhicule après intervention.

- **Producteur** : Service Maintenance
- **Consommateurs** : Service Véhicules (→ statut `AVAILABLE`), Service Événements (alerte si retard)
- **Partitions** : 4 | **Clé** : `vehicle_id`

**Types d'événements** : `MAINTENANCE_SCHEDULED` · `MAINTENANCE_STARTED` · `MAINTENANCE_COMPLETED` · `MAINTENANCE_OVERDUE` · `MAINTENANCE_CANCELLED`

```json
{
  "event_id": "uuid-v4",
  "event_type": "MAINTENANCE_COMPLETED",
  "event_version": "1.0",
  "timestamp": "2026-03-14T16:00:00Z",
  "payload": {
    "record_id": "uuid-v4",
    "vehicle_id": "uuid-v4",
    "type": "PREVENTIVE",
    "status": { "previous": "IN_PROGRESS", "current": "COMPLETED" },
    "technician_id": "uuid-v4",
    "completed_date": "2026-03-14",
    "cost_eur": 320.00,
    "next_service_km": 97430
  },
  "metadata": { "correlation_id": "uuid-v4", "triggered_by": "uuid-technicien" }
}
```

---

## Topic 6 — `flotte.alertes.events`

Centralise toutes les alertes opérationnelles à destination des dispatchers et managers DHL.  
Seul le Service Événements produit sur ce topic.

- **Producteur** : Service Événements
- **Consommateurs** : API Gateway / Frontend (notification temps réel dispatcher), Service Email (futur)
- **Partitions** : 4 | **Clé** : `vehicle_id`

**Types d'alertes** :

| `alert_type`              | Sévérité   | Topic source                   | Contexte DHL                              |
|---------------------------|------------|--------------------------------|-------------------------------------------|
| `GEOFENCING_BREACH`       | `HIGH`     | `flotte.localisation.gps`      | Véhicule hors zone de tournée assignée    |
| `SPEED_EXCEEDED`          | `MEDIUM`   | `flotte.localisation.gps`      | Dépassement des limites de vitesse DHL    |
| `VEHICLE_IMMOBILIZED`     | `CRITICAL` | `flotte.localisation.gps`      | Arrêt anormal en cours de tournée         |
| `MAINTENANCE_OVERDUE`     | `HIGH`     | `flotte.maintenance.events`    | Véhicule non entretenu, risque opérationnel |
| `LICENSE_EXPIRING`        | `MEDIUM`   | `flotte.conducteurs.events`    | Permis C du livreur bientôt expiré        |
| `DELIVERY_TOUR_DELAYED`   | `MEDIUM`   | `flotte.localisation.gps`      | Tournée en retard sur le planning         |

```json
{
  "event_id": "uuid-v4",
  "event_type": "ALERT_CREATED",
  "event_version": "1.0",
  "timestamp": "2026-03-14T10:35:00Z",
  "payload": {
    "alert_id": "uuid-v4",
    "type": "GEOFENCING_BREACH",
    "severity": "HIGH",
    "status": "OPEN",
    "vehicle_id": "uuid-v4",
    "driver_id": "uuid-v4",
    "message": "Le véhicule DH-076-LR a quitté la zone de tournée R12 (Rouen Nord).",
    "expires_at": "2026-03-15T10:35:00Z"
  },
  "metadata": { "correlation_id": "uuid-v4", "source_event_id": "uuid-event-gps" }
}
```

---

## Flux entre topics

```
[Boîtier GPS] ──► flotte.localisation.gps ──► Service Événements ──► flotte.alertes.events ──► Dispatcher DHL
                                          └──► Service Maintenance (kilométrage)

[Svc Véhicules] ──► flotte.vehicules.events   ──► Service Maintenance
                                               └──► Service Événements

[Svc Véhicules] ──► flotte.assignments.events ──► Service Conducteurs (livreur en tournée)
                                               └──► Service Événements

[Svc Conducteurs] ──► flotte.conducteurs.events ──► Service Véhicules (libère le véhicule)
                                                 └──► Service Événements ──► flotte.alertes.events

[Svc Maintenance] ──► flotte.maintenance.events ──► Service Véhicules (→ AVAILABLE)
                                                └──► Service Événements ──► flotte.alertes.events
```
