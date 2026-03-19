# Système de Gestion de Flotte de Véhicules

Projet M1 GIL -- Université de Rouen Normandie (2025-2026).
Architecture microservices distribuée pour la gestion d'une flotte de véhicules.

---

## 1. État de l'Infrastructure (Semaine 2)

L'infrastructure DevOps est opérationnelle. Elle supporte le développement local via Docker Compose et l'orchestration via Kubernetes (Minikube).

### Stack Technique
- Orchestration : Kubernetes (Minikube) & Docker Compose.
- Bus & Data : Apache Kafka (KRaft), PostgreSQL, Redis.
- Sécurité : Keycloak (SSO).
- Observabilité : OpenTelemetry, Jaeger, Prometheus, Loki, Grafana.

---

## 2. Développement Local (Docker Compose)

### Lancement
```bash
docker compose up -d
```

### Accès aux Services (Localhost)
| Service | URL / Port | Identifiants |
| :--- | :--- | :--- |
| Grafana | http://localhost:3001 | admin / admin |
| Jaeger | http://localhost:16686 | - |
| Keycloak | http://localhost:8180 | admin / admin |
| pgAdmin | http://localhost:5050 | admin@flotte.com / admin |

---

## 3. Mode Cluster Local (Kubernetes / Minikube)

### Étape 1 : Préparer Minikube
```bash
minikube start
minikube addons enable ingress
kubectl apply -f infra/kubernetes/namespaces/namespaces.yaml
```

### Étape 2 : Construire les images (Local Build)
```bash
eval $(minikube docker-env)
docker build -t vehicle-service:latest ./services/vehicle-service/
```

### Étape 3 : Déployer l'Infrastructure (Helm)
```bash
helm dependency update ./infra/helm/fleet-infra/
helm upgrade --install fleet-infra ./infra/helm/fleet-infra/ \
  -n flotte-namespace
```

### Étape 4 : Déployer l'Application (Helm)
```bash
helm upgrade --install fleet-app ./infra/helm/fleet-app/ \
  -n flotte-namespace
```

### Étape 5 : Configuration Réseau
```bash
echo "$(minikube ip) flotte.local" | sudo tee -a /etc/hosts
```

Accès unifiés (Slash final obligatoire) :
- Grafana : http://flotte.local/grafana/
- Jaeger : http://flotte.local/jaeger/
- APIs : http://flotte.local/api/[service]/

---

## 4. Pipeline CI/CD (GitHub Actions)
Le workflow `.github/workflows/ci-cd.yml` valide chaque commit :
- Tests unitaires (Maven/NPM).
- Linting Helm.
- Build Docker & Push vers GHCR (branche main).
