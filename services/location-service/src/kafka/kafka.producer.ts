import { Injectable, OnModuleInit, OnModuleDestroy, Logger } from '@nestjs/common';
import { Kafka, Producer } from 'kafkajs';

@Injectable()
export class KafkaProducer implements OnModuleInit, OnModuleDestroy {

    private readonly logger = new Logger(KafkaProducer.name);
    private producer: Producer;

    async onModuleInit() {
        const kafka = new Kafka({
            clientId: 'location-service',
            brokers: [process.env.KAFKA_BROKER || 'localhost:9092'],
        });

        this.producer = kafka.producer();
        await this.producer.connect();
        this.logger.log('Kafka producer connecté');
    }

    async onModuleDestroy() {
        await this.producer.disconnect();
    }

    async publishGpsPosition(position: any) {
        try {
            await this.producer.send({
                topic: 'flotte.localisation.gps',
                messages: [{
                    key: position.vehicle_id,
                    value: JSON.stringify({
                        event_id:      crypto.randomUUID(),
                        event_type:    'GPS_POSITION_UPDATED',
                        event_version: '1.0',
                        timestamp:     new Date().toISOString(),
                        payload: {
                            vehicle_id:  position.vehicle_id,
                            latitude:    position.latitude,
                            longitude:   position.longitude,
                            speed_kmh:   position.speed_kmh,
                            heading_deg: position.heading_deg,
                            accuracy_m:  position.accuracy_m,
                            source:      position.source,
                        },
                        metadata: {
                            correlation_id: crypto.randomUUID(),
                        },
                    }),
                }],
            });
        } catch (error) {
            this.logger.error(`Erreur publication Kafka : ${error.message}`);
        }
    }
}