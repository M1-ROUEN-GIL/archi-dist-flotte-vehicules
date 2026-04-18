#!/bin/bash
cd "$(dirname "$0")/.."

# Script de simulation de flotte universel (Docker / Minikube)
echo "--------------------------------------------------------"
echo "🚛 SIMULATEUR DE FLOTTE EN TEMPS RÉEL"
echo "--------------------------------------------------------"

# 1. Vérification et Détection de l'environnement
if [ "$1" == "docker" ]; then
    ENV="docker"
elif [ "$1" == "minikube" ]; then
    ENV="minikube"
else
    echo "❌ Erreur : Vous devez spécifier un environnement."
    echo "👉 Usage : ./simulate.sh [docker|minikube]"
    echo ""
    echo "Exemple :"
    echo "  ./simulate.sh docker    <- Pour tester sur Docker (localhost:3005)"
    echo "  ./simulate.sh minikube  <- Pour tester sur Minikube (flotte.local)"
    exit 1
fi

echo "🌐 Environnement cible : $ENV"

# 2. Vérification des dépendances (installation locale au service si besoin)
if [ ! -d "node_modules/@grpc/grpc-js" ]; then
    echo "📦 Installation des dépendances gRPC à la racine..."
    npm install @grpc/grpc-js @grpc/proto-loader
fi

# 3. Configuration de l'accès
if [ "$ENV" == "docker" ]; then
    if ! docker ps | grep -q "flotte-location"; then
        echo "❌ Erreur : Le conteneur Docker 'flotte-location' n'est pas lancé."
        echo "👉 Lancez : docker compose up -d location"
        exit 1
    fi
    export LOCATION_GRPC_URL="localhost:50051"
    echo "✅ Cible Docker détectée sur $LOCATION_GRPC_URL"
else
    echo "🔍 Vérification de Minikube..."
    if ! kubectl get svc -n flotte-namespace | grep -q "location-service"; then
        echo "❌ Erreur : Le service 'location-service' n'est pas déployé dans Minikube."
        exit 1
    fi
    
    echo "🔌 Ouverture d'un tunnel temporaire vers Minikube (Port-Forward)..."
    # On lance le port-forward en arrière-plan
    kubectl port-forward svc/location-service 50051:50051 -n flotte-namespace &
    PF_PID=$!
    
    # On laisse un peu de temps au tunnel pour s'ouvrir
    sleep 2
    export LOCATION_GRPC_URL="localhost:50051"
    
    # On s'assure de tuer le tunnel à la fin du script
    trap "kill $PF_PID" EXIT
    echo "✅ Tunnel gRPC ouvert sur $LOCATION_GRPC_URL (PID: $PF_PID)"
fi

# 4. Définition du chemin du Proto (le script officiel le cherche par défaut dans ../grpc/)
# On s'assure qu'il le trouve bien
export PROTO_PATH="$(pwd)/services/location-service/src/grpc/location.proto"

echo "--------------------------------------------------------"
echo "🚀 Lancement de la simulation officielle..."
echo "📍 Trajet : Départ de Rouen !"
echo "--------------------------------------------------------"

node services/location-service/src/simulation/simulator.js
