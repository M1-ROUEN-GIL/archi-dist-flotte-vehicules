import React, { useMemo } from 'react';
import { ApolloClient, InMemoryCache, ApolloProvider, createHttpLink } from '@apollo/client';
import { setContext } from '@apollo/client/link/context';
import { useAuth } from '@flotte/shared-auth';

// ⚠️ À remplacer par l'URL exacte de la Gateway GraphQL
const GATEWAY_URL = 'http://localhost:4000/graphql';

export const AppGraphQLProvider = ({ children }: { children: React.ReactNode }) => {
    const { token } = useAuth();

    // useMemo permet de ne pas recréer le client à chaque rendu, sauf si le token change
    const client = useMemo(() => {

        const httpLink = createHttpLink({
            uri: GATEWAY_URL,
        });

        // L'intercepteur magique : il ajoute le token JWT dans l'en-tête de CHAQUE requête
        const authLink = setContext((_, { headers }) => {
            return {
                headers: {
                    ...headers,
                    authorization: token ? `Bearer ${token}` : "",
                }
            }
        });

        return new ApolloClient({
            link: authLink.concat(httpLink),
            cache: new InMemoryCache(), // Garde les données en mémoire pour éviter de spammer le backend
        });

    }, [token]);

    return <ApolloProvider client={client}>{children}</ApolloProvider>;
};