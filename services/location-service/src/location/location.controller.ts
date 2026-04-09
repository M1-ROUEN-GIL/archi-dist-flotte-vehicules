import { Controller } from '@nestjs/common';
import { GrpcStreamCall, GrpcMethod } from '@nestjs/microservices';
import { Observable, Subject } from 'rxjs';
import { LocationService } from './location.service';

@Controller()
export class LocationController {

    constructor(private readonly locationService: LocationService) {}

    // Streaming bidirectionnel — boîtier GPS envoie des positions
    @GrpcStreamCall('LocationService', 'StreamPositions')
    async streamPositions(
        requestStream: any,
        response: Subject<any>,
    ): Promise<void> {
        requestStream.on('data', async (position: any) => {
            try {
                await this.locationService.savePosition(position);
                response.next({
                    event_id: crypto.randomUUID(),
                    accepted: true,
                });
            } catch (error) {
                response.next({
                    event_id: crypto.randomUUID(),
                    accepted: false,
                });
            }
        });

        requestStream.on('end', () => {
            response.complete();
        });

        requestStream.on('error', (error: any) => {
            response.error(error);
        });
    }

    // Streaming serveur — s'abonner aux positions d'un véhicule
    @GrpcMethod('LocationService', 'WatchVehicle')
    watchVehicle(request: { vehicle_id: string }): Observable<any> {
        return this.locationService.watchVehicle(request.vehicle_id);
    }
}