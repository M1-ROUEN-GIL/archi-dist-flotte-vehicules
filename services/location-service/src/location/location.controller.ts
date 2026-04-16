import { Controller } from '@nestjs/common';
import { GrpcStreamCall, GrpcMethod, GrpcStreamMethod } from '@nestjs/microservices';
import { Observable, Subject } from 'rxjs';
import { LocationService } from './location.service';
import {randomUUID} from "node:crypto";

@Controller()
export class LocationController {

    constructor(private readonly locationService: LocationService) {}

    // Streaming bidirectionnel — boîtier GPS envoie des positions
    @GrpcStreamCall('LocationService', 'StreamPositions')
    streamPositions(requestStream: any) {
        // On ne renvoie rien au simulateur pour l'instant, on veut juste stocker !
        requestStream.on('data', async (data: any) => {
            try {
                // Nettoyage rapide de la date si nécessaire
                const position = {
                    ...data,
                    time: new Date().toISOString()
                };
                await this.locationService.savePosition(position);
                console.log(`📍 Position enregistrée pour ${data.vehicle_id}`);
            } catch (e) {
                console.error("Erreur de sauvegarde interne", e.message);
            }
        });

        requestStream.on('error', (err) => console.error('Flux Error:', err));
        requestStream.on('end', () => requestStream.removeAllListeners());
    }

    // Streaming serveur — s'abonner aux positions d'un véhicule
    @GrpcStreamMethod('LocationService', 'WatchVehicle')
    watchVehicle(request: { vehicle_id: string }): Observable<any> {
        return this.locationService.watchVehicle(request.vehicle_id);
    }
}