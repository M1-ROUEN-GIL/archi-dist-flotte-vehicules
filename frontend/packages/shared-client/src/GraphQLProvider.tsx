import React, { useMemo } from 'react';
import { ApolloClient, InMemoryCache, ApolloProvider, createHttpLink, split } from '@apollo/client';
import { setContext } from '@apollo/client/link/context';
import { getMainDefinition } from '@apollo/client/utilities';
import { GraphQLWsLink } from '@apollo/client/link/subscriptions';
import { createClient } from 'graphql-ws';
import { useAuth } from '@flotte/shared-auth';

const isProd = import.meta.env.PROD;

// ⚠️ URLs de la Gateway GraphQL
const GATEWAY_HTTP_URL = isProd ? '/graphql' : 'http://localhost:4000/graphql';
const GATEWAY_WS_URL = isProd 
    ? `ws://${window.location.host}/graphql` 
    : 'ws://localhost:4000/graphql';

export const AppGraphQLProvider = ({ children }: { children: React.ReactNode }) => {
    const { token } = useAuth();

    // useMemo permet de ne pas recréer le client à chaque rendu, sauf si le token change
    const client = useMemo(() => {

        // 1. Configuration HTTP standard
        const httpLink = createHttpLink({
            uri: GATEWAY_HTTP_URL,
        });

        // L'intercepteur magique : il ajoute le token JWT dans l'en-tête de CHAQUE requête HTTP
        const authLink = setContext((_, { headers }) => {
            return {
                headers: {
                    ...headers,
                    authorization: token ? `Bearer ${token}` : "",
                }
            }
        });

        // Lien HTTP final (Auth + HTTP)
        const authenticatedHttpLink = authLink.concat(httpLink);

        // 2. Configuration WebSocket (Temps réel)
        const wsLink = new GraphQLWsLink(createClient({
            url: GATEWAY_WS_URL,
            // Pour les WebSockets, l'authentification passe par connectionParams
            connectionParams: {
                authorization: token ? `Bearer ${token}` : "",
            },
        }));

        // 3. Le Routeur (Split) : diriger vers WS si c'est une Subscription, sinon vers HTTP
        const splitLink = split(
            ({ query }) => {
                const definition = getMainDefinition(query);
                return (
                    definition.kind === 'OperationDefinition' &&
                    definition.operation === 'subscription'
                );
            },
            wsLink, // Le flux temps réel passe par ici
            authenticatedHttpLink // Les requêtes et mutations classiques passent par ici
        );

        return new ApolloClient({
            link: splitLink, // On utilise le routeur comme lien principal
            cache: new InMemoryCache(), // Garde les données en mémoire
        });

    }, [token]);

    return <ApolloProvider client={client} children={children} />;};