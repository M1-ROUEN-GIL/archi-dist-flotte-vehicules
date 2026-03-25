import { GraphQLError } from 'graphql';
import type { GraphQLContext } from '../context.js';

export const vehicleResolvers = {
  Query: {
    vehicles: async (
      _: unknown,
      args: {
        status?: string | null;
        limit?: number | null;
        offset?: number | null;
      },
      ctx: GraphQLContext,
    ) => {
      return ctx.vehicle.listVehicles({
        status: args.status,
        limit: args.limit ?? 20,
        offset: args.offset ?? 0,
      });
    },

    vehicle: async (_: unknown, args: { id: string }, ctx: GraphQLContext) => {
      return ctx.vehicle.getVehicle(args.id);
    },

    vehicleAssignments: async (
      _: unknown,
      args: { vehicle_id: string },
      ctx: GraphQLContext,
    ) => {
      return ctx.vehicle.getAssignments(args.vehicle_id);
    },
  },

  Mutation: {
    createVehicle: async (
      _: unknown,
      args: {
        plate_number: string;
        brand: string;
        model: string;
        fuel_type: string;
        mileage_km: number;
        vin?: string | null;
        color?: string | null;
      },
      ctx: GraphQLContext,
    ) => {
      return ctx.vehicle.createVehicle(args);
    },

    updateVehicleStatus: async (
      _: unknown,
      args: { id: string; status: string },
      ctx: GraphQLContext,
    ) => {
      return ctx.vehicle.updateVehicleStatus(args.id, args.status);
    },

    assignVehicle: async (
      _: unknown,
      args: {
        vehicle_id: string;
        driver_id: string;
        notes?: string | null;
      },
      ctx: GraphQLContext,
    ) => {
      return ctx.vehicle.assignVehicle(
        args.vehicle_id,
        args.driver_id,
        args.notes,
      );
    },

    unassignVehicle: async (
      _: unknown,
      args: { vehicle_id: string },
      ctx: GraphQLContext,
    ) => {
      return ctx.vehicle.unassignVehicle(args.vehicle_id);
    },

    updateVehicle: async (
      _: unknown,
      args: {
        id: string;
        brand?: string | null;
        model?: string | null;
        mileage_km?: number | null;
        vin?: string | null;
        color?: string | null;
      },
      ctx: GraphQLContext,
    ) => {
      return ctx.vehicle.updateVehicle(args.id, args);
    },

    deleteVehicle: async (
      _: unknown,
      args: { id: string },
      ctx: GraphQLContext,
    ) => {
      await ctx.vehicle.deleteVehicle(args.id);
      return true;
    },
    
  },

  Vehicle: {
    current_location: () => null,
    current_assignment: () => null,
    maintenance_records: () => [],
  },

  Assignment: {
    vehicle: async (
      parent: { vehicle_id?: string },
      _: unknown,
      ctx: GraphQLContext,
    ) => {
      const id = parent.vehicle_id;
      if (!id)
        throw new GraphQLError('Assignment sans vehicle_id', {
          extensions: { code: 'INVALID_UPSTREAM' },
        });
      const v = await ctx.vehicle.getVehicle(String(id));
      if (!v)
        throw new GraphQLError(`Véhicule ${id} introuvable`, {
          extensions: { code: 'NOT_FOUND' },
        });
      return v;
    },
    driver: async (
      parent: { driver_id?: string },
      _: unknown,
      ctx: GraphQLContext,
    ) => {
      const id = parent.driver_id;
      if (!id)
        throw new GraphQLError('Assignment sans driver_id', {
          extensions: { code: 'INVALID_UPSTREAM' },
        });
      const d = await ctx.driver.getDriver(String(id));
      if (!d)
        throw new GraphQLError(`Conducteur ${id} introuvable`, {
          extensions: { code: 'NOT_FOUND' },
        });
      return d;
    },
  },

  Driver: {
    license: () => null,
    current_assignment: () => null,
  },

};
