import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { IncomingMessage } from 'http';
import {
	DRIVER_SERVICE_URL,
	EVENTS_SERVICE_URL,
	LOCATION_SERVICE_URL,
	MAINTENANCE_SERVICE_URL,
	VEHICLE_SERVICE_URL,
} from '../config.js';

//  NOUVEAUX IMPORTS POUR LE TEMPS RÉEL
import * as path from 'path';
import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import { PubSub } from 'graphql-subscriptions';

const pubsub = new PubSub();

/** Réessaie les erreurs réseau transitoires (backends pas encore prêts au démarrage K8s). */
function attachTransientNetworkRetry(http: AxiosInstance, maxRetries = 4): void {
  http.interceptors.response.use(
    (r) => r,
    async (error: AxiosError) => {
      const cfg = error.config as (InternalAxiosRequestConfig & { __retryCount?: number }) | undefined;
      if (!cfg) throw error;
      const code = error.code;
      const transient =
        code === 'ECONNREFUSED' ||
        code === 'ECONNRESET' ||
        code === 'ETIMEDOUT' ||
        code === 'EAI_AGAIN' ||
        (error.message?.includes('socket hang up') ?? false);
      const n = cfg.__retryCount ?? 0;
      if (!transient || n >= maxRetries) throw error;
      cfg.__retryCount = n + 1;
      const delayMs = 400 * (n + 1);
      await new Promise((resolve) => setTimeout(resolve, delayMs));
      return http.request(cfg);
    }
  );
}

export interface GraphQLContext {
  vehicle: VehicleClient;
  driver: DriverClient;
  maintenance: MaintenanceClient;
  events: EventsClient;
  location: LocationClient;
}

class BaseClient {
  protected http: AxiosInstance;
  constructor(baseURL: string, authHeader?: string) {
    this.http = axios.create({
      baseURL,
      timeout: 30_000,
      headers: authHeader ? { Authorization: authHeader } : {},
    });
    attachTransientNetworkRetry(this.http);
  }
}

class VehicleClient extends BaseClient {
  async listVehicles(params: any) {
    const { data } = await this.http.get('/vehicles', { params });
    return data;
  }
  async getVehicle(id: string) {
    const { data } = await this.http.get(`/vehicles/${id}`);
    return data;
  }
  async createVehicle(input: any) {
    const { data } = await this.http.post('/vehicles', input);
    return data;
  }
  async updateVehicle(id: string, input: any) {
    const { data } = await this.http.put(`/vehicles/${id}`, input);
    return data;
  }
  async updateVehicleStatus(id: string, status: string) {
    const { data } = await this.http.patch(`/vehicles/${id}/status`, { status });
    return data;
  }
  async deleteVehicle(id: string) {
    await this.http.delete(`/vehicles/${id}`);
    return true;
  }
  async assignVehicle(vehicleId: string, driverId: string, notes?: string) {
    const { data } = await this.http.post(`/vehicles/${vehicleId}/assignments`, { driver_id: driverId, notes, created_by: driverId });
    return data;
  }
  async unassignVehicle(vehicleId: string) {
    const { data } = await this.http.delete(`/vehicles/${vehicleId}/assignments/current`);
    return data;
  }
  async getAssignments(vehicleId: string) {
    const { data } = await this.http.get(`/vehicles/${vehicleId}/assignments`);
    return data;
  }
}

class DriverClient extends BaseClient {
  async listDrivers(params: any) {
    const { data } = await this.http.get('/drivers', { params });
    return data;
  }
  async getDriver(id: string) {
    const { data } = await this.http.get(`/drivers/${id}`);
    return data;
  }
  async createDriver(input: any) {
    const { data } = await this.http.post('/drivers', input);
    return data;
  }
  async updateDriverStatus(id: string, status: string) {
    const { data } = await this.http.patch(`/drivers/${id}/status`, { status });
    return data;
  }
  async getLicenses(driverId: string) {
    const { data } = await this.http.get(`/drivers/${driverId}/licenses`);
    return data;
  }
  async updateDriver(id: string, payload: any) {
    const { data } = await this.http.put(`/drivers/${id}`, payload);
    return data;
  }

  async deleteDriver(id: string) {
    const { data } = await this.http.delete(`/drivers/${id} `);
    return data;
  }

  async addLicenseToDriver(driverId: string, payload: any) {
    const { data } = await this.http.post(`/drivers/${driverId}/licenses`, payload);
    return data;
  }
}

