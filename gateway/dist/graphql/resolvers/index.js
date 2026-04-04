import { stubResolvers } from './stubResolvers.js';
import { vehicleResolvers } from './vehicleResolvers.js';
import { maintenanceResolvers } from './maintenanceResolvers.js';
import { driverResolvers } from './driverResolvers.js';
export const resolvers = {
    Query: {
        ...stubResolvers.Query,
        ...vehicleResolvers.Query,
        ...maintenanceResolvers.Query,
        ...driverResolvers.Query,
    },
    Mutation: {
        ...stubResolvers.Mutation,
        ...vehicleResolvers.Mutation,
        ...maintenanceResolvers.Mutation,
        ...driverResolvers.Mutation,
    },
    Subscription: {
        ...stubResolvers.Subscription,
    },
    Vehicle: vehicleResolvers.Vehicle,
    MaintenanceRecord: maintenanceResolvers.MaintenanceRecord,
    Assignment: vehicleResolvers.Assignment,
    Driver: {
        ...vehicleResolvers.Driver,
        ...driverResolvers.Driver,
    },
};
