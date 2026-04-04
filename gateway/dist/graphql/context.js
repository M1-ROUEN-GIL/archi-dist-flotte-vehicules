import axios from 'axios';
const VEHICLE_SERVICE_URL = process.env.VEHICLE_SERVICE_URL || 'http://vehicle-service:8080';
const DRIVER_SERVICE_URL = process.env.DRIVER_SERVICE_URL || 'http://driver-service:8080';
const MAINTENANCE_SERVICE_URL = process.env.MAINTENANCE_SERVICE_URL || 'http://maintenance-service:8080';
/** Réessaie les erreurs réseau transitoires (backends pas encore prêts au démarrage K8s). */
function attachTransientNetworkRetry(http, maxRetries = 4) {
    http.interceptors.response.use((r) => r, async (error) => {
        const cfg = error.config;
        if (!cfg)
            throw error;
        const code = error.code;
        const transient = code === 'ECONNREFUSED' ||
            code === 'ECONNRESET' ||
            code === 'ETIMEDOUT' ||
            code === 'EAI_AGAIN' ||
            (error.message?.includes('socket hang up') ?? false);
        const n = cfg.__retryCount ?? 0;
        if (!transient || n >= maxRetries)
            throw error;
        cfg.__retryCount = n + 1;
        const delayMs = 400 * (n + 1);
        await new Promise((resolve) => setTimeout(resolve, delayMs));
        return http.request(cfg);
    });
}
class BaseClient {
    http;
    constructor(baseURL, authHeader) {
        this.http = axios.create({
            baseURL,
            timeout: 30_000,
            headers: authHeader ? { Authorization: authHeader } : {},
        });
        attachTransientNetworkRetry(this.http);
    }
}
class VehicleClient extends BaseClient {
    async listVehicles(params) {
        const { data } = await this.http.get('/vehicles', { params });
        return data;
    }
    async getVehicle(id) {
        const { data } = await this.http.get(`/vehicles/${id}`);
        return data;
    }
    async createVehicle(input) {
        const { data } = await this.http.post('/vehicles', input);
        return data;
    }
    async updateVehicle(id, input) {
        const { data } = await this.http.put(`/vehicles/${id}`, input);
        return data;
    }
    async updateVehicleStatus(id, status) {
        const { data } = await this.http.patch(`/vehicles/${id}/status`, { status });
        return data;
    }
    async deleteVehicle(id) {
        await this.http.delete(`/vehicles/${id}`);
        return true;
    }
    async assignVehicle(vehicleId, driverId, notes) {
        const { data } = await this.http.post(`/vehicles/${vehicleId}/assignments`, { driver_id: driverId, notes });
        return data;
    }
    async unassignVehicle(vehicleId) {
        const { data } = await this.http.delete(`/vehicles/${vehicleId}/assignments/current`);
        return data;
    }
    async getAssignments(vehicleId) {
        const { data } = await this.http.get(`/vehicles/${vehicleId}/assignments`);
        return data;
    }
}
class DriverClient extends BaseClient {
    async listDrivers(params) {
        const { data } = await this.http.get('/drivers', { params });
        return data;
    }
    async getDriver(id) {
        const { data } = await this.http.get(`/drivers/${id}`);
        return data;
    }
    async createDriver(input) {
        const { data } = await this.http.post('/drivers', input);
        return data;
    }
    async updateDriverStatus(id, status) {
        const { data } = await this.http.patch(`/drivers/${id}/status`, { status });
        return data;
    }
    async getLicenses(driverId) {
        const { data } = await this.http.get(`/drivers/${driverId}/licenses`);
        return data;
    }
}
class MaintenanceClient extends BaseClient {
    async listRecords(params) {
        const { data } = await this.http.get('/maintenance', { params });
        return data;
    }
    async getRecord(id) {
        const { data } = await this.http.get(`/maintenance/${id}`);
        return data;
    }
    async createRecord(input) {
        const { data } = await this.http.post('/maintenance', input);
        return data;
    }
    async updateStatus(id, input) {
        const { data } = await this.http.patch(`/maintenance/${id}/status`, input);
        return data;
    }
}
export const createContext = async ({ req }) => {
    const authHeader = req.headers.authorization;
    return {
        vehicle: new VehicleClient(VEHICLE_SERVICE_URL, authHeader),
        driver: new DriverClient(DRIVER_SERVICE_URL, authHeader),
        maintenance: new MaintenanceClient(MAINTENANCE_SERVICE_URL, authHeader),
    };
};
