import { GraphQLError } from 'graphql';
export const maintenanceResolvers = {
    Query: {
        maintenanceRecords: async (_, args, ctx) => {
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
        maintenanceRecord: async (_, args, ctx) => {
            return ctx.maintenance.getRecord(args.id);
        },
    },
    Mutation: {
        createMaintenanceRecord: async (_, args, ctx) => {
            return ctx.maintenance.createRecord(args);
        },
        updateMaintenanceStatus: async (_, args, ctx) => {
            return ctx.maintenance.updateStatus(args.id, args);
        },
    },
    MaintenanceRecord: {
        vehicle: async (parent, _, ctx) => {
            const id = parent.vehicle_id;
            if (!id)
                throw new GraphQLError('MaintenanceRecord sans vehicle_id');
            return ctx.vehicle.getVehicle(id);
        },
        technician: async (parent, _, ctx) => {
            const id = parent.technician_id;
            if (!id)
                return null; // Un rendez-vous planifié n'a pas encore de technicien
            return ctx.driver.getDriver(id);
        },
    },
};
