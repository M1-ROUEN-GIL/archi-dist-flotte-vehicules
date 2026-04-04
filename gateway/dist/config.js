import { existsSync } from 'fs';
/** Présent dans les conteneurs Linux — absent si tu lances le gateway avec `npm` sur la machine hôte. */
export const runningInDocker = existsSync('/.dockerenv');
/**
 * URLs des microservices.
 * - Dans Docker Compose : noms de service `vehicle`, `driver`, `maintenance`.
 * - Sur l’hôte (npm run) : ports publiés par compose (voir compose.yaml).
 */
export const VEHICLE_SERVICE_URL = process.env.VEHICLE_SERVICE_URL ??
    (runningInDocker ? 'http://vehicle:8080' : 'http://127.0.0.1:8080');
export const DRIVER_SERVICE_URL = process.env.DRIVER_SERVICE_URL ?? (runningInDocker ? 'http://driver:8080' : 'http://127.0.0.1:8081');
export const MAINTENANCE_SERVICE_URL = process.env.MAINTENANCE_SERVICE_URL ??
    (runningInDocker ? 'http://maintenance:8080' : 'http://127.0.0.1:8082');
