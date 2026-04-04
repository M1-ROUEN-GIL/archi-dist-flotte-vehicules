import { GraphQLError } from 'graphql';
function notImplemented(field) {
    throw new GraphQLError(`« ${field} » : pas encore relié au microservice (voir resolvers).`, { extensions: { code: 'NOT_IMPLEMENTED' } });
}
function subscriptionDisabled(name) {
    throw new GraphQLError(`Subscription « ${name} » : non activée sur ce gateway (WebSocket / graphql-ws à brancher).`, { extensions: { code: 'SUBSCRIPTIONS_DISABLED' } });
}
export const stubResolvers = {
    Query: {
        alerts: () => notImplemented('alerts'),
        alert: () => notImplemented('alert'),
        vehicleLocation: () => notImplemented('vehicleLocation'),
        locationHistory: () => notImplemented('locationHistory'),
    },
    Mutation: {
        acknowledgeAlert: () => notImplemented('acknowledgeAlert'),
        resolveAlert: () => notImplemented('resolveAlert'),
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
