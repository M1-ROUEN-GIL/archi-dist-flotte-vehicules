import {Injectable} from "@nestjs/common";
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import {Observable, Subject} from "rxjs";
import {LocationReading} from "./location.entity";
import { KafkaProducer } from '../kafka/kafka.producer';
import { DetectionService } from '../detection/detection.service';


@Injectable()
export class LocationService {
    // Map des subjects par vehicleId pour le WatchVehicle
    private vehicleStreams = new Map<string, Subject<any>>();

    constructor(
        @InjectRepository(LocationReading)
        private readonly locationRepo: Repository<LocationReading>,
        private readonly kafkaProducer: KafkaProducer,
        private readonly detectionService: DetectionService,
    ){}

    async savePosition(position : any): Promise<void>{
        const reading = this.locationRepo.create({
            vehicleId:  position.vehicle_id,
            driverId:   position.driver_id || null,
            latitude:   position.latitude,
            longitude:  position.longitude,
            speedKmh:   position.speed_kmh,
            headingDeg: position.heading_deg,
            accuracyM:  position.accuracy_m,
            altitudeM:  position.altitude_m,
            source:     position.source || 'GPS_DEVICE',
            time: new Date(Number(position.timestamp)),
        });

        await this.locationRepo.save(reading);

        // Publier sur Kafka + analyser pour détecter des événements métier
        await Promise.all([
            this.kafkaProducer.publishGpsPosition(position),
            this.detectionService.analyzePosition(position),
        ]);

        // Notifier les subscribers WatchVehicle
        const stream = this.vehicleStreams.get(position.vehicle_id);
        if (stream) {
            stream.next(position);
        }
    }
    watchVehicle(vehicleId: string): Observable<any> {
        // 1. On essaie de récupérer le stream existant
        let stream = this.vehicleStreams.get(vehicleId);

        // 2. S'il n'existe pas encore, on le crée et on l'ajoute à la Map
        if (!stream) {
            stream = new Subject<any>();
            this.vehicleStreams.set(vehicleId, stream);
        }

        // 3. Ici, TypeScript est 100% sûr que 'stream' existe, l'erreur disparaît !
        return stream.asObservable();
    }

    async getLatestPosition(vehicleId: string) {
        return this.locationRepo.findOne({
            where: { vehicleId },
            order: { time: 'DESC' },
        });
    }

    async getHistory(vehicleId: string, from: Date, to: Date, limit: number, offset: number) {
        return this.locationRepo
            .createQueryBuilder('l')
            .where('l.vehicleId = :vehicleId', { vehicleId })
            .andWhere('l.time BETWEEN :from AND :to', { from, to })
            .orderBy('l.time', 'DESC')
            .limit(limit)
            .offset(offset)
            .getMany();
    }

    async getStats(vehicleId: string, from: Date, to: Date) {
        return this.locationRepo
            .createQueryBuilder('l')
            .select('COUNT(*)', 'total_readings')
            .addSelect('AVG(l.speedKmh)', 'avg_speed_kmh')
            .addSelect('MAX(l.speedKmh)', 'max_speed_kmh')
            .where('l.vehicleId = :vehicleId', { vehicleId })
            .andWhere('l.time BETWEEN :from AND :to', { from, to })
            .getRawOne();
    }
}