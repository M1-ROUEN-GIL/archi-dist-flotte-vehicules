const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');
const path = require('path');

// 1. Charger le contrat (le fichier .proto)
const PROTO_PATH = path.join(__dirname, '../src/grpc/location.proto');
const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
    keepCase: true, // Très important pour garder les underscores (vehicle_id)
    longs: String,
    enums: String,
    defaults: true,
    oneofs: true,
});

const protoDescriptor = grpc.loadPackageDefinition(packageDefinition);
const locationService = protoDescriptor.flotte.localisation.v1.LocationService;

// 2. Créer le client gRPC (Connexion au serveur NestJS)
const client = new locationService(
    'location:50051',
    grpc.credentials.createInsecure() // Pas de SSL pour les tests en local
);

// Variables du camion fantôme (Départ de Rouen 📍)
const VEHICLE_ID = 'CAMION-TEST-76';
const DRIVER_ID = 'CHAUFFEUR-001';
let currentLat = 49.4432; // Rouen - Centre
let currentLon = 1.0999;

console.log(`🚛 Démarrage du simulateur pour ${VEHICLE_ID}...`);

// 3. Ouvrir le canal bidirectionnel (StreamPositions)
const call = client.StreamPositions();

// Écouter les accusés de réception du serveur
call.on('data', (ack) => {
    console.log(`✅ Serveur a accepté le point (Event ID: ${ack.event_id})`);
});

call.on('error', (err) => {
    console.error('❌ Erreur de connexion au serveur :', err.message);
    clearInterval(interval);
});

call.on('end', () => {
    console.log('🔌 Le serveur a fermé la connexion.');
    clearInterval(interval);
});

// 4. Boucle d'envoi des positions toutes les 3 secondes
const interval = setInterval(() => {
    // Simuler un déplacement vers le sud (Direction Évreux/Paris)
    currentLat -= 0.0005 + (Math.random() * 0.0002);
    currentLon += 0.0002 + (Math.random() * 0.0001);

    const position = {
        vehicle_id: VEHICLE_ID,
        driver_id: DRIVER_ID,
        latitude: currentLat,
        longitude: currentLon,
        speed_kmh: 85 + (Math.random() * 10), // Vitesse entre 85 et 95 km/h
        heading_deg: 180, // Cap au sud
        accuracy_m: 5,
        altitude_m: 25,
        source: 'SIMULATOR',
        timestamp: Date.now().toString(), // Les int64 protobuf sont souvent gérés comme des strings en JS
    };

    console.log(`📡 Envoi position : Lat ${position.latitude.toFixed(4)}, Lon ${position.longitude.toFixed(4)}...`);

    // Pousser la donnée dans le tuyau gRPC
    call.write(position);
}, 3000); // 3000 ms = 3 secondes

// Gérer l'arrêt propre du script (Ctrl+C)
process.on('SIGINT', () => {
    console.log('\n🛑 Arrêt du simulateur...');
    clearInterval(interval);
    call.end(); // Fermer le tuyau
    process.exit(0);
});