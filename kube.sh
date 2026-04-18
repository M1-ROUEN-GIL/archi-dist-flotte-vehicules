#!/bin/bash
set -e

echo "🚀 Démarrage de Minikube..."
minikube start --addons=ingress

echo "⏳ Attente du contrôleur Ingress..."
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s

# Passage en LoadBalancer pour minikube tunnel
kubectl patch svc ingress-nginx-controller -n ingress-nginx \
  -p '{"spec":{"type":"LoadBalancer"}}'

echo "🧹 Nettoyage du namespace..."
kubectl delete namespace flotte-namespace --ignore-not-found
kubectl create namespace flotte-namespace

# Ajout des repos pour éviter les erreurs de dependency build
helm repo add bitnami https://charts.bitnami.com/bitnami --force-update > /dev/null 2>&1 || true
helm repo update bitnami > /dev/null 2>&1 || true

echo "🔐 Création des secrets..."
kubectl create secret generic db-secrets \
  --from-literal=SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-flotte-service:5432/vehicle_db \
  --from-literal=DRIVER_DATASOURCE_URL=jdbc:postgresql://postgres-flotte-service:5432/driver_db \
  --from-literal=MAINTENANCE_DATASOURCE_URL=jdbc:postgresql://postgres-flotte-service:5432/maintenance_db \
  --from-literal=EVENTS_DATASOURCE_URL=jdbc:postgresql://postgres-flotte-service:5432/events_db \
  --from-literal=SPRING_DATASOURCE_USERNAME=admin \
  --from-literal=SPRING_DATASOURCE_PASSWORD=password \
  --from-literal=KAFKA_BROKER=kafka-service:9092 \
  -n flotte-namespace

echo "📦 Build des images (via minikube image build)..."
services=("vehicle-service" "driver-service" "maintenance-service" "events-service" "location-service")
for s in "${services[@]}"; do
    echo "🔨 Building $s..."
    minikube image build -t "$s:latest" "./services/$s/"
done

echo "🔨 Building graphql-gateway..."
minikube image build -t "graphql-gateway:latest" "./gateway/"

echo "🔨 Building flotte-frontend..."
minikube image build -t "flotte-frontend:latest" "./frontend/"

echo "🛠️ Déploiement de l'infrastructure (DB, Kafka, Redis)..."
helm dependency build ./infra/helm/fleet-infra/ || echo "⚠️ Warning: build failed, attempting to continue..."
helm upgrade --install fleet-infra ./infra/helm/fleet-infra/ -n flotte-namespace -f ./infra/helm/fleet-infra/values.secret.yaml

echo "📊 Déploiement de l'observabilité..."
helm upgrade --install fleet-obs ./infra/helm/fleet-observability/ -n flotte-namespace

echo "⏳ Attente de PostgreSQL..."
kubectl wait --for=condition=ready --timeout=300s pod/postgres-flotte-service-0 -n flotte-namespace

echo "⏳ Attente de Kafka..."
kubectl wait --for=condition=ready --timeout=300s pod -l app=kafka-broker -n flotte-namespace
echo "🎸 Création des topics Kafka..."
KAFKA_POD=$(kubectl get pods -n flotte-namespace -l app=kafka-broker -o name | head -n 1)
# On cherche le binaire avec ou sans .sh
KAFKA_BIN=$(kubectl exec $KAFKA_POD -n flotte-namespace -- sh -c "find /usr/bin /opt/bitnami/kafka/bin -name kafka-topics -o -name kafka-topics.sh 2>/dev/null | head -1")

# Si on n'a rien trouvé, on tente un nom par défaut
if [ -z "$KAFKA_BIN" ]; then KAFKA_BIN="kafka-topics"; fi

echo "🔧 Utilisation de : $KAFKA_BIN"
kubectl exec $KAFKA_POD -n flotte-namespace -- $KAFKA_BIN --create --topic flotte.localisation.gps --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --if-not-exists || true
kubectl exec $KAFKA_POD -n flotte-namespace -- $KAFKA_BIN --create --topic flotte.location.events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1 --if-not-exists || true


echo "🚀 Déploiement de l'application complète via Helm..."
helm upgrade --install fleet-app ./infra/helm/fleet-app/ -n flotte-namespace

echo "🏁 Attente des déploiements applicatifs..."
kubectl rollout status deployment/keycloak-deployment -n flotte-namespace --timeout=900s
kubectl rollout status deployment/graphql-gateway-deployment -n flotte-namespace --timeout=600s
kubectl rollout status deployment/frontend-deployment -n flotte-namespace --timeout=600s

echo "♻️ Redémarrage des collecteurs (Force Refresh)..."
kubectl rollout restart daemonset promtail -n flotte-namespace
kubectl rollout restart deployment otel-collector -n flotte-namespace

echo "--------------------------------------------------"
echo "✅ Installation terminée avec succès !"
echo ""
echo "1. Lancez 'minikube tunnel' dans un terminal séparé (Windows ou Linux)."
echo "2. Ajoutez l'entrée suivante à votre fichier hosts (C:\Windows\System32\drivers\etc\hosts) :"
echo "   127.0.0.1 flotte.local"
echo ""
echo "Accès :"
echo "- Frontend : http://flotte.local"
echo "- GraphQL  : http://flotte.local/graphql"
echo "- Keycloak : http://flotte.local/auth"
echo "--------------------------------------------------"
