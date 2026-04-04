import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { IncomingMessage } from 'http';
import {
	DRIVER_SERVICE_URL,
	MAINTENANCE_SERVICE_URL,
	VEHICLE_SERVICE_URL,
} from '../config.js';

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
    const { data } = await this.http.post(`/vehicles/${vehicleId}/assignments`, { driver_id: driverId, notes });
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
}

export const createContext = async ({ req }: { req: IncomingMessage }): Promise<GraphQLContext> => {
  const authHeader = req.headers.authorization;
  return {
    vehicle: new VehicleClient(VEHICLE_SERVICE_URL, authHeader),
    driver: new DriverClient(DRIVER_SERVICE_URL, authHeader),
    maintenance: new MaintenanceClient(MAINTENANCE_SERVICE_URL, authHeader),
  };
};
