# Système de Gestion de Flotte de Véhicules

Projet M1 GIL -- Université de Rouen Normandie (2025-2026).
Architecture microservices distribuée pour la gestion d'une flotte de véhicules.

---

## 1. État du Projet (Semaine 3)

Le projet a franchi une étape majeure avec l'implémentation du premier microservice métier complet (**Service Véhicules**) et l'intégration de la stack d'observabilité.

### Réalisations Semaine 3
- **Microservice Véhicules** : API REST CRUD complète, validation des données, Soft Delete.
- **Architecture Événementielle** : Intégration d'Apache Kafka (Producer/Consumer) pour les événements métier (`VEHICLE_CREATED`, etc.).
- **Qualité & Tests** : Tests unitaires et d'intégration avec une couverture de code > 90% (JaCoCo).
- **GraphQL** : Résolveurs opérationnels pour les véhicules au niveau de la Gateway.
- **Observabilité** : Instrumentation native avec le SDK OpenTelemetry (Traces, Métriques, Logs structurés).
- **DevOps** : Support complet Docker Compose et Helm (Kubernetes).

### Stack Technique
- Orchestration : Kubernetes (Minikube) & Docker Compose.
- Langages : Java 21 (Spring Boot 3), Node.js (Gateway GraphQL).
- Bus & Data : Apache Kafka (KRaft), PostgreSQL, Redis.
- Sécurité : Keycloak (SSO).
- Observabilité : OpenTelemetry, Jaeger, Prometheus, Loki, Grafana.

---

## 2. Développement Local (Docker Compose)

### Lancement
```bash
docker compose up -d --build
```

### Accès aux Services (Localhost)
| Service | URL / Port | Identifiants |
| :--- | :--- | :--- |
| **API Véhicules** | http://localhost:8080 | - |
| Grafana | http://localhost:3001 | admin / admin |
| Jaeger | http://localhost:16686 | - |
| Keycloak | http://localhost:8180 | admin / admin |
| pgAdmin | http://localhost:5050 | admin@flotte.com / admin |
| Prometheus | http://localhost:9090 | - |

### Vérification du service (Véhicules)
Si vous accédez à `http://localhost:8080/`, vous verrez une "Whitelabel Error Page", ce qui est normal pour une API REST sans page d'accueil. Utilisez les points de terminaison suivants pour tester le service :

- **Liste des véhicules :** [http://localhost:8080/vehicles](http://localhost:8080/vehicles) (Retourne `[]` ou la liste des véhicules en JSON)
- **État de santé :** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) (Doit retourner `{"status":"UP"}`)
- **Métriques :** [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus)

---

## 3. Mode Cluster Local (Kubernetes / Minikube)

### Étape 1 : Préparer Minikube
```bash
minikube start
minikube addons enable ingress
kubectl apply -f infra/kubernetes/namespaces/namespaces.yaml
```

### Étape 2 : Créer les secrets (Obligatoire)
Le service véhicules a besoin de secrets pour se connecter aux infrastructures.
```bash
kubectl create secret generic db-secrets \
  --from-literal=SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-service:5432/flotte_db \
  --from-literal=SPRING_DATASOURCE_PASSWORD=password \
  --from-literal=KAFKA_BROKER=kafka-service:9092 \
  -n flotte-namespace
```

### Étape 3 : Construire les images (Local Build)
```bash
eval $(minikube docker-env)

# Build de tous les services
docker build -t vehicle-service:latest ./services/vehicle-service/
docker build -t driver-service:latest ./services/driver-service/
docker build -t maintenance-service:latest ./services/maintenance-service/
docker build -t event-service:latest ./services/events-service/
docker build -t location-service:latest ./services/location-service/
```

### Étape 4 : Déployer l'Infrastructure
```bash
# 1. Installer l'infra de base (PostgreSQL, Kafka, Redis) via Helm
helm dependency update ./infra/helm/fleet-infra/
helm upgrade --install fleet-infra ./infra/helm/fleet-infra/ \
  -n flotte-namespace \
  -f ./infra/helm/fleet-infra/values.secret.yaml

# 2. Installer la stack d'observabilité (LGTMe + OTel) via Helm
helm upgrade --install fleet-obs ./infra/helm/fleet-observability/ \
  -n flotte-namespace

# 3. Déployer Keycloak (SSO) via les fichiers YAML
kubectl apply -f infra/kubernetes/keycloak/keycloak-configmap.yaml -n flotte-namespace
kubectl apply -f infra/kubernetes/keycloak/keycloak-service.yaml -n flotte-namespace
kubectl apply -f infra/kubernetes/keycloak/keycloak-deployment.yaml -n flotte-namespace
```

### Étape 5 : Déployer l'Application (Helm)
```bash
helm upgrade --install fleet-app ./infra/helm/fleet-app/ \
  -n flotte-namespace
```

### Étape 6 : Configuration Réseau
```bash
echo "$(minikube ip) flotte.local" | sudo tee -a /etc/hosts
```

Accès unifiés :
- **API Véhicules :** [http://flotte.local/api/vehicles/](http://flotte.local/api/vehicles/)
- **Keycloak Admin :** [http://flotte.local/admin](http://flotte.local/admin) (admin / admin)
- **Grafana :** [http://flotte.local/grafana/](http://flotte.local/grafana/)
- **Jaeger :** [http://flotte.local/jaeger/](http://flotte.local/jaeger/)


### Vérification du service sur Minikube
Une fois déployé, vous pouvez tester le bon fonctionnement via l'Ingress :
- **Santé :** `curl http://flotte.local/api/vehicles/actuator/health` (Doit retourner `{"status":"UP"}`)
- **API :** `curl http://flotte.local/api/vehicles/vehicles` (Doit retourner `[]`)

---

## 4. Tests et Qualité (Service Véhicule)
Le service véhicule dispose de tests unitaires et d'intégration avec un objectif de couverture > 90% (actuellement ~91%).

### Lancer les tests
```bash
cd services/vehicle-service
./mvnw test
```

### Consulter la couverture
Le rapport de couverture JaCoCo est **automatiquement généré** après l'exécution des tests. Le rapport détaillé est consultable ici :
`services/vehicle-service/target/site/jacoco/index.html`

---

## 6. Observabilité & Monitoring (Grafana)

L'application utilise une stack complète (LGTMe) pour le monitoring. Vous pouvez accéder à Grafana sur [http://localhost:3001](http://localhost:3001) (ou [http://flotte.local/grafana/](http://flotte.local/grafana/) sur Minikube).

### Logs (Loki)
Pour consulter les logs du microservice véhicule dans l'onglet **Explore** :
- **Source :** Loki
- **Query :** `{job="vehicle-service"}`

### Métriques (Prometheus)
Pour visualiser les métriques de performance de la JVM et de l'application :
- **Source :** Prometheus
- **Query (exemples) :** 
  - `jvm_memory_used_bytes` : Utilisation de la mémoire RAM par le service.
  - `http_server_requests_seconds_count` : Nombre total de requêtes HTTP traitées.
  - `process_cpu_usage` : Utilisation du CPU par le microservice.

### Traces (Jaeger)
Pour suivre le cheminement d'une requête de bout en bout :
- Accédez à Jaeger ([http://localhost:16686](http://localhost:16686)).
- Sélectionnez le service **vehicle-service** dans la liste déroulante.
- Cliquez sur **Find Traces** pour visualiser les appels API et les requêtes SQL (PostgreSQL).
