import { stubResolvers } from './stubResolvers.js';
import { vehicleResolvers } from './vehicleResolvers.js';
import { maintenanceResolvers } from './maintenanceResolvers.js';

export const resolvers = {
  Query: {
    ...stubResolvers.Query,
    ...vehicleResolvers.Query,
    ...maintenanceResolvers.Query,
  },
  Mutation: {
    ...stubResolvers.Mutation,
    ...vehicleResolvers.Mutation,
    ...maintenanceResolvers.Mutation,
  },
  Subscription: {
    ...stubResolvers.Subscription,
  },
  Vehicle: vehicleResolvers.Vehicle,
  MaintenanceRecord: maintenanceResolvers.MaintenanceRecord,
  Assignment: vehicleResolvers.Assignment,
  Driver: vehicleResolvers.Driver,
};
