import { Outlet, Link, NavLink } from 'react-router-dom';
import { useAuth } from '@flotte/shared-auth';
import { Truck, Users, Wrench, LayoutDashboard, Search, MapPin, LogOut } from 'lucide-react';
import monLogo from './assets/logo.png';

export const Layout = () => {
    // Récupération des infos dynamiques de l'utilisateur
    const { username, roles, logout } = useAuth();

    // Petite fonction pour extraire les 2 premières lettres pour l'avatar (ex: "Ahcene" -> "AH")
    const getInitials = (name: string) => {
        return name ? name.substring(0, 2).toUpperCase() : '??';
    };

    return (
        <div className="app-shell" style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', backgroundColor: '#f8fafc' }}>

            {/* EN-TÊTE FIXE */}
            <header style={{ backgroundColor: 'white', borderBottom: '1px solid #e2e8f0', position: 'sticky', top: 0, zIndex: 50 }}>

                {/* 1. Ligne Supérieure : Logo + Recherche + Profil */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px 32px' }}>

                    {/* Logo & Titre */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>

                        <img
                            src={monLogo}
                            alt="Logo"
                            style={{
                                height: '40px',             // On bloque la hauteur pour ne pas gonfler le header
                                width: 'auto',              // La largeur s'adapte automatiquement
                                objectFit: 'contain',
                                transform: 'scale(5)',    // 👈 LA MAGIE : On zoome l'image de 50%
                                transformOrigin: 'left',    // Pour qu'elle s'agrandisse vers la droite sans cacher le bord gauche
                                flexShrink: 0
                            }}
                        />
                        <Link to="/" style={{ marginLeft:'220px', textDecoration: 'none' }}>
                            <p style={{ fontSize: '12px', color: '#64748b', margin: 0, marginTop: '30px' }}>Gérer mon parc automobile</p>
                        </Link>
                    </div>

                    {/* Partie Droite */}
                    <div style={{ display: 'flex', alignItems: 'center', gap: '24px' }}>

                        {/* Barre de recherche */}
                        <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                            <Search size={18} color="#94a3b8" style={{ position: 'absolute', left: '12px' }} />
                            <input
                                placeholder="Rechercher..."
                                style={{ padding: '8px 12px 8px 38px', borderRadius: '20px', border: '1px solid #e2e8f0', backgroundColor: '#f8fafc', width: '220px', fontSize: '14px', outline: 'none' }}
                            />
                        </div>

                        {/* Profil Utilisateur Dynamique */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: '16px', borderLeft: '1px solid #e2e8f0', paddingLeft: '24px' }}>
                            <div style={{ textAlign: 'right' }}>
                                <div style={{ fontSize: '14px', fontWeight: '600', color: '#0f172a' }}>{username || 'Utilisateur'}</div>
                                <div style={{ fontSize: '12px', color: '#64748b', textTransform: 'capitalize' }}>{roles?.[0] || 'Employé'}</div>
                            </div>

                            {/* Avatar dynamique */}
                            <div style={{
                                width: '40px', height: '40px', borderRadius: '50%', backgroundColor: '#3b82f6',
                                color: 'white', display: 'flex', justifyContent: 'center', alignItems: 'center', fontWeight: '600', letterSpacing: '1px'
                            }}>
                                {getInitials(username)}
                            </div>

                            {/* Bouton Déconnexion */}
                            <button
                                onClick={logout}
                                style={{
                                    display: 'flex', alignItems: 'center', gap: '8px', padding: '8px 12px',
                                    borderRadius: '8px', border: '1px solid #fee2e2', backgroundColor: '#fff',
                                    color: '#dc2626', cursor: 'pointer', fontSize: '14px', fontWeight: '500', transition: 'all 0.2s'
                                }}
                                onMouseOver={(e) => e.currentTarget.style.backgroundColor = '#fef2f2'}
                                onMouseOut={(e) => e.currentTarget.style.backgroundColor = '#fff'}
                            >
                                <LogOut size={16} /> Quitter
                            </button>
                        </div>
                    </div>
                </div>

                {/* 2. Ligne Inférieure : Onglets de Navigation */}
                {/* J'ai gardé tes classes CSS d'origine pour ne pas casser ton design si tu en as déjà dans un fichier externe */}
                <nav className="topnav__tabs" aria-label="Navigation principale" style={{ padding: '0 32px', display: 'flex', gap: '20px' }}>
                    <NavLink to="/" end className={({ isActive }) => `topnav__tab ${isActive ? 'is-active' : ''}`}>
                        <LayoutDashboard size={16} /> Tableau de bord
                    </NavLink>
                    <NavLink to="/vehicles" className={({ isActive }) => `topnav__tab ${isActive ? 'is-active' : ''}`}>
                        <Truck size={16} /> Véhicules
                    </NavLink>
                    <NavLink to="/drivers" className={({ isActive }) => `topnav__tab ${isActive ? 'is-active' : ''}`}>
                        <Users size={16} /> Conducteurs
                    </NavLink>
                    <NavLink to="/maintenance" className={({ isActive }) => `topnav__tab ${isActive ? 'is-active' : ''}`}>
                        <Wrench size={16} /> Maintenance
                    </NavLink>
                    <NavLink to="/location" className={({ isActive }) => `topnav__tab ${isActive ? 'is-active' : ''}`}>
                        <MapPin size={16} /> Temps réel
                    </NavLink>
                </nav>
            </header>

            {/* CONTENU DE LA PAGE (Les Micro-Frontends viennent s'injecter ici) */}
            <main className="app-shell__content" style={{ flex: 1, padding: '32px', boxSizing: 'border-box' }}>
                <Outlet />
            </main>

        </div>
    );
};