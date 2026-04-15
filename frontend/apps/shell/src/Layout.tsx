import { Outlet, Link } from 'react-router-dom';
import { useAuth } from '@flotte/shared-auth';

export const Layout = () => {
    const { username, roles, logout } = useAuth();

    return (
        <div style={{ display: 'flex', flexDirection: 'column', height: '100vh', fontFamily: 'sans-serif', backgroundColor: 'white' }}>
            {/* BARRE DE NAVIGATION HORIZONTALE EN HAUT */}
            <header style={{ backgroundColor: 'white', borderBottom: '1px solid #e0e0e0', padding: '20px 30px' }}>
                {/* Section titre */}
                <div style={{ marginBottom: '20px' }}>
                    <h1 style={{ margin: '0 0 5px 0', fontSize: '28px', color: 'black', fontWeight: '600' }}>Gestion de Flotte</h1>
                    <p style={{ margin: 0, fontSize: '14px', color: '#666' }}>DHL Express France</p>
                </div>

                {/* Section navigation + user */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    {/* Boutons de navigation */}
                    <nav style={{ display: 'flex', gap: '30px' }}>
                        <Link to="/" style={{ color: '#666', textDecoration: 'none', fontSize: '14px', fontWeight: '500', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <span>📊</span> Tableau de bord
                        </Link>
                        <Link to="/vehicles" style={{ color: '#666', textDecoration: 'none', fontSize: '14px', fontWeight: '500', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <span>🚚</span> Véhicules
                        </Link>
                        <Link to="/" style={{ color: '#666', textDecoration: 'none', fontSize: '14px', fontWeight: '500', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <span>👤</span> Conducteurs
                        </Link>
                        <Link to="/" style={{ color: '#666', textDecoration: 'none', fontSize: '14px', fontWeight: '500', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <span>🔧</span> Maintenance
                        </Link>
                    </nav>

                    {/* Infos utilisateur + déconnexion */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                        <span style={{ fontSize: '14px', color: '#666' }}>👤 {username} ({roles[0]})</span>
                        <button onClick={logout} style={{ padding: '6px 12px', backgroundColor: '#f0f0f0', border: '1px solid #ddd', borderRadius: '4px', cursor: 'pointer', fontSize: '14px', color: 'black' }}>
                            Déconnexion
                        </button>
                    </div>
                </div>
            </header>

            {/* CONTENU CENTRAL DYNAMIQUE */}
            <main style={{ flex: 1, padding: '20px 30px', backgroundColor: 'white', color: 'black', overflowY: 'auto' }}>
                <Outlet />
            </main>
        </div>
    );
};