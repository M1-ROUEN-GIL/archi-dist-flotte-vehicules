import { Entity, Column, PrimaryGeneratedColumn, CreateDateColumn, Index } from 'typeorm';

@Entity('location_readings')
@Index(['vehicleId', 'time'])
export class LocationReading {

    @PrimaryGeneratedColumn('uuid')
    id: string;

    @Column({ name: 'vehicle_id', type: 'uuid' })
    vehicleId: string;

    @Column({ name: 'driver_id', type: 'uuid', nullable: true })
    driverId: string;

    @Column({ type: 'double precision' })
    latitude: number;

    @Column({ type: 'double precision' })
    longitude: number;

    @Column({ name: 'speed_kmh', type: 'real', default: 0 })
    speedKmh: number;

    @Column({ name: 'heading_deg', type: 'real', default: 0 })
    headingDeg: number;

    @Column({ name: 'accuracy_m', type: 'real', default: 0 })
    accuracyM: number;

    @Column({ name: 'altitude_m', type: 'real', nullable: true })
    altitudeM: number;

    @Column({ type: 'varchar', length: 20, default: 'GPS_DEVICE' })
    source: string;

    @CreateDateColumn({ type: 'timestamptz', name: 'time' })
    time: Date;
}