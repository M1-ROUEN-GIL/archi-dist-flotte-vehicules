import { GraphQLError } from 'graphql';
import { Kafka } from 'kafkajs';
import { EventEmitter, on } from 'events'; // 👈 On utilise le natif de Node.js !

import { vehicleResolvers } from './vehicleResolvers.js';
import { maintenanceResolvers } from './maintenanceResolvers.js';
import { driverResolvers } from './driverResolvers.js';
import { alertResolvers } from './alertResolvers.js';
import { locationResolvers } from './locationResolvers.js';

// 1. Notre tuyau de communication temps réel 100% robuste
const ee = new EventEmitter();

// 2. Connecter la Gateway à Kafka
const kafka = new Kafka({
  clientId: 'api-gateway',
  brokers: [process.env.KAFKA_BROKER || 'kafka:9092']
});
const consumer = kafka.consumer({ groupId: 'gateway-websockets-group' });

const startKafkaConsumer = async () => {
  let subscribed = false;
  while (!subscribed) {
    try {
      await consumer.connect();
      await consumer.subscribe({ topic: 'flotte.localisation.gps', fromBeginning: false });
      subscribed = true;
      console.log('✅ Gateway : Écouteur Kafka branché sur flotte.localisation.gps !');
    } catch (err) {
      console.error('⚠️ Attente du topic Kafka flotte.localisation.gps (nouvel essai dans 5s)...');
      await new Promise(resolve => setTimeout(resolve, 5000));
    }
  }

  try {
    await consumer.run({
      eachMessage: async ({ message }) => {
        if (message.value) {
          const data = JSON.parse(message.value.toString());

          // 🛡️ FILET DE SÉCURITÉ : On s'adapte aux différents noms de variables possibles
          const positionPourReact = {
            ...data.payload,
            latitude: data.payload.latitude ?? data.payload.lat,
            longitude: data.payload.longitude ?? data.payload.lng ?? data.payload.lon,
            speed_kmh: data.payload.speed_kmh ?? data.payload.speed ?? 0,
            heading_deg: data.payload.heading_deg ?? data.payload.heading ?? 0,
            recorded_at: data.timestamp
          };

          // Le nouveau mouchard pour voir exactement ce qui passe
          console.log("🔍 DONNÉES ENVOYÉES À REACT :", positionPourReact);

          // On vérifie que la latitude existe bien avant d'envoyer
          if (positionPourReact.latitude !== undefined && positionPourReact.latitude !== null) {
            ee.emit('VEHICLE_LOCATION_UPDATED', { vehicleLocationUpdated: positionPourReact });
          } else {
            console.warn("⚠️ Point GPS ignoré (Latitude manquante) :", data.payload);
          }
        }
      },
    });
    console.log('✅ Gateway : Écouteur Kafka branché sur flotte.localisation.gps !');
  } catch (err) {
    console.error('❌ Erreur de connexion Kafka sur la Gateway:', err);
  }
};
startKafkaConsumer();

// 4. Les Resolvers
export const resolvers = {
  Query: {
    ...vehicleResolvers.Query,
    ...maintenanceResolvers.Query,
    ...driverResolvers.Query,
    ...alertResolvers.Query,
    ...locationResolvers.Query,
  },
  Mutation: {
    ...vehicleResolvers.Mutation,
    ...maintenanceResolvers.Mutation,
    ...driverResolvers.Mutation,
    ...alertResolvers.Mutation,
  },
  Subscription: {
    vehicleLocationUpdated: {
      subscribe: async function* (_: any, args: { vehicle_id: string }) {
        for await (const [payload] of on(ee, 'VEHICLE_LOCATION_UPDATED')) {
          // 🎯 TRÈS IMPORTANT : On ne transmet au navigateur QUE les infos du camion sélectionné !
          if (payload.vehicleLocationUpdated.vehicle_id === args.vehicle_id) {
            yield payload;
          }
        }
      },
    },
    // Bouchons silencieux pour éviter les crashs sur les autres écrans
    vehicleStatusChanged: {
      subscribe: async function* () { for await (const [payload] of on(ee, 'VEHICLE_STATUS_CHANGED')) yield payload; }
    },
    alertCreated: {
      subscribe: async function* () { for await (const [payload] of on(ee, 'ALERT_CREATED')) yield payload; }
    },
    alertCreatedBySeverity: {
      subscribe: async function* () { for await (const [payload] of on(ee, 'ALERT_CREATED_SEVERITY')) yield payload; }
    },
  },
  Vehicle: vehicleResolvers.Vehicle,
  MaintenanceRecord: maintenanceResolvers.MaintenanceRecord,
  Assignment: vehicleResolvers.Assignment,
  Alert: alertResolvers.Alert,
  Driver: {
    ...vehicleResolvers.Driver,
    ...driverResolvers.Driver,
  },
};