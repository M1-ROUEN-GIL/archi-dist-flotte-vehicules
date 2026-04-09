import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { LocationModule } from './location/location.module';
import { LocationReading } from './location/location.entity';

@Module({
  imports: [
    TypeOrmModule.forRoot({
      type: 'postgres', // TimescaleDB est une extension Postgres !
      host: process.env.DB_HOST || 'localhost',
      port: parseInt(process.env.DB_PORT || '5432', 10),      username: process.env.DB_USER || 'postgres',
      password: process.env.DB_PASSWORD || 'postgres',
      database: process.env.DB_NAME || 'flotte',
      entities: [LocationReading],
      // ⚠️ TRÈS IMPORTANT : false avec TimescaleDB.
      // NestJS ne sait pas créer des Hypertables, on le fera à la main via SQL.
      synchronize: false,
    }),
    LocationModule,
  ],
})
export class AppModule {}