import React, { Suspense } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { useQuery, gql } from '@apollo/client'; // Importation Apollo
import { Layout } from './Layout';
import { Truck, Wrench, Users, Clock } from 'lucide-react';

// 1. La requête combinée (basée sur les requêtes existantes de tes autres pages)
const GET_ALL_FOR_STATS = gql`
  query GetAllForStats {
    vehicles {
      id
      status
    }
    drivers {
      items {
        id
        status
      }
    }
    maintenanceRecords {
      items {
        id
        status
      }
    }
  }
`;

// 🎨 Composant Carte (optimisé pour 2 par ligne)
const DashboardCard = ({ title, value, icon: Icon, color, bgColor }: any) => (
    <div style={{
        flex: '1 1 calc(50% - 10px)', // Prend 50% moins la moitié du gap
        minWidth: '250px',
        padding: '24px',
        backgroundColor: 'white',
        border: '1px solid #e2e8f0',
        borderRadius: '12px',
        display: 'flex',
        alignItems: 'center',
        gap: '20px',
        boxSizing: 'border-box'
    }}>
        <div style={{
            width: '56px',
            height: '56px',
            borderRadius: '12px',
            backgroundColor: bgColor,
            color: color,
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center'
        }}>
            <Icon size={28} />
        </div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
            <span style={{ fontSize: '13px', color: '#64748b', fontWeight: '500' }}>{title}</span>
            <span style={{ fontSize: '32px', color: '#0f172a', fontWeight: '600', lineHeight: '1' }}>{value}</span>
        </div>
    </div>
);

// 🏠 La page d'accueil avec calcul manuel
const Home = () => {
    const { data, loading, error } = useQuery(GET_ALL_FOR_STATS);

    if (loading) return <div style={{ padding: '30px' }}>⏳ Chargement des statistiques...</div>;

    if (error) {
        console.error("Détails de l'erreur GraphQL :", error);
        return <div style={{ padding: '30px', color: 'red' }}>❌ Erreur : {error.message}</div>;
    }

    // 1. On récupère les tableaux de données (sécurisés pour éviter les crashs si vides)
    const allVehicles = data?.vehicles || [];
    const allDrivers = data?.drivers?.items || [];
    const allMaintenance = data?.maintenanceRecords?.items || [];

    // 2. LE FILTRAGE DE PRÉCISION 🎯

    // Véhicules : Uniquement ceux qui sont 'AVAILABLE' (Disponibles)
    const activeVehiclesCount = allVehicles.filter((v: any) => v.status === 'AVAILABLE').length;

    // Conducteurs : Ceux qui sont 'ACTIVE' ou 'ON_TOUR' (En tournée)
    const activeDriversCount = allDrivers.filter((d: any) =>
        d.status === 'ACTIVE' || d.status === 'ON_TOUR'
    ).length;

    // Maintenances : En cours et Prévues
    const inMaintenanceCount = allMaintenance.filter((m: any) => m.status === 'IN_PROGRESS').length;
    const pendingMaintenanceCount = allMaintenance.filter((m: any) => m.status === 'SCHEDULED').length;

    return (
        <div style={{ backgroundColor: '#f8fafc', padding: '32px', minHeight: '100%' }}>
            <div style={{ marginBottom: '24px' }}>
                <h2 style={{ fontSize: '24px', fontWeight: '700', color: '#0f172a', margin: 0 }}>Tableau de bord</h2>
                <p style={{ color: '#64748b', marginTop: '4px' }}>Vue d'ensemble de votre activité logistique</p>
            </div>

            {/* LE GRID MODIFIÉ */}
            <div style={{
                display: 'grid',
                gridTemplateColumns: '1fr 1fr', // Force 2 colonnes égales (50% chacune)
                gap: '24px',                    // Espace entre les cartes
                width: '100%',                  // Occupe toute la largeur du parent
                boxSizing: 'border-box'
            }}>
                <DashboardCard title="Véhicules disponibles" value={activeVehiclesCount} icon={Truck} color="#16a34a" bgColor="#dcfce7" />
                <DashboardCard title="En maintenance" value={inMaintenanceCount} icon={Wrench} color="#ea580c" bgColor="#ffedd5" />
                <DashboardCard title="Conducteurs actifs" value={activeDriversCount} icon={Users} color="#2563eb" bgColor="#dbeafe" />
                <DashboardCard title="Maintenances prévues" value={pendingMaintenanceCount} icon={Clock} color="#9333ea" bgColor="#f3e8ff" />
            </div>
        </div>
    );
};

// Imports dynamiques des Micro-Frontends avec résolution robuste (m.default || m)
const VehicleList = React.lazy(() => import('vehicles_app/VehicleList').then(m => ({ default: m.default || m })));
const DriverList = React.lazy(() => import('drivers_app/DriverList').then(m => ({ default: m.default || m })));
const MaintenanceList = React.lazy(() => import('maintenance_app/MaintenanceList').then(m => ({ default: m.default || m })));
const LocationMap = React.lazy(() => import('location_app/LocationMap').then(m => ({ default: m.default || m })));

function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* On définit la route parente qui utilise le Layout */}
                <Route path="/" element={<Layout />}>

                    {/* Route par défaut (l'accueil) */}
                    <Route index element={<Home />} />

                    {/* Les autres pages */}
                    <Route path="map" element={<h2>Page de la Carte (Bientôt)</h2>} />
                    <Route
                        path="vehicles"
                        element={
                            <Suspense fallback={<div>Chargement du module véhicules...</div>}>
                                <VehicleList />
                            </Suspense>
                        }
                    />
                    <Route
                        path="drivers"
                        element={
                            <Suspense fallback={<div>Chargement du module chauffeurs...</div>}>
                                <DriverList />
                            </Suspense>
                        }
                    />

                    <Route path="maintenance" element={
                        <Suspense fallback={<div>Chargement de la maintenance...</div>}>
                            <MaintenanceList />
                        </Suspense>
                    } />

                    <Route
                        path="/location"
                        element={
                            <Suspense fallback={<div style={{ padding: '2rem' }}>⏳ Chargement de la carte radar...</div>}>
                                <LocationMap />
                            </Suspense>
                        }
                    />

                </Route>
            </Routes>
        </BrowserRouter>
    );
}

export default App;