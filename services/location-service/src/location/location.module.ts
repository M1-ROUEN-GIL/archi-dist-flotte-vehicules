import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { LocationService } from './location.service';
import { LocationController } from './location.controller';
import { LocationRestController } from '../rest/location.rest.controller';
import { LocationReading } from './location.entity';
import { KafkaProducer } from '../kafka/kafka.producer';

@Module({
    // On importe l'entité pour que TypeORM crée le Repository
    imports: [TypeOrmModule.forFeature([LocationReading])],
    // On déclare tes deux points d'entrée (gRPC et HTTP)
    controllers: [LocationController, LocationRestController],
    // On fournit tes services métier
    providers: [LocationService, KafkaProducer],
})
export class LocationModule {}