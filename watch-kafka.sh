#!/bin/bash

# Configuration
NAMESPACE="flotte-namespace"
SELECTOR="app=kafka-broker"

echo "🔍 Recherche du Pod Kafka dans le namespace : $NAMESPACE..."

KAFKA_POD=$(kubectl get pods -n $NAMESPACE -l $SELECTOR -o jsonpath='{.items[0].metadata.name}' 2>/dev/null)

if [ -z "$KAFKA_POD" ]; then
    echo "❌ Erreur : Impossible de trouver le Pod Kafka."
    exit 1
fi

echo "✅ Pod trouvé : $KAFKA_POD"
echo "🚀 Écoute des nouveaux événements (Temps Réel)..."
echo "💡 (Note : L'historique est masqué. Fais une action dans Postman pour voir un event)"
echo "--------------------------------------------------------------------------------"

# Commande Kafka simplifiée et propre
# On enlève --from-beginning pour ne voir que le présent
kubectl exec -it $KAFKA_POD -n $NAMESPACE -- \
  kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --include "flotte\..*" \
  --property print.topic=true \
  --property print.timestamp=true \
  --property separator=" | "
