# Système de Gestion de Flotte de Véhicules

Ce projet est une application basée sur une architecture microservices pour la gestion et le suivi en temps réel d'une flotte de véhicules, dans le contexte applicatif de DHL Express.

## 1. Vue d'ensemble de l'architecture

Le système est découpé en services indépendants qui communiquent de manière synchrone via une API Gateway et de manière asynchrone via un bus d'événements Apache Kafka. Cette approche permet une meilleure scalabilité, une résilience accrue et des déploiements indépendants.

Les décisions d'architecture détaillées sont documentées dans le répertoire [/ADR](./ADR/).

### Composants principaux

- **Architecture** : 5 microservices principaux (Spring Boot, Node.js)
- **API Gateway** : Point d'entrée unique exposant les API REST et GraphQL.
- **Communication Asynchrone** : Apache Kafka comme bus d'événements.
- **Authentification** : Keycloak pour la gestion centralisée des identités et des accès (RBAC).
- **Bases de données** : Approche "une base par service" avec PostgreSQL, TimescaleDB (séries temporelles) et Redis (cache).
- **Observabilité** : Stack basée sur OpenTelemetry, Prometheus, Grafana, Loki et Jaeger.

### Les Microservices

| Service | Technologie | Responsabilité |
|---|---|---|
| **Véhicules** | Spring Boot | CRUD du parc, gestion des statuts et de l'historique. |
| **Conducteurs** | Spring Boot | Gestion des profils livreurs, permis, et assignations. |
| **Localisation** | Node.js | Ingestion du streaming GPS temps réel via gRPC, stockage sur TimescaleDB. |
| **Maintenance** | Spring Boot | Planification et suivi des interventions techniques. |
| **Événements** | Spring Boot | Analyse des règles métier et génération des alertes (ex: geofencing). |

---

## 2. API et Schémas

Le système expose ses fonctionnalités à travers une API Gateway qui supporte les protocoles REST et GraphQL.

### API REST (OpenAPI)

Les contrats d'interface pour les services REST sont définis au format OpenAPI v3 dans le répertoire [/gateway/openapi](./gateway/openapi/).

- `openapi-vehicles.yaml`
- `openapi-drivers.yaml`
- `openapi-maintenance.yaml`
- `openapi-events.yaml`
- `openapi-locations.yaml`

### API GraphQL

Le schéma GraphQL fédéré est défini dans le répertoire [/gateway/schema](./gateway/schema/). Chaque service expose une partie du graphe de données.

- `schema.graphql` (Point d'entrée)
- `vehicle.graphql`
- `driver.graphql`
- `maintenance.graphql`
- `location.graphql`
- `alert.graphql`

---

## 3. Communication Asynchrone avec Kafka

La communication inter-services est basée sur des événements échangés via Apache Kafka, garantissant le découplage des services. La liste complète des topics et leurs schémas sont documentés dans [kafka/kafka_topics.md](./kafka/kafka_topics.md).

**Topics principaux :**
- `flotte.vehicules.events` : Cycle de vie des véhicules.
- `flotte.conducteurs.events` : Cycle de vie des conducteurs.
- `flotte.assignments.events` : Affectation véhicule-livreur.
- `flotte.localisation.gps` : Positions GPS brutes à haute fréquence.
- `flotte.maintenance.events` : Événements de maintenance.
- `flotte.alertes.events` : Alertes consolidées pour les managers.

---

## 4. Schémas de Base de Données

Chaque microservice possède sa propre base de données. Les schémas SQL pour les bases de données PostgreSQL sont disponibles dans le répertoire [/sql](./sql/).

- `vehicle.sql`
- `driver.sql`
- `maintenance.sql`
- `location.sql`
- `events.sql`

---

## 5. Sécurité

L'authentification et l'autorisation sont gérées de manière centralisée par **Keycloak**.
- La configuration du Realm, des clients et des rôles est exportée dans [keycloak/realm-export.json](./keycloak/realm-export.json).
- Les rôles définis sont : `admin`, `manager`, `technicien`, `utilisateur`.

---

## 6. Démarrage Rapide

*(Cette section est à compléter avec les commandes spécifiques au projet)*

### Prérequis

- Docker & Docker Compose
- JDK 17+
- Node.js 18+
- Go 1.20+
- Un client `psql`

### Installation et Lancement

1.  **Cloner le dépôt :**
    ```bash
    git clone <URL_DU_PROJET>
    cd archi-dist-flotte-vehicules
    ```

2.  **Lancer l'infrastructure de base (Kafka, Keycloak, BDD...) :**
    ```bash
    # (À compléter) Exemple:
    # docker-compose up -d
    ```

3.  **Lancer les microservices :**
    ```bash
    # (À compléter)
    # ./scripts/start-all.sh
    ```

4.  **Accéder à l'application :**
    - API Gateway : `http://localhost:8080`
    - Keycloak : `http://localhost:8888`
    - Grafana : `http://localhost:3000`
