import { GraphQLError } from 'graphql';
import type { GraphQLContext } from '../context.js';

export const maintenanceResolvers = {
  Query: {
    maintenanceRecords: async (
      _: unknown,
      args: {
        vehicle_id?: string | null;
        status?: string | null;
        priority?: string | null;
        limit?: number | null;
        offset?: number | null;
      },
      ctx: GraphQLContext,
    ) => {
      const records = await ctx.maintenance.listRecords({
        vehicleId: args.vehicle_id,
        status: args.status,
        priority: args.priority
      });
      
      // Handle pagination locally for now if needed, or just return as a Page
      const limit = args.limit ?? 20;
      const offset = args.offset ?? 0;
      const paginated = records.slice(offset, offset + limit);

      return {
        items: paginated,
        total_count: records.length,
      };
    },

    maintenanceRecord: async (
      _: unknown,
      args: { id: string },
      ctx: GraphQLContext,
    ) => {
      return ctx.maintenance.getRecord(args.id);
    },
  },

  Mutation: {
    createMaintenanceRecord: async (
      _: unknown,
      args: {
        vehicle_id: string;
        type: string;
        priority?: string | null;
        scheduled_date: string;
        description?: string | null;
      },
      ctx: GraphQLContext,
    ) => {
      return ctx.maintenance.createRecord(args);
    },

    updateMaintenanceStatus: async (
      _: unknown,
      args: {
        id: string;
        status: string;
        cost_eur?: number | null;
        notes?: string | null;
      },
      ctx: GraphQLContext,
    ) => {
      return ctx.maintenance.updateStatus(args.id, args);
    },
  },

  MaintenanceRecord: {
    vehicle: async (
      parent: { vehicle_id?: string },
      _: unknown,
      ctx: GraphQLContext,
    ) => {
      const id = parent.vehicle_id;
      if (!id) throw new GraphQLError('MaintenanceRecord sans vehicle_id');
      return ctx.vehicle.getVehicle(id);
    },

    technician: async (
      parent: { technician_id?: string },
      _: unknown,
      ctx: GraphQLContext,
    ) => {
      const id = parent.technician_id;
      if (!id) return null; // Un rendez-vous planifié n'a pas encore de technicien
      return ctx.driver.getDriver(id);
    },
  },
};
