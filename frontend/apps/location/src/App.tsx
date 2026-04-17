import { useState, useEffect } from 'react';
import { useQuery, useSubscription } from '@apollo/client';
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { GET_VEHICLE_LOCATION, WATCH_VEHICLE_LOCATION } from './queries';
import { Navigation } from 'lucide-react';

// Composant pour recentrer la carte automatiquement quand le camion bouge
const RecenterAutomatically = ({ lat, lng }: { lat: number, lng: number }) => {
  const map = useMap();
  useEffect(() => {
    map.setView([lat, lng], map.getZoom(), { animate: true });
  }, [lat, lng, map]);
  return null;
};

export default function LocationMap() {
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    try {
      // 🐛 Fix obligatoire pour que les icônes Leaflet s'affichent bien dans React
      delete (L.Icon.Default.prototype as any)._getIconUrl;
      L.Icon.Default.mergeOptions({
        iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
        iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
        shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
      });
    } catch (e) {
      console.error("Leaflet setup error:", e);
      setError("Erreur d'initialisation de la carte.");
    }
  }, []);

  if (error) return <div style={{ color: 'red', padding: '1rem' }}>❌ {error}</div>;

  // ⚠️ ID écrit en dur pour l'exemple. À relier avec ton module Véhicules !
  const [selectedVehicleId, setSelectedVehicleId] = useState<string>("550e8400-e29b-41d4-a716-446655440076");

  // 1. Récupérer la dernière position connue
  const { data: initialData, loading } = useQuery(GET_VEHICLE_LOCATION, {
    variables: { vehicle_id: selectedVehicleId },
    skip: !selectedVehicleId,
  });
  console.log("🚨 L'ID ENVOYÉ EST :", selectedVehicleId);
  // 2. S'abonner au flux GPS temps réel (WebSocket)
  const { data: realtimeData } = useSubscription(WATCH_VEHICLE_LOCATION, {
    variables: { vehicle_id: selectedVehicleId },
    skip: !selectedVehicleId,
  });

  // On prend la donnée temps réel en priorité, sinon la donnée initiale
  const location = realtimeData?.vehicleLocationUpdated || initialData?.vehicleLocation;

  // Coordonnées par défaut (Centre de la France) si le camion n'a pas de GPS
  const position: [number, number] = location
      ? [location.latitude, location.longitude]
      : [46.603354, 1.888334];

  if (loading) return <div>Chargement...</div>;
  return (
      <div style={{ padding: '0', fontFamily: 'sans-serif', display: 'flex', flexDirection: 'column', minHeight: 'calc(100vh - 100px)' }}>
        {/* HEADER & CONTROLES */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h2 style={{ margin: 0, color: '#1e293b' }}>📍 Suivi de la Flotte</h2>

          <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
            {location && (
                <div style={{ display: 'flex', gap: '10px', fontSize: '0.9rem', color: '#475569', backgroundColor: '#f1f5f9', padding: '8px 15px', borderRadius: '8px' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Navigation size={14} /> Vitesse: <strong>{location.speed_kmh || 0} km/h</strong>
                  </span>
                  <span>•</span>
                  <span>Dernier signal: <strong>{new Date(location.recorded_at).toLocaleTimeString()}</strong></span>
                </div>
            )}
            <select
                value={selectedVehicleId}
                onChange={(e) => setSelectedVehicleId(e.target.value)}
                style={{ padding: '8px 12px', borderRadius: '8px', border: '1px solid #e2e8f0', backgroundColor: '#f8fafc' }}
            >
              <option value="550e8400-e29b-41d4-a716-446655440076">Camion A </option>
              {/* Ajoute d'autres ID ici pour tester */}
            </select>
          </div>
        </div>

        {/* CARTE LEAFLET */}
        <div style={{ flex: 1, minHeight: '480px', borderRadius: '8px', overflow: 'hidden', border: '1px solid #e2e8f0' }}>
          <MapContainer center={position} zoom={location ? 14 : 5} style={{ height: '100%', minHeight: '480px', width: '100%' }}>

            <TileLayer
                url="https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png"
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
            />

            {location && (
                <>
                  <RecenterAutomatically lat={location.latitude} lng={location.longitude} />
                  <Marker position={position}>
                    <Popup>
                      <div style={{ textAlign: 'center' }}>
                        <strong>Camion en route</strong><br/>
                        Vitesse : {location.speed_kmh || 0} km/h<br/>
                        Cap : {location.heading_deg || 0}°
                      </div>
                    </Popup>
                  </Marker>
                </>
            )}
          </MapContainer>
        </div>
      </div>
  );
}