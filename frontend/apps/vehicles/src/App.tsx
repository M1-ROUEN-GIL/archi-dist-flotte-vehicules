import { useState } from 'react';
import { useQuery, useMutation } from '@apollo/client';
import { GET_VEHICLES, CREATE_VEHICLE, UPDATE_VEHICLE, DELETE_VEHICLE } from './queries';
import { Edit2, Trash2, Plus } from 'lucide-react';

export default function VehicleList() {
    const { loading, error, data, refetch } = useQuery(GET_VEHICLES);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingVehicle, setEditingVehicle] = useState<any>(null);
    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    // Mutations
    const [createVehicle] = useMutation(CREATE_VEHICLE, { onCompleted: () => { refetch(); closeModal(); } });
    const [updateVehicle] = useMutation(UPDATE_VEHICLE, { onCompleted: () => { refetch(); closeModal(); } });
    const [deleteVehicle] = useMutation(DELETE_VEHICLE, { onCompleted: () => refetch() });

    const [formData, setFormData] = useState({
        plate_number: '', brand: '', model: '', fuel_type: 'DIESEL',
        mileage_km: 0, payload_capacity_kg: 1000, cargo_volume_m3: 20, vin: ''
    });

    const openModal = (vehicle: any = null) => {
        setErrorMessage(null);
        if (vehicle) {
            setEditingVehicle(vehicle);
            setFormData({ ...vehicle });
        } else {
            setEditingVehicle(null);
            setFormData({ plate_number: '', brand: '', model: '', fuel_type: 'DIESEL', mileage_km: 0, payload_capacity_kg: 1000, cargo_volume_m3: 20, vin: '' });
        }
        setIsModalOpen(true);
    };

    const closeModal = () => {
        setIsModalOpen(false);
        setEditingVehicle(null);
        setErrorMessage(null);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setErrorMessage(null);

        try {
            if (editingVehicle) {
                await updateVehicle({ variables: {
                        id: editingVehicle.id,
                        brand: formData.brand,
                        model: formData.model,
                        mileage_km: parseInt(formData.mileage_km.toString()),
                        vin: formData.vin
                    }});
            } else {
                await createVehicle({ variables: {
                        ...formData,
                        mileage_km: parseInt(formData.mileage_km.toString()),
                        payload_capacity_kg: parseInt(formData.payload_capacity_kg.toString()),
                        cargo_volume_m3: parseFloat(formData.cargo_volume_m3.toString())
                    }});
            }
        } catch (err: any) {
            const msg = err.message || "";
            if (msg.includes("400")) {
                setErrorMessage("Impossible d'enregistrer : Cette plaque d'immatriculation existe probablement déjà ou les données sont invalides.");
            } else {
                setErrorMessage("Erreur serveur : " + msg);
            }
        }
    };

    if (loading) return <div>Chargement...</div>;
    if (error) return <div>Erreur...</div>;

    return (
        <div style={{ padding: '0', fontFamily: 'sans-serif' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px', alignItems: 'center' }}>
                <h2 style={{ color: '#0f172a', margin: 0 }}>Véhicules</h2>
                <button onClick={() => openModal()} style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '10px 20px', backgroundColor: '#0f172a', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: '500', fontSize: '14px' }}>
                    <Plus size={16} /> Ajouter un véhicule
                </button>
            </div>

            <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: 'white', border: '1px solid #e2e8f0', borderRadius: '8px', overflow: 'hidden', fontSize: '14px' }}>
                <thead style={{ backgroundColor: '#f8fafc', color: '#64748b', textAlign: 'left', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                <tr>
                    <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>Immatriculation</th>
                    <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>Marque & Modèle</th>
                    <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>Kilométrage</th>
                    <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>Statut</th>
                    <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>Actions</th>
                </tr>
                </thead>
                <tbody>
                {data?.vehicles.map((v: any) => (
                    <tr key={v.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                        <td style={{ padding: '10px 16px', fontWeight: '500', color: '#0f172a' }}>{v.plate_number}</td>
                        <td style={{ padding: '10px 16px', color: '#475569' }}>{v.brand} {v.model}</td>
                        <td style={{ padding: '10px 16px', color: '#475569' }}>{v.mileage_km.toLocaleString()} km</td>
                        <td style={{ padding: '10px 16px' }}>
                            <StatusBadge status={v.status} />
                        </td>
                        <td style={{ padding: '10px 16px' }}>
                            <div style={{ display: 'flex', gap: '12px' }}>
                                <button onClick={() => openModal(v)} style={{ cursor: 'pointer', background: 'none', border: 'none', color: '#64748b', display: 'flex', alignItems: 'center', padding: 0 }} title="Éditer">
                                    <Edit2 size={16} />
                                </button>
                                <button onClick={() => { if(window.confirm("Supprimer ?")) deleteVehicle({ variables: { id: v.id } }) }} style={{ cursor: 'pointer', background: 'none', border: 'none', color: '#ef4444', display: 'flex', alignItems: 'center', padding: 0 }} title="Supprimer">
                                    <Trash2 size={16} />
                                </button>
                            </div>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>

            {isModalOpen && (
                <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 }}>
                    <div style={{ backgroundColor: 'white', padding: '30px', borderRadius: '12px', width: '450px', boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1)' }}>
                        <h3 style={{ marginTop: 0, marginBottom: '20px', color: '#1e293b', display: 'flex', alignItems: 'center', gap: '8px' }}>
                            {editingVehicle ? <><Edit2 size={20} /> Modifier le véhicule</> : <><Plus size={20} /> Nouveau véhicule</>}
                        </h3>

                        {errorMessage && (
                            <div style={{ backgroundColor: '#fef2f2', color: '#991b1b', padding: '10px', borderRadius: '6px', marginBottom: '15px', fontSize: '0.9rem', border: '1px solid #f87171' }}>
                                {errorMessage}
                            </div>
                        )}

                        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>

                            {!editingVehicle && (
                                <div style={{ display: 'flex', flexDirection: 'column' }}>
                                    <label style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '4px', fontWeight: 'bold' }}>Plaque d'immatriculation *</label>
                                    <input style={{ padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} placeholder="ex: AA-123-BB" value={formData.plate_number} onChange={e => setFormData({...formData, plate_number: e.target.value})} required />
                                </div>
                            )}

                            <div style={{ display: 'flex', gap: '10px' }}>
                                <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
                                    <label style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '4px', fontWeight: 'bold' }}>Marque *</label>
                                    <input style={{ padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} placeholder="ex: Renault" value={formData.brand} onChange={e => setFormData({...formData, brand: e.target.value})} required />
                                </div>
                                <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
                                    <label style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '4px', fontWeight: 'bold' }}>Modèle *</label>
                                    <input style={{ padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} placeholder="ex: Master" value={formData.model} onChange={e => setFormData({...formData, model: e.target.value})} required />
                                </div>
                            </div>

                            <div style={{ display: 'flex', flexDirection: 'column' }}>
                                <label style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '4px', fontWeight: 'bold' }}>Kilométrage initial *</label>
                                <input style={{ padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} type="number" min="0" value={formData.mileage_km} onChange={e => setFormData({...formData, mileage_km: parseInt(e.target.value)})} required />
                            </div>

                            {!editingVehicle && (
                                <>
                                    <div style={{ display: 'flex', gap: '10px' }}>
                                        <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
                                            <label style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '4px', fontWeight: 'bold' }}>Charge utile (kg) *</label>
                                            <input style={{ padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} type="number" min="1" value={formData.payload_capacity_kg} onChange={e => setFormData({...formData, payload_capacity_kg: parseInt(e.target.value)})} required />
                                        </div>
                                        <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
                                            <label style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '4px', fontWeight: 'bold' }}>Volume (m³) *</label>
                                            <input style={{ padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} type="number" min="1" step="0.1" value={formData.cargo_volume_m3} onChange={e => setFormData({...formData, cargo_volume_m3: parseFloat(e.target.value)})} required />
                                        </div>
                                    </div>

                                    <div style={{ display: 'flex', flexDirection: 'column' }}>
                                        <label style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '4px', fontWeight: 'bold' }}>Type de carburant *</label>
                                        <select style={{ padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', backgroundColor: 'white' }} value={formData.fuel_type} onChange={e => setFormData({...formData, fuel_type: e.target.value})}>
                                            <option value="DIESEL">Diesel</option>
                                            <option value="ELECTRIC">Électrique</option>
                                            <option value="GASOLINE">Essence</option>
                                            <option value="HYBRID">Hybride</option>
                                        </select>
                                    </div>
                                </>
                            )}

                            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '15px' }}>
                                <button type="button" onClick={closeModal} style={{ padding: '10px 15px', backgroundColor: '#f1f5f9', color: '#475569', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold' }}>
                                    Annuler
                                </button>
                                <button type="submit" style={{ backgroundColor: '#16a34a', color: 'white', border: 'none', padding: '10px 15px', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold' }}>
                                    {editingVehicle ? "Mettre à jour" : "Enregistrer"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}

// 👇 Le composant Badge ajouté en bas du fichier
const StatusBadge = ({ status }: { status: string }) => {
    let color = '#94a3b8'; // Défaut (gris)
    let bg = '#f1f5f9';
    let label = status;

    if (status === 'AVAILABLE') { color = '#16a34a'; bg = '#dcfce7'; label = 'Disponible'; }
    if (status === 'ON_DELIVERY') { color = '#2563eb'; bg = '#dbeafe'; label = 'En tournée'; }
    if (status === 'IN_MAINTENANCE') { color = '#ca8a04'; bg = '#fef08a'; label = 'En maintenance'; }
    if (status === 'OUT_OF_SERVICE') { color = '#dc2626'; bg = '#fee2e2'; label = 'Hors service'; }

    return (
        <span style={{ backgroundColor: bg, color: color, padding: '4px 8px', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 'bold', display: 'inline-block' }}>
      {label}
    </span>
    );
};