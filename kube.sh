#!/bin/bash

# 1. Préparation Minikube
minikube start
minikube addons enable ingress

# 2. Nettoyage (optionnel mais recommandé pour repartir de zéro)
kubectl delete namespace flotte-namespace --ignore-not-found
kubectl create namespace flotte-namespace

# 3. Création des Secrets et ConfigMaps
# Utilisation de l'utilisateur 'admin' défini dans la configuration Helm
kubectl create secret generic db-secrets \
  --from-literal=SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-flotte-service:5432/vehicle_db \
  --from-literal=DRIVER_DATASOURCE_URL=jdbc:postgresql://postgres-flotte-service:5432/driver_db \
  --from-literal=MAINTENANCE_DATASOURCE_URL=jdbc:postgresql://postgres-flotte-service:5432/maintenance_db \
  --from-literal=SPRING_DATASOURCE_USERNAME=admin \
  --from-literal=SPRING_DATASOURCE_PASSWORD=password \
  --from-literal=KAFKA_BROKER=kafka-service:9092 \
  -n flotte-namespace

# 4. Build des Images dans le Docker daemon de Minikube
eval $(minikube docker-env)

echo "Building images..."
docker build -t vehicle-service:latest ./services/vehicle-service/
docker build -t driver-service:latest ./services/driver-service/
docker build -t maintenance-service:latest ./services/maintenance-service/
docker build -t graphql-gateway:latest ./gateway/

# 5. Déploiement Infrastructure (DB, Kafka, Redis)
helm dependency update ./infra/helm/fleet-infra/
helm upgrade --install fleet-infra ./infra/helm/fleet-infra/ \
  -n flotte-namespace \
  -f ./infra/helm/fleet-infra/values.secret.yaml

# 6. Déploiement Observabilité (Loki, Grafana, OTel)
helm upgrade --install fleet-obs ./infra/helm/fleet-observability/ \
  -n flotte-namespace

# 7. Déploiement Keycloak
# On s'assure que le ConfigMap est créé avant le déploiement
kubectl apply -f infra/kubernetes/keycloak/keycloak-configmap.yaml -n flotte-namespace
kubectl apply -f infra/kubernetes/keycloak/keycloak-service.yaml -n flotte-namespace
kubectl apply -f infra/kubernetes/keycloak/keycloak-deployment.yaml -n flotte-namespace

# 8. Déploiement de l'Application via Helm (Fleet-App)
# On attend un peu que l'infra soit prête
echo "Waiting for infrastructure to be ready..."
kubectl wait --for=condition=ready --timeout=300s pod/postgres-flotte-service-0 -n flotte-namespace

helm upgrade --install fleet-app ./infra/helm/fleet-app/ \
  -n flotte-namespace

# 9. Configuration du Host (Optionnel : demande sudo)
echo "--------------------------------------------------"
echo "Terminé ! N'oubliez pas d'ajouter l'entrée suivante à votre /etc/hosts :"
echo "$(minikube ip) flotte.local"
echo "--------------------------------------------------"
