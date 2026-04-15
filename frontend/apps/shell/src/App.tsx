import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Layout } from './Layout';
import {Suspense} from "react";

// Un composant simple pour ta page d'accueil (Home)
const Home = () => (
    <div style={{ backgroundColor: 'white', color: 'black', height: '100%', padding: '20px' }}>
        <h1 style={{ color: 'black' }}>Bienvenue sur votre portail</h1>
        <p style={{ color: 'black' }}>Ceci est la page centrale de votre application.</p>
        <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
            <div style={{ padding: '20px', border: '1px solid #000', borderRadius: '8px', flex: 1, backgroundColor: 'white', color: 'black' }}>
                <h3 style={{ color: 'black' }}>Statistiques</h3>
                <p style={{ color: 'black' }}>12 camions en ligne</p>
            </div>
            <div style={{ padding: '20px', border: '1px solid #000', borderRadius: '8px', flex: 1, backgroundColor: 'white', color: 'black' }}>
                <h3 style={{ color: 'black' }}>Alertes</h3>
                <p style={{ color: 'black' }}>Aucune panne détectée</p>
            </div>
        </div>
    </div>
);

// @ts-ignore
const VehicleList = React.lazy(() => import('vehicles_app/VehicleList'));

function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* On définit la route parente qui utilise le Layout */}
                <Route path="/" element={<Layout />}>

                    {/* Route par défaut (l'accueil) */}
                    <Route index element={<Home />} />

                    {/* Les autres pages (en construction) */}
                    <Route path="map" element={<h2>Page de la Carte (Bientôt)</h2>} />
                    <Route
                        path="vehicles"
                        element={
                            <Suspense fallback={<div>Chargement du module véhicules...</div>}>
                                <VehicleList />
                            </Suspense>
                        }
                    />

                </Route>
            </Routes>
        </BrowserRouter>
    );
}

export default App;