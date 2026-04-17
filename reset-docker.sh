#!/bin/bash

echo "🗑️ Nettoyage complet de Docker (Volumes inclus)..."
docker compose down -v

echo "🚀 Lancement des services..."
docker compose up -d --build

echo "⏳ Attente du démarrage de Kafka et Postgres (15s)..."
sleep 15

echo "🔄 Redémarrage de la Gateway pour forcer l'abonnement Kafka..."
docker compose restart gateway

echo "✅ Tout est prêt !"
echo "👉 1. Ouvrez http://localhost:3005/location (La carte doit être VIDE)"
echo "👉 2. Lancez ./simulate.sh docker (Le camion doit apparaître et BOUGER)"
