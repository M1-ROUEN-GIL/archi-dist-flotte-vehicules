import { useState } from 'react';
import { useQuery, useMutation } from '@apollo/client';
import { GET_MAINTENANCES, CREATE_MAINTENANCE, UPDATE_MAINTENANCE_RECORD, UPDATE_MAINTENANCE_STATUS } from './queries';
import { Edit2, Plus } from 'lucide-react';

export default function MaintenanceList() {
  const { loading, error, data, refetch } = useQuery(GET_MAINTENANCES, { fetchPolicy: 'network-only' });
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // Modales
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [selectedRecord, setSelectedRecord] = useState<any>(null);

  // Mutations
  const [createRecord] = useMutation(CREATE_MAINTENANCE, { onCompleted: () => { refetch(); closeModals(); } });
  const [updateRecord] = useMutation(UPDATE_MAINTENANCE_RECORD);
  const [updateStatus] = useMutation(UPDATE_MAINTENANCE_STATUS);
  // États des formulaires
  const [createForm, setCreateForm] = useState({ vehicle_id: '', type: 'PREVENTIVE', priority: 'MEDIUM', scheduled_date: '', description: '' });
  const [editForm, setEditForm] = useState({
    status: '', type: '', priority: '', scheduled_date: '', completed_date: '',
    cost_eur: 0, mileage_at_service: 0, next_service_km: 0, parts_used: '', notes: '', technician_id: ''
  });

  const openCreateModal = () => {
    setErrorMessage(null);
    setCreateForm({ vehicle_id: '', type: 'PREVENTIVE', priority: 'MEDIUM', scheduled_date: new Date().toISOString().split('T')[0], description: '' });
    setIsCreateModalOpen(true);
  };

  const openEditModal = (record: any) => {
    setErrorMessage(null);
    setSelectedRecord(record);
    setEditForm({
      status: record.status || '',
      type: record.type || '',
      priority: record.priority || '',
      scheduled_date: record.scheduled_date ? record.scheduled_date.split('T')[0] : '',
      completed_date: record.completed_date ? record.completed_date.split('T')[0] : '',
      cost_eur: record.cost_eur || 0,
      mileage_at_service: record.mileage_at_service || 0,
      next_service_km: record.next_service_km || 0,
      parts_used: record.parts_used ? record.parts_used.join(', ') : '',
      notes: record.notes || '',
      technician_id: record.technician_id || ''
    });
    setIsEditModalOpen(true);
  };

  const closeModals = () => {
    setIsCreateModalOpen(false);
    setIsEditModalOpen(false);
    setErrorMessage(null);
  };

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    try {
      await createRecord({
        variables: {
          ...createForm,
          scheduled_date: createForm.scheduled_date + "T00:00:00Z",
          description: createForm.description.trim() === '' ? null : createForm.description
        }
      });
    } catch (err: any) { setErrorMessage("Erreur création : " + err.message); }
  };
  const handleEditSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);
    try {
      const partsArray = editForm.parts_used.split(',').map(p => p.trim()).filter(p => p !== '');

      // 1. On lance la mise à jour des détails (PUT)
      const updateDetailsPromise = updateRecord({
        variables: {
          id: selectedRecord.id,
          technician_id: editForm.technician_id === '' ? null : editForm.technician_id,
          type: editForm.type,
          priority: editForm.priority,
          scheduled_date: editForm.scheduled_date ? editForm.scheduled_date + "T00:00:00Z" : null,
          completed_date: editForm.completed_date ? editForm.completed_date + "T00:00:00Z" : null,
          mileage_at_service: parseInt(editForm.mileage_at_service.toString()) || null,
          next_service_km: parseInt(editForm.next_service_km.toString()) || null,
          parts_used: partsArray.length > 0 ? partsArray : null,
        }
      });

      // 2. On lance la mise à jour du Statut, Coût et Notes (PATCH)
      const updateStatusPromise = updateStatus({
        variables: {
          id: selectedRecord.id,
          status: editForm.status,
          cost_eur: parseFloat(editForm.cost_eur.toString()) || null,
          notes: editForm.notes.trim() === '' ? null : editForm.notes
        }
      });

      // On attend que les deux opérations finissent, puis on rafraîchit la page
      await Promise.all([updateDetailsPromise, updateStatusPromise]);
      refetch();
      closeModals();

    } catch (err: any) {
      setErrorMessage("Erreur mise à jour : " + err.message);
    }
  };

  if (loading) return <div style={{ padding: '2rem' }}>⏳ Chargement de la maintenance...</div>;
  if (error) return <div style={{ padding: '2rem', color: '#dc2626' }}>❌ Erreur GraphQL : {error.message}</div>;

  const records = data?.maintenanceRecords?.items || [];

  return (
      <div style={{ padding: '0', fontFamily: 'sans-serif' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px', alignItems: 'center' }}>
          <h2 style={{ color: '#0f172a', margin: 0 }}>Interventions & Maintenance</h2>
          <button onClick={openCreateModal} style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '10px 20px', backgroundColor: '#0f172a', color: 'white', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: '500', fontSize: '14px' }}>
            <Plus size={16} /> Planifier
          </button>
        </div>

        <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: 'white', border: '1px solid #e2e8f0', borderRadius: '8px', overflow: 'hidden', fontSize: '14px' }}>
          <thead style={{ backgroundColor: '#f8fafc', color: '#64748b', textAlign: 'left', fontSize: '12px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
          <tr>
            <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>Véhicule</th>
            <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>Intervention</th>
            <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>Date prévue</th>
            <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>Statut & Coût</th>
            <th style={{ padding: '10px 16px', borderBottom: '1px solid #e2e8f0' }}>Actions</th>
          </tr>
          </thead>
          <tbody>
          {records.length === 0 ? (
              <tr><td colSpan={5} style={{ textAlign: 'center', padding: '20px', color: '#94a3b8' }}>Aucune intervention trouvée.</td></tr>
          ) : (
              records.map((r: any) => (
                  <tr key={r.id} style={{ borderBottom: '1px solid #f1f5f9' }}>
                    <td style={{ padding: '10px 16px', fontWeight: '500', fontFamily: 'monospace', color: '#0f172a' }}>
                      {r.vehicle?.plate_number || r.vehicle?.id?.substring(0,8) + '...'}
                    </td>
                    <td style={{ padding: '10px 16px', fontSize: '13px', color: '#475569' }}>
                      <strong>{r.type}</strong><br/>
                      <span style={{ color: r.priority === 'HIGH' || r.priority === 'CRITICAL' ? '#ef4444' : '#64748b' }}>
                    Priorité: {r.priority}
                  </span>
                    </td>
                    <td style={{ padding: '10px 16px', color: '#475569' }}>{new Date(r.scheduled_date).toLocaleDateString()}</td>
                    <td style={{ padding: '10px 16px' }}>
                      <StatusBadge status={r.status} /><br/>
                      {r.cost_eur ? <span style={{ fontSize: '12px', color: '#16a34a', fontWeight: 'bold', marginTop: '4px', display: 'block' }}>{r.cost_eur} €</span> : null}
                    </td>
                    <td style={{ padding: '10px 16px' }}>
                      <button onClick={() => openEditModal(r)} style={{ cursor: 'pointer', background: 'none', border: 'none', color: '#64748b', display: 'flex', alignItems: 'center', padding: 0 }} title="Éditer">
                        <Edit2 size={16} />
                      </button>
                    </td>
                  </tr>
              ))
          )}
          </tbody>
        </table>

        {/* ================= MODALE DE CRÉATION ================= */}
        {isCreateModalOpen && (
            <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 }}>
              <div style={{ backgroundColor: 'white', padding: '30px', borderRadius: '12px', width: '450px' }}>
                <h3 style={{ marginTop: 0, color: '#1e293b', display: 'flex', alignItems: 'center', gap: '8px' }}><Plus size={20} /> Planifier une intervention</h3>
                {errorMessage && <div style={{ color: '#991b1b', backgroundColor: '#fef2f2', padding: '10px', marginBottom: '15px', borderRadius: '5px' }}>{errorMessage}</div>}

                <form onSubmit={handleCreateSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  <div>
                    <label style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#64748b' }}>ID du Véhicule *</label>
                    <input style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} value={createForm.vehicle_id} onChange={e => setCreateForm({...createForm, vehicle_id: e.target.value})} required />
                  </div>

                  <div style={{ display: 'flex', gap: '10px' }}>
                    <div style={{ flex: 1 }}>
                      <label style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#64748b' }}>Type *</label>
                      <select style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} value={createForm.type} onChange={e => setCreateForm({...createForm, type: e.target.value})}>
                        <option value="PREVENTIVE">Préventive</option>
                        <option value="CORRECTIVE">Corrective</option>
                        <option value="INSPECTION">Inspection</option>
                      </select>
                    </div>
                    <div style={{ flex: 1 }}>
                      <label style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#64748b' }}>Priorité *</label>
                      <select style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} value={createForm.priority} onChange={e => setCreateForm({...createForm, priority: e.target.value})}>
                        <option value="LOW">Basse</option>
                        <option value="MEDIUM">Moyenne</option>
                        <option value="HIGH">Haute</option>
                        <option value="CRITICAL">Critique</option>
                      </select>
                    </div>
                  </div>

                  <div>
                    <label style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#64748b' }}>Date prévue *</label>
                    <input type="date" style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} value={createForm.scheduled_date} onChange={e => setCreateForm({...createForm, scheduled_date: e.target.value})} required />
                  </div>

                  <div>
                    <label style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#64748b' }}>Description du problème</label>
                    <textarea style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} rows={3} value={createForm.description} onChange={e => setCreateForm({...createForm, description: e.target.value})} />
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
                    <button type="button" onClick={closeModals} style={{ padding: '10px 15px', backgroundColor: '#f1f5f9', color: '#475569', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold' }}>Annuler</button>
                    <button type="submit" style={{ padding: '10px 15px', backgroundColor: '#16a34a', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold' }}>Planifier</button>
                  </div>
                </form>
              </div>
            </div>
        )}

        {/* ================= MODALE D'ÉDITION COMPLÈTE ================= */}
        {isEditModalOpen && (
            <div style={{ position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000, overflowY: 'auto' }}>
              <div style={{ backgroundColor: 'white', padding: '30px', borderRadius: '12px', width: '600px', maxHeight: '90vh', overflowY: 'auto', margin: '20px' }}>
                <h3 style={{ marginTop: 0, color: '#1e293b', display: 'flex', alignItems: 'center', gap: '8px' }}><Edit2 size={20} /> Détails & Clôture de l'intervention</h3>
                {errorMessage && <div style={{ color: '#991b1b', backgroundColor: '#fef2f2', padding: '10px', marginBottom: '15px', borderRadius: '5px' }}>{errorMessage}</div>}

                <form onSubmit={handleEditSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>

                  {/* Ligne 1 : Statut et Dates */}
                  <div style={{ display: 'flex', gap: '10px', paddingBottom: '10px', borderBottom: '1px solid #e2e8f0' }}>
                    <div style={{ flex: 1 }}>
                      <label style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#64748b' }}>Statut</label>
                      <select style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} value={editForm.status} onChange={e => setEditForm({...editForm, status: e.target.value})}>
                        <option value="SCHEDULED">Planifié</option>
                        <option value="IN_PROGRESS">En cours</option>
                        <option value="COMPLETED">Terminé</option>
                        <option value="CANCELLED">Annulé</option>
                      </select>
                    </div>
                    <div style={{ flex: 1 }}>
                      <label style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#64748b' }}>Date fin réelle</label>
                      <input type="date" style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} value={editForm.completed_date} onChange={e => setEditForm({...editForm, completed_date: e.target.value})} />
                    </div>
                  </div>

                  {/* Ligne 2 : Type et Technicien */}
                  <div style={{ display: 'flex', gap: '10px' }}>
                    <div style={{ flex: 1 }}>
                      <label style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#64748b' }}>Type</label>
                      <select style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px' }} value={editForm.type} onChange={e => setEditForm({...editForm, type: e.target.value})}>
                        <option value="PREVENTIVE">Préventive</option>
                        <option value="CORRECTIVE">Corrective</option>
                        <option value="INSPECTION">Inspection</option>
                      </select>
                    </div>
                    <div style={{ flex: 1 }}>
                      <label style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#64748b' }}>ID Technicien</label>
                      <input type="text" style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} value={editForm.technician_id} onChange={e => setEditForm({...editForm, technician_id: e.target.value})} placeholder="Facultatif" />
                    </div>
                  </div>

                  {/* Ligne 3 : KMs et Coût */}
                  <div style={{ display: 'flex', gap: '10px' }}>
                    <div style={{ flex: 1 }}>
                      <label style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#64748b' }}>KM Actuel</label>
                      <input type="number" style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} value={editForm.mileage_at_service} onChange={e => setEditForm({...editForm, mileage_at_service: parseInt(e.target.value)})} />
                    </div>
                    <div style={{ flex: 1 }}>
                      <label style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#64748b' }}>Prochain KM prévu</label>
                      <input type="number" style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} value={editForm.next_service_km} onChange={e => setEditForm({...editForm, next_service_km: parseInt(e.target.value)})} />
                    </div>
                    <div style={{ flex: 1 }}>
                      <label style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#64748b' }}>Coût Total (€)</label>
                      <input type="number" step="0.01" style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} value={editForm.cost_eur} onChange={e => setEditForm({...editForm, cost_eur: parseFloat(e.target.value)})} />
                    </div>
                  </div>

                  {/* Ligne 4 : Pièces */}
                  <div>
                    <label style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#64748b' }}>Pièces remplacées (séparées par une virgule)</label>
                    <input type="text" style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} value={editForm.parts_used} onChange={e => setEditForm({...editForm, parts_used: e.target.value})} placeholder="Ex: Filtre à huile, Pneus avant, Plaquettes" />
                  </div>

                  {/* Ligne 5 : Notes */}
                  <div>
                    <label style={{ fontSize: '0.85rem', fontWeight: 'bold', color: '#64748b' }}>Notes du mécanicien</label>
                    <textarea style={{ width: '100%', padding: '8px', border: '1px solid #cbd5e1', borderRadius: '4px', boxSizing: 'border-box' }} rows={3} value={editForm.notes} onChange={e => setEditForm({...editForm, notes: e.target.value})} />
                  </div>

                  <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '10px' }}>
                    <button type="button" onClick={closeModals} style={{ padding: '10px 15px', backgroundColor: '#f1f5f9', color: '#475569', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold' }}>Annuler</button>
                    <button type="submit" style={{ padding: '10px 15px', backgroundColor: '#16a34a', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer', fontWeight: 'bold' }}>Enregistrer</button>
                  </div>
                </form>
              </div>
            </div>
        )}
      </div>
  );
}

const StatusBadge = ({ status }: { status: string }) => {
  let color = '#475569'; let bg = '#e2e8f0'; let label = status;

  if (status === 'SCHEDULED') { color = '#2563eb'; bg = '#dbeafe'; label = 'Planifié'; }
  if (status === 'IN_PROGRESS') { color = '#ca8a04'; bg = '#fef08a'; label = 'En cours'; }
  if (status === 'COMPLETED') { color = '#16a34a'; bg = '#dcfce7'; label = 'Terminé'; }
  if (status === 'CANCELLED') { color = '#dc2626'; bg = '#fee2e2'; label = 'Annulé'; }

  return <span style={{ backgroundColor: bg, color: color, padding: '4px 8px', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 'bold' }}>{label}</span>;
};