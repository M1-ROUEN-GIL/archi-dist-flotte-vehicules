import { useState } from 'react';
import { useQuery, useMutation } from '@apollo/client';
import { GET_DRIVERS, CREATE_DRIVER, UPDATE_DRIVER_STATUS, UPDATE_DRIVER, DELETE_DRIVER } from './queries';
import { Edit2, Trash2, Plus } from 'lucide-react';

export default function DriverList() {
  const { loading, error, data, refetch } = useQuery(GET_DRIVERS, { fetchPolicy: 'network-only' });
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // 👇 Nouvel état pour savoir si on est en train de modifier ou de créer
  const [editingDriver, setEditingDriver] = useState<any>(null);

  // Mutations
  const [createDriver] = useMutation(CREATE_DRIVER, { onCompleted: () => { refetch(); closeModal(); } });
  const [updateStatus] = useMutation(UPDATE_DRIVER_STATUS);
  const [updateDriver] = useMutation(UPDATE_DRIVER, { onCompleted: () => { refetch(); closeModal(); } });
  const [deleteDriver] = useMutation(DELETE_DRIVER, { onCompleted: () => refetch() });

  const [formData, setFormData] = useState({
    first_name: '', last_name: '', email: '', phone: '', employee_id: '',
    keycloak_user_id: ''
  });

  // 👇 Modification de l'ouverture de la modale pour accepter un chauffeur
  const openModal = (driver: any = null) => {
    setErrorMessage(null);
    if (driver) {
      setEditingDriver(driver);
      setFormData({
        first_name: driver.first_name,
        last_name: driver.last_name,
        email: driver.email,
        phone: driver.phone || '',
        employee_id: driver.employee_id || '',
        keycloak_user_id: driver.keycloak_user_id
      });
    } else {
      setEditingDriver(null);
      setFormData({
        first_name: '', last_name: '', email: '', phone: '', employee_id: '',
        keycloak_user_id: crypto.randomUUID ? crypto.randomUUID() : '550e8400-e29b-41d4-a716-446655440000'
      });
    }
    setIsModalOpen(true);
  };

  const closeModal = () => { setIsModalOpen(false); setErrorMessage(null); setEditingDriver(null); };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    try {
      const payload = {
        first_name: formData.first_name,
        last_name: formData.last_name,
        email: formData.email,
        phone: formData.phone.trim() === '' ? null : formData.phone,
        employee_id: formData.employee_id.trim() === '' ? null : formData.employee_id
      };

      if (editingDriver) {
        // Mode Modification
        await updateDriver({ variables: { id: editingDriver.id, keycloak_user_id: formData.keycloak_user_id, ...payload } });
      } else {
        // Mode Création
        await createDriver({ variables: { keycloak_user_id: formData.keycloak_user_id, ...payload } });
      }
    } catch (err: any) {
      setErrorMessage("Erreur d'enregistrement : " + (err.message || "Données invalides"));
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm("Êtes-vous sûr de vouloir supprimer ce conducteur ? Cette action est irréversible.")) {
      try {
        await deleteDriver({ variables: { id } });
      } catch (err: any) {
        alert("Erreur lors de la suppression : " + err.message);
      }
    }
  };

  const handleStatusChange = async (id: string, newStatus: string) => {
    try {
      await updateStatus({ variables: { id, status: newStatus } });
    } catch (err: any) {
      alert("Impossible de changer le statut : " + err.message);
    }
  };

  if (loading) return <div style={{ padding: '2rem' }}>⏳ Chargement des conducteurs...</div>;
  if (error) return <div style={{ padding: '2rem', color: '#dc2626' }}>❌ Erreur GraphQL : {error.message}</div>;

  const drivers = data?.drivers?.items || [];
  const totalCount = data?.drivers?.total_count || 0;

  return (
      <div style={{ padding: '0', fontFamily: 'sans-serif' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px', alignItems: 'center' }}>
          <h2 style={{ color: '#0f172a', margin: 0 }}>
            Conducteurs <span style={{ fontSize: '1rem', color: '#64748b', fontWeight: 'normal' }}>({totalCount} au total)</span>
          </h2>
          <button onClick={() => openModal()} style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '10px 20px', backgroundColor: '#0f172a', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: '500', fontSize: '14px' }}>
            <Plus size={16} /> Ajouter un conducteur
          </button>
        </div>

        <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: 'white', border: '1px solid #e2e8f0', borderRadius: '8px', overflow: 'hidden', fontSize: '14px' }}>
          <thead style={{ backgroundColor: '#f8fafc', color: '#64748b', textAlign: 'left', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
          <tr>
            <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>Nom & Prénom</th>
            <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>Contact</th>
            <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>N° de Permis</th>
            <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>Statut</th>
            <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>Actions</th>
          </tr>
          </thead>
          <tbody>
          {drivers.length === 0 ? (
              <tr><td colSpan={5} style={{ textAlign: 'center', padding: '20px', color: '#94a3b8' }}>Aucun conducteur trouvé.</td></tr>
          ) : (
              drivers.map((d: any) => (
                  <tr key={d.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                    <td style={{ padding: '10px 16px', fontWeight: '500', color: '#0f172a' }}>{d.last_name?.toUpperCase()} {d.first_name}</td>
                    <td style={{ padding: '10px 16px', fontSize: '13px', color: '#475569' }}>{d.email}<br/><span style={{color: '#94a3b8'}}>{d.phone}</span></td>
                    <td style={{ padding: '10px 16px', fontFamily: 'monospace', color: '#475569' }}>{d.license?.license_number || 'Non renseigné'}</td>
                    <td style={{ padding: '10px 16px' }}>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', alignItems: 'flex-start' }}>
                        <DriverStatusBadge status={d.status} />
                        <select
                            value={d.status}
                            onChange={(e) => handleStatusChange(d.id, e.target.value)}
                            style={{ padding: '2px 6px', borderRadius: '4px', border: '1px solid #cbd5e1', cursor: 'pointer', fontSize: '11px', backgroundColor: 'white', color: '#475569' }}
                        >
                          <option value="ACTIVE">Activer</option>
                          <option value="ON_TOUR">En Tournée</option>
                          <option value="ON_LEAVE">En Congé</option>
                          <option value="SUSPENDED">Suspendre</option>
                          <option value="INACTIVE">Désactiver</option>
                        </select>
                      </div>
                    </td>
                    <td style={{ padding: '10px 16px' }}>
                      <div style={{ display: 'flex', gap: '12px' }}>
                        <button onClick={() => openModal(d)} style={{ cursor: 'pointer', background: 'none', border: 'none', color: '#64748b', display: 'flex', alignItems: 'center', padding: 0 }} title="Modifier">
                          <Edit2 size={16} />
                        </button>
                        <button onClick={() => handleDelete(d.id)} style={{ cursor: 'pointer', background: 'none', border: 'none', color: '#ef4444', display: 'flex', alignItems: 'center', padding: 0 }} title="Supprimer">
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
              ))
          )}
          </tbody>
        </table>

        {isModalOpen && (
            <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 }}>
              <div style={{ backgroundColor: 'white', padding: '30px', borderRadius: '12px', width: '450px', boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1)' }}>
                <h3 style={{ marginTop: 0, marginBottom: '20px', color: '#1e293b', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  {editingDriver ? <><Edit2 size={20} /> Modifier le Conducteur</> : <><Plus size={20} /> Nouveau Conducteur</>}
                </h3>

                {errorMessage && (
                    <div style={{ backgroundColor: '#fef2f2', color: '#991b1b', padding: '10px', borderRadius: '6px', marginBottom: '15px', fontSize: '0.9rem', border: '1px solid #f87171' }}>{errorMessage}</div>
                )}

                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  <div style={{ display: 'flex', gap: '10px' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
                      <label style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '4px', fontWeight: 'bold' }}>Prénom *</label>
                      <input style={{ padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} value={formData.first_name} onChange={e => setFormData({...formData, first_name: e.target.value})} required />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
                      <label style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '4px', fontWeight: 'bold' }}>Nom *</label>
                      <input style={{ padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} value={formData.last_name} onChange={e => setFormData({...formData, last_name: e.target.value})} required />
                    </div>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column' }}>
                    <label style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '4px', fontWeight: 'bold' }}>Email *</label>
                    <input type="email" style={{ padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} value={formData.email} onChange={e => setFormData({...formData, email: e.target.value})} required />
                  </div>

                  <div style={{ display: 'flex', gap: '10px' }}>
                    <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
                      <label style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '4px', fontWeight: 'bold' }}>Téléphone</label>
                      <input style={{ padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} placeholder="Facultatif" value={formData.phone} onChange={e => setFormData({...formData, phone: e.target.value})} />
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
                      <label style={{ fontSize: '0.85rem', color: '#64748b', marginBottom: '4px', fontWeight: 'bold' }}>Matricule</label>
                      <input style={{ padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} placeholder="Facultatif" value={formData.employee_id} onChange={e => setFormData({...formData, employee_id: e.target.value})} />
                    </div>
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '15px' }}>
                    <button type="button" onClick={closeModal} style={{ padding: '10px 15px', backgroundColor: '#f1f5f9', color: '#475569', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold' }}>Annuler</button>
                    <button type="submit" style={{ backgroundColor: '#16a34a', color: 'white', border: 'none', padding: '10px 15px', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold' }}>
                      {editingDriver ? "Mettre à jour" : "Créer"}
                    </button>
                  </div>
                </form>
              </div>
            </div>
        )}
      </div>
  );
}

const DriverStatusBadge = ({ status }: { status: string }) => {
  let color = '#475569'; let bg = '#e2e8f0'; let label = status;

  if (status === 'ACTIVE') { color = '#16a34a'; bg = '#dcfce7'; label = 'Actif'; }
  if (status === 'ON_TOUR') { color = '#2563eb'; bg = '#dbeafe'; label = 'En tournée'; }
  if (status === 'ON_LEAVE') { color = '#ca8a04'; bg = '#fef08a'; label = 'En congé'; }
  if (status === 'SUSPENDED') { color = '#dc2626'; bg = '#fee2e2'; label = 'Suspendu'; }
  if (status === 'INACTIVE') { color = '#475569'; bg = '#cbd5e1'; label = 'Inactif'; }

  return <span style={{ backgroundColor: bg, color: color, padding: '4px 8px', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 'bold' }}>{label}</span>;
};