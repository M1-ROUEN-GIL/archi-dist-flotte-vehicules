import React, { createContext, useEffect, useState, useRef } from 'react';
import Keycloak from 'keycloak-js';

// Configuration pointant vers ton conteneur Keycloak (vérifie bien le port !)
const keycloak = new Keycloak({
    url: 'http://localhost:8180/auth',
    realm: 'gestion-flotte',
    clientId: 'frontend-web'
});

interface AuthContextType {
    isAuthenticated: boolean;
    token: string | undefined;
    username: string | undefined;
    roles: string[];
    logout: () => void;
}

export const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    const [isInitialized, setIsInitialized] = useState(false);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const isRun = useRef(false); // Évite le double appel en React Strict Mode

    useEffect(() => {
        if (isRun.current) return;
        isRun.current = true;

        keycloak.init({ onLoad: 'login-required' }) // 👈 Magique : Redirige vers Keycloak si non connecté
            .then((authenticated) => {
                setIsAuthenticated(authenticated);
                setIsInitialized(true);
            })
            .catch(console.error);
    }, []);

    if (!isInitialized) {
        return <div style={{ padding: '2rem', textAlign: 'center' }}>Chargement de la sécurité centralisée...</div>;
    }

    // 🧹 Nettoyage : On exclut les rôles techniques de Keycloak
    const rawRoles = keycloak.tokenParsed?.realm_access?.roles || [];
    const cleanRoles = rawRoles.filter(role =>
        role !== 'offline_access' &&
        role !== 'uma_authorization' &&
        !role.startsWith('default-roles-')
    );

    return (
        <AuthContext.Provider
            value={{
                isAuthenticated,
                token: keycloak.token,
                username: keycloak.tokenParsed?.preferred_username,
                roles: cleanRoles, // 👈 On distribue la liste propre !
                logout: () => keycloak.logout({ redirectUri: 'http://localhost:5173' }),
            }}
        >
    {children}
    </AuthContext.Provider>
);
};