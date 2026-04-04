# 🚛 Système de Gestion de Flotte de Véhicules
**Projet M1 GIL -- Université de Rouen Normandie (2025-2026)**

Architecture microservices distribuée pour la gestion d'une flotte de véhicules, conducteurs et interventions techniques.

---

## 🏗️ Architecture du Système (Semaine 4)

Le projet repose sur une architecture **Cloud Native** décomposée en microservices spécialisés, communiquant via REST (synchrone) et Apache Kafka (asynchrone / Saga).

### Microservices Métier Opérationnels
- **[Vehicle Service](./services/vehicle-service)** (Spring Boot) : Inventaire, caractéristiques techniques, états des véhicules.
- **[Driver Service](./services/driver-service)** (Spring Boot) : Profils conducteurs, gestion des permis, validité.
- **[Maintenance Service](./services/maintenance-service)** (Spring Boot) : Planification, historique des réparations, alertes.

### Infrastructure & Edge
- **Gateway GraphQL** (Apollo Server) : Point d'entrée unique agrégeant les données des services REST.
- **Keycloak** (IAM) : Sécurisation des APIs via OAuth2/OIDC (JWT).
- **Apache Kafka** : Orchestration des processus inter-services (Pattern Saga par chorégraphie).
- **PostgreSQL** : Persistance polyglotte (une base isolée par service).

---

## 🚀 Démarrage Rapide

### Option A : Docker Compose (Développement)
Idéal pour tester rapidement l'application sur `localhost`.
```bash
docker compose up -d --build
```
> Les bases de données sont automatiquement peuplées de données réalistes au démarrage grâce à **Datafaker**.

### Option B : Kubernetes / Minikube (Production-like)
Utilisez le script d'automatisation pour déployer toute la stack (Helm + Ingress + Keycloak).
```bash
chmod +x kube.sh
./kube.sh
```
Ajoutez ensuite l'entrée DNS suivante à votre fichier `/etc/hosts` :
```bash
echo "$(minikube ip) flotte.local" | sudo tee -a /etc/hosts
```

---

## 🔐 Accès et Services

| Service | Local (Compose) | Cluster (K8s) |
| :--- | :--- | :--- |
| **Gateway GraphQL** | [http://127.0.0.1:4000](http://127.0.0.1:4000) (`localhost` équivalent) | `http://flotte.local/graphql` |
| **Keycloak** | [http://127.0.0.1:8180](http://127.0.0.1:8180) (`localhost` équivalent) | `http://flotte.local/auth` |
| **Bases de Données** | [localhost:5432](localhost:5432) | Service interne |
| **Kafka UI / Watch** | `./watch-kafka.sh` | Pod dédié |

### Identifiants par défaut
- **Keycloak Admin :** `admin` / `admin`
- **Utilisateur Test :** `test-user` / `password` (Realm `gestion-flotte`)
- **Bases PG :** `admin` / `password`

---

## 🧪 Tests et Documentation API

### Bruno (Remplaçant de Postman)
Toutes les requêtes de test (REST et GraphQL) sont centralisées dans une collection **[Bruno](https://usebruno.com)**.

#### Utilisation
1.  **Ouvrir Bruno** et cliquer sur "Open Collection".
2.  Sélectionner le dossier [`/bruno`](./bruno) à la racine du projet.
3.  **Sélectionner l'environnement** en haut à droite :
    - `Docker` : `base_url` = `http://127.0.0.1:4000` (gateway), Keycloak en `8180` avec préfixe `/auth` (voir [`bruno/environments/Docker.json`](./bruno/environments/Docker.json)).
    - `Minikube` : pour tester sur `http://flotte.local` (nécessite la configuration du fichier `/etc/hosts`).
4.  **Authentification automatique** : le script `pre-request` de `collection.bru` obtient un JWT Keycloak via `bru.sendRequest`, rafraîchit le jeton selon l’expiration du payload, et pose `Authorization` sur chaque requête. Aucun POST « token » manuel.

#### Structure de la collection
- **`bruno.json`** : métadonnées de la collection (nom `archi-distrib`).
- **`collection.bru`** : script de collection (token + réécriture d’URL selon l’environnement).
- **`environments/`** : `Docker.json` et `Minikube.json` (`base_url`, `keycloak_token_url`, identifiants `kc_*`).
- **`REST/`** : tests des endpoints CRUD pour les services `vehicle`, `driver` et `maintenance`.
- **`GraphQL/`** : requêtes d’agrégation via `{{base_url}}/graphql` (gateway).

### Qualité et Couverture (JaCoCo)
Chaque service Spring Boot vise une couverture de test **> 80%**.
```bash
# Exemple pour le service Driver
cd services/driver-service && ./mvnw test -Punit-coverage
```
Les rapports sont générés dans `target/site/jacoco/index.html` de chaque service.

---

## 🛠️ Stack Technique
- **Backend :** Java 21, Spring Boot 3.4, Node.js 20.
- **Data :** PostgreSQL 16, Hibernate (DDL Auto-update).
- **Event :** Kafka (KRaft mode).
- **Ops :** Helm 3, Docker, GitHub Actions (CI/CD).
- **Observabilité :** Prometheus, Grafana, Jaeger, Loki.
