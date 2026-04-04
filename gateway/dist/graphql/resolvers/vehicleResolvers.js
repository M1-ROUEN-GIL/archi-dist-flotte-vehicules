import { GraphQLError } from 'graphql';
export const vehicleResolvers = {
    Query: {
        vehicles: async (_, args, ctx) => {
            return ctx.vehicle.listVehicles({
                status: args.status,
                limit: args.limit ?? 20,
                offset: args.offset ?? 0,
            });
        },
        vehicle: async (_, args, ctx) => {
            return ctx.vehicle.getVehicle(args.id);
        },
        vehicleAssignments: async (_, args, ctx) => {
            return ctx.vehicle.getAssignments(args.vehicle_id);
        },
    },
    Mutation: {
        createVehicle: async (_, args, ctx) => {
            return ctx.vehicle.createVehicle({
                ...args,
                fuel_type: args.fuel_type.toLowerCase()
            });
        },
        updateVehicleStatus: async (_, args, ctx) => {
            return ctx.vehicle.updateVehicleStatus(args.id, args.status.toLowerCase());
        },
        assignVehicle: async (_, args, ctx) => {
            return ctx.vehicle.assignVehicle(args.vehicle_id, args.driver_id, args.notes ?? undefined);
        },
        unassignVehicle: async (_, args, ctx) => {
            return ctx.vehicle.unassignVehicle(args.vehicle_id);
        },
        updateVehicle: async (_, args, ctx) => {
            return ctx.vehicle.updateVehicle(args.id, args);
        },
        deleteVehicle: async (_, args, ctx) => {
            await ctx.vehicle.deleteVehicle(args.id);
            return true;
        },
    },
    Vehicle: {
        current_location: () => null,
        current_assignment: () => null,
        maintenance_records: async (parent, args, ctx) => {
            const { items } = await ctx.maintenance.listRecords({
                vehicle_id: parent.id,
                limit: args.limit ?? 5,
            });
            return items;
        },
    },
    Assignment: {
        vehicle: async (parent, _, ctx) => {
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
        driver: async (parent, _, ctx) => {
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
