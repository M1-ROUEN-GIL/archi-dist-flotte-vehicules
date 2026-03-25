import { GraphQLError } from 'graphql';

function notImplemented(field: string): never {
  throw new GraphQLError(
    `« ${field} » : pas encore relié au microservice (voir resolvers).`,
    { extensions: { code: 'NOT_IMPLEMENTED' } },
  );
}

function subscriptionDisabled(name: string): never {
  throw new GraphQLError(
    `Subscription « ${name} » : non activée sur ce gateway (WebSocket / graphql-ws à brancher).`,
    { extensions: { code: 'SUBSCRIPTIONS_DISABLED' } },
  );
}

export const stubResolvers = {
  Query: {
    drivers: () => notImplemented('drivers'),
    driver: () => notImplemented('driver'),
    alerts: () => notImplemented('alerts'),
    alert: () => notImplemented('alert'),
    vehicleLocation: () => notImplemented('vehicleLocation'),
    locationHistory: () => notImplemented('locationHistory'),
    maintenanceRecords: () => notImplemented('maintenanceRecords'),
    maintenanceRecord: () => notImplemented('maintenanceRecord'),
  },
  Mutation: {
    createDriver: () => notImplemented('createDriver'),
    updateDriverStatus: () => notImplemented('updateDriverStatus'),
    acknowledgeAlert: () => notImplemented('acknowledgeAlert'),
    resolveAlert: () => notImplemented('resolveAlert'),
    createMaintenanceRecord: () => notImplemented('createMaintenanceRecord'),
    updateMaintenanceStatus: () => notImplemented('updateMaintenanceStatus'),
  },
  Subscription: {
    vehicleStatusChanged: {
      subscribe: () => subscriptionDisabled('vehicleStatusChanged'),
    },
    vehicleLocationUpdated: {
      subscribe: () => subscriptionDisabled('vehicleLocationUpdated'),
    },
    alertCreated: {
      subscribe: () => subscriptionDisabled('alertCreated'),
    },
    alertCreatedBySeverity: {
      subscribe: () => subscriptionDisabled('alertCreatedBySeverity'),
    },
  },
};
