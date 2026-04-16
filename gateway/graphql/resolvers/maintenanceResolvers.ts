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

        // 👇 NOUVELLE ROUTE (Historique du véhicule)
        vehicleMaintenanceHistory: async (
            _: unknown,
            args: { vehicle_id: string },
            ctx: GraphQLContext,
        ) => {
            return ctx.maintenance.getVehicleHistory(args.vehicle_id);
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


        updateMaintenanceRecord: async (
            _: unknown,
            args: any,
            ctx: GraphQLContext,
        ) => {
            // On traduit en camelCase et on se limite STRICTEMENT aux 4 champs du DTO Java
            const payload = {
                description: args.description,
                priority: args.priority,
                // Attention: LocalDate en Java veut "YYYY-MM-DD", pas de "T00:00:00Z" à la fin !
                scheduledDate: args.scheduled_date ? args.scheduled_date.split('T')[0] : undefined,
                status: args.status
            };

            const cleanPayload = Object.fromEntries(
                Object.entries(payload).filter(([_, v]) => v !== undefined)
            );

            return ctx.maintenance.updateRecord(args.id, cleanPayload);
        },
    },
    MaintenanceRecord: {
        vehicle: async (
            parent: { vehicle_id?: string },
            _: unknown,
            ctx: GraphQLContext,
        ) => {
            const id = parent.vehicle_id;
            if (!id) return null; // Pas de véhicule lié

            try {
                // On essaie de récupérer le véhicule
                return await ctx.vehicle.getVehicle(id);
            } catch (error: any) {
                // Si le microservice Véhicules répond 404, on intercepte l'erreur !
                // On renvoie juste un objet avec l'ID pour ne pas tout faire planter
                if (error.response?.status === 404 || error.message.includes('404')) {
                    console.warn(`Véhicule introuvable pour l'ID: ${id}`);
                    return { id: id, plate_number: "Véhicule Supprimé/Inconnu" };
                }
                throw error; // Si c'est une autre erreur (ex: 500), on la laisse passer
            }
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