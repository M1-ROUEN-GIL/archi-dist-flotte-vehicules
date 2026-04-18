import './tracing';
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { MicroserviceOptions, Transport } from '@nestjs/microservices';
import { join } from 'path';

async function bootstrap() {
  // 1. Initialiser l'application classique (HTTP pour REST/GraphQL)
  const app = await NestFactory.create(AppModule);

  // 2. Ajouter la couche gRPC
  app.connectMicroservice<MicroserviceOptions>({
    transport: Transport.GRPC,
    options: {
      package: 'flotte.location.v1',
      // Attention au chemin : pointe vers le bon dossier proto
      protoPath: join(__dirname, 'proto/location.proto'),
      url: '0.0.0.0:50051',
      loader: {
        keepCase: true,  // ← ajouter ça
      },
    },
  });

  // 3. Démarrer les microservices (gRPC) PUIS le serveur HTTP
  await app.startAllMicroservices();
  await app.listen(3000);

  console.log('🚀 Service Localisation démarré !');
  console.log('📡 gRPC écoute sur le port 50051');
  console.log('🌐 HTTP API écoute sur le port 3000');
}
bootstrap();