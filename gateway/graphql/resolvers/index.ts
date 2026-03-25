import { stubResolvers } from './stubResolvers.js';
import { vehicleResolvers } from './vehicleResolvers.js';

export const resolvers = {
  Query: {
    ...stubResolvers.Query,
    ...vehicleResolvers.Query,
  },
  Mutation: {
    ...stubResolvers.Mutation,
    ...vehicleResolvers.Mutation,
  },
  Subscription: {
    ...stubResolvers.Subscription,
  },
  Vehicle: vehicleResolvers.Vehicle,
  Assignment: vehicleResolvers.Assignment,
  Driver: vehicleResolvers.Driver,
};
