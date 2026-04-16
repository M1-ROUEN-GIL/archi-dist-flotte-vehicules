import type { GraphQLContext } from '../context.js';

/** Mappe la réponse camelCase du location-service vers le schema GraphQL snake_case. */
function toGqlLocation(pos: any) {
  if (!pos) return null;
  return {
    vehicle_id:  pos.vehicleId  ?? pos.vehicle_id,
    latitude:    pos.latitude,
    longitude:   pos.longitude,
    speed_kmh:   pos.speedKmh   ?? pos.speed_kmh   ?? null,
    heading_deg: pos.headingDeg ?? pos.heading_deg  ?? null,
    accuracy_m:  pos.accuracyM  ?? pos.accuracy_m   ?? null,
    recorded_at: pos.time       ?? pos.recorded_at  ?? new Date().toISOString(),
  };
}

export const locationResolvers = {
  Query: {
    vehicleLocation: async (
      _: unknown,
      args: { vehicle_id: string },
      ctx: GraphQLContext,
    ) => {
      try {
        const pos = await ctx.location.getLatestPosition(args.vehicle_id);
        return toGqlLocation(pos);
      } catch {
        return null;
      }
    },

    locationHistory: async (
      _: unknown,
      args: { vehicle_id: string; from: string; to: string },
      ctx: GraphQLContext,
    ) => {
      const positions = await ctx.location.getHistory(args.vehicle_id, args.from, args.to);
      const mapped = (positions as any[]).map(toGqlLocation);
      return {
        vehicle_id: args.vehicle_id,
        positions:  mapped,
        total:      mapped.length,
      };
    },
  },

  //  LA NOUVELLE SECTION POUR LE TEMPS RÉEL
  Subscription: {
    vehicleLocationUpdated: {
      // 1. La méthode subscribe : elle doit retourner un flux (AsyncIterator)
      subscribe: async (_: unknown, args: { vehicle_id: string }, ctx: GraphQLContext) => {
        // ctx.location doit retourner le flux gRPC converti en AsyncIterator
        return ctx.location.watchVehicleStream(args.vehicle_id);
      },

      // 2. La méthode resolve : magique ! Elle applique ton formateur sur chaque point GPS qui passe
      resolve: (payload: any) => {
        // Le payload brut arrive du flux gRPC, on le passe dans ta fonction géniale
        return toGqlLocation(payload);
      },
    },
  },
};