class MaintenanceClient extends BaseClient {
  async listRecords(params: any) {
    const { data } = await this.http.get('/maintenance', { params });
    return data;
  }
  async getRecord(id: string) {
    const { data } = await this.http.get(`/maintenance/${id}`);
    return data;
  }
  async createRecord(input: any) {
    const { data } = await this.http.post('/maintenance', input);
    return data;
  }
  async updateStatus(id: string, input: any) {
    const { data } = await this.http.patch(`/maintenance/${id}/status`, input);
    return data;
  }
  async getVehicleHistory(vehicleId: string) {
    const { data } = await this.http.get(`/maintenance/vehicle/${vehicleId}`);
    return data;
  }

  async updateRecord(id: string, payload: any) {
    const { data } = await this.http.put(`/maintenance/${id}`, payload);
    return data;
  }
}

class EventsClient extends BaseClient {
  async listAlerts(params: any) {
    const { data } = await this.http.get('/alerts', { params });
    return data;
  }
  async getAlert(id: string) {
    const { data } = await this.http.get(`/alerts/${id}`);
    return data;
  }
  async acknowledgeAlert(id: string) {
    const { data } = await this.http.patch(`/alerts/${id}/acknowledge`);
    return data;
  }
  async resolveAlert(id: string) {
    const { data } = await this.http.patch(`/alerts/${id}/resolve`);
    return data;
  }
}

class LocationClient extends BaseClient {
  private grpcClient: any;

  constructor(baseURL: string) {
    super(baseURL); // Initialise l'appel HTTP classique (Axios)

    try {
      // 1. Calcul du chemin absolu vers le fichier .proto
      // process.cwd() correspond à la racine où tu lances ton serveur (flotte/gateway)
      const PROTO_PATH = path.resolve(process.cwd(), '../services/location-service/src/grpc/location.proto');

      // 2. Chargement du fichier
      const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
        keepCase: true,
        longs: String,
        enums: String,
        defaults: true,
        oneofs: true
      });

      // 3. Extraction du package avec ton nom précis "flotte.location.v1"
      const protoDescriptor = grpc.loadPackageDefinition(packageDefinition) as any;
      const locationProto = protoDescriptor.flotte.location.v1;

      // 4. Initialisation du client gRPC
      const grpcUrl = process.env.LOCATION_GRPC_URL || 'localhost:50051';
      this.grpcClient = new locationProto.LocationService(
          grpcUrl,
          grpc.credentials.createInsecure()
      );

      console.log("✅ Client gRPC Location initialisé avec succès !");
    } catch (error) {
      console.warn("⚠️ Impossible de charger le client gRPC Location:", error);
    }
  }

  // --- REQUÊTES REST EXISTANTES ---
  async getLatestPosition(vehicleId: string) {
    const { data } = await this.http.get(`/locations/${vehicleId}/latest`);
    return data;
  }
  async getHistory(vehicleId: string, from: string, to: string) {
    const { data } = await this.http.get(`/locations/${vehicleId}/history`, {
      params: { from, to, limit: 1000 },
    });
    return data;
  }

  // --- FLUX GRPC TEMPS RÉEL ---
  watchVehicleStream(vehicleId: string) {
    if (!this.grpcClient) {
      throw new Error("Client gRPC non initialisé");
    }

    const topic = `VEHICLE_LOCATION_${vehicleId}`;

    // Appel gRPC vers NestJS (Assure-toi que la méthode s'appelle bien WatchVehicle ou StreamPositions selon ton .proto)
    const call = this.grpcClient.WatchVehicle({ vehicle_id: vehicleId });

    call.on('data', (position: any) => {
      // Publie l'événement dans GraphQL dès qu'on le reçoit du gRPC
      pubsub.publish(topic, { vehicleLocationUpdated: position });
    });

    call.on('error', (error: any) => {
      console.error(`[gRPC] Erreur sur le flux du véhicule ${vehicleId}:`, error.message);
    });

    call.on('end', () => {
      console.log(`[gRPC] Fin du flux pour le véhicule ${vehicleId}`);
    });

    // Retourne l'itérateur (bypass du check TypeScript avec "as any")
    return (pubsub as any).asyncIterator(topic);
  }
}

export const createContext = async ({ req }: { req: IncomingMessage }): Promise<GraphQLContext> => {
  const authHeader = req.headers.authorization;
  return {
    vehicle: new VehicleClient(VEHICLE_SERVICE_URL, authHeader),
    driver: new DriverClient(DRIVER_SERVICE_URL, authHeader),
    maintenance: new MaintenanceClient(MAINTENANCE_SERVICE_URL, authHeader),
    events: new EventsClient(EVENTS_SERVICE_URL, authHeader),
    location: new LocationClient(LOCATION_SERVICE_URL),
  };
};
