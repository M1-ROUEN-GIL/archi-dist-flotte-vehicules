# Système de Gestion de Flotte de Véhicules
**Projet M1 GIL — Université de Rouen Normandie (2025-2026)**

Architecture microservices distribuée pour la gestion d'une flotte de véhicules.

---

## Démarrage rapide

### Docker Compose
```bash
docker compose up -d --build
```

### Minikube
```bash
./scripts/kube.sh
echo "$(minikube ip) flotte.local" | sudo tee -a /etc/hosts
minikube tunnel  # dans un terminal séparé
```

---

## Accès aux services

| Service        | Docker Compose                   | Minikube                        |
| :------------- | :------------------------------- | :------------------------------ |
| Frontend       | http://localhost:3005            | http://flotte.local             |
| Gateway GraphQL| http://localhost:4000            | http://flotte.local/graphql     |
| Keycloak       | http://localhost:8180            | http://flotte.local/auth        |
| Grafana        | http://localhost:3000            | http://flotte.local/grafana     |
| Jaeger         | http://localhost:16686           | http://flotte.local/jaeger      |

**Identifiants par défaut**
- Keycloak admin : `admin` / `admin`
- Utilisateur test : `test-user` / `password` (realm `gestion-flotte`)
- PostgreSQL : `admin` / `password`

---

## Scripts

Tous les scripts se trouvent dans [`scripts/`](./scripts).

| Script | Description |
| :----- | :---------- |
| `kube.sh` | Lance Minikube, build les images, déploie toute la stack via Helm et attend que tout soit prêt. |
| `simulate.sh docker\|minikube` | Simule un camion en mouvement en envoyant des coordonnées GPS en temps réel via gRPC. |
| `reset-docker.sh` | Remet Docker Compose à zéro (volumes inclus), relance les services et redémarre la Gateway. |
| `watch-kafka.sh` | Affiche en temps réel les événements Kafka du namespace `flotte.*` (Minikube uniquement). |
| `test-e2e.sh docker\|minikube` | Vérifie l'accès à l'environnement cible, lance les tests Playwright et ouvre le rapport HTML. |

```bash
# Exemples
./scripts/simulate.sh docker
./scripts/simulate.sh minikube

./scripts/test-e2e.sh docker
./scripts/test-e2e.sh minikube
```

---

## Microservices

| Service | Rôle |
| :------ | :--- |
| `vehicle-service` | Inventaire et états des véhicules |
| `driver-service` | Profils conducteurs et permis |
| `maintenance-service` | Planification et historique des réparations |
| `location-service` | Géolocalisation et suivi GPS (gRPC + WebSocket) |
| `events-service` | Événements métier via Kafka |
| `graphql-gateway` | Agrégation et exposition unifiée de l'API |

---

## Frontend

Architecture **Micro-Frontend** basée sur Vite Module Federation (npm workspaces).

| App | Rôle | Port (dev) |
| :-- | :--- | :--------- |
| `shell` | Application hôte — routing, layout, chargement des remotes | — |
| `vehicles` | Module véhicules (remote) | 5002 |
| `drivers` | Module conducteurs (remote) | 5003 |
| `maintenance` | Module maintenance (remote) | 5004 |
| `location` | Module géolocalisation temps réel (remote) | 5005 |

**Packages partagés** (`packages/`) :

| Package | Contenu |
| :------ | :------ |
| `shared-auth` | Contexte Keycloak / OIDC partagé entre remotes |
| `shared-client` | Client Apollo GraphQL partagé |
| `shared-ui` | Composants UI communs |

---

## Infrastructure & Bases de données

| Service | Image | Rôle |
| :------ | :---- | :--- |
| `flotte-postgres` | `timescale/timescaledb:2.14.2-pg15` | Base de données principale (TimescaleDB) — une base par microservice |
| `flotte-redis` | `redis:7.2-alpine` | Cache distribué |
| `flotte-kafka` | `apache/kafka:3.7.0` | Bus d'événements (KRaft, sans ZooKeeper) |
| `flotte-keycloak` | `keycloak:26.5.7` | Authentification OAuth2 / OIDC |
| `flotte-pgadmin` | `dpage/pgadmin4` | Interface d'administration PostgreSQL |

---

## Stack d'observabilité

| Service | Image | Rôle |
| :------ | :---- | :--- |
| `flotte-jaeger` | `jaegertracing/all-in-one:1.55` | Tracing distribué — UI sur :16686 |
| `flotte-prometheus` | `prom/prometheus:v2.51.0` | Collecte des métriques |
| `flotte-loki` | `grafana/loki:2.9.4` | Agrégation des logs |
| `flotte-grafana` | `grafana/grafana:10.4.1` | Dashboards (métriques, logs, traces) |
| `flotte-otel-collector` | `otel/opentelemetry-collector-contrib:0.96.0` | Point d'ingestion OTLP central (gRPC :4317, HTTP :4318) |
| `flotte-promtail` | `grafana/promtail:2.9.4` | Collecte des logs non-OTLP (frontend, location-service) vers Loki |

---

## Tests API avec Bruno

1. Ouvrir [Bruno](https://usebruno.com) et charger le dossier [`/bruno`](./bruno)
2. Choisir l'environnement : **Docker** ou **Minikube**
3. L'authentification JWT est gérée automatiquement par la collection

---

## Stack technique

- **Backend** : Java 21, Spring Boot 3.4, Node.js 20
- **Frontend** : React, Vite, Module Federation (Micro-Frontend)
- **Auth** : Keycloak (OAuth2 / OIDC)
- **Data** : PostgreSQL 16, Apache Kafka (KRaft)
- **Infra** : Docker, Kubernetes / Helm 3
- **Observabilité** : Prometheus, Grafana, Jaeger, Loki, Promtail, OpenTelemetry
- **Tests E2E** : Playwright
