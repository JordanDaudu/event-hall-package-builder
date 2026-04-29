import { useEffect, useState, type FormEvent } from "react";
import {
    listAllVenues,
    createVenue,
    updateVenue,
    setVenueActive,
    deleteVenue,
} from "../../api/adminVenueApi";
import { useToast } from "../../contexts/ToastContext";
import Modal from "../../components/Modal";
import ConfirmDeleteDialog from "../../components/ConfirmDeleteDialog";
import type { VenueResponse } from "../../types/api";

const EMPTY_FORM = {
    nameHe: "",
    nameEn: "",
    descriptionHe: "",
    imageUrl: "",
    sortOrder: "0",
};

type VenueForm = typeof EMPTY_FORM;

export default function AdminVenuesPage() {
    const { showToast } = useToast();
    const [venues, setVenues] = useState<VenueResponse[]>([]);
    const [loading, setLoading] = useState(true);

    const [createOpen, setCreateOpen] = useState(false);
    const [form, setForm] = useState<VenueForm>(EMPTY_FORM);
    const [saving, setSaving] = useState(false);

    const [editTarget, setEditTarget] = useState<VenueResponse | null>(null);
    const [editForm, setEditForm] = useState<VenueForm>(EMPTY_FORM);
    const [editing, setEditing] = useState(false);

    const [deleteTarget, setDeleteTarget] = useState<VenueResponse | null>(null);
    const [deleting, setDeleting] = useState(false);

    const [togglingId, setTogglingId] = useState<number | null>(null);

    async function load() {
        try {
            setLoading(true);
            setVenues(await listAllVenues());
        } catch {
            showToast("שגיאה בטעינת האולמות", "error");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => { load(); }, []);

    function formFromVenue(v: VenueResponse): VenueForm {
        return {
            nameHe: v.nameHe,
            nameEn: v.nameEn ?? "",
            descriptionHe: v.descriptionHe ?? "",
            imageUrl: v.imageUrl ?? "",
            sortOrder: String(v.sortOrder),
        };
    }

    async function handleCreate(e: FormEvent) {
        e.preventDefault();
        setSaving(true);
        try {
            const created = await createVenue({
                nameHe: form.nameHe,
                nameEn: form.nameEn || undefined,
                descriptionHe: form.descriptionHe || undefined,
                imageUrl: form.imageUrl || undefined,
                sortOrder: Number(form.sortOrder),
            });
            setVenues((prev) => [...prev, created].sort((a, b) => a.sortOrder - b.sortOrder));
            setCreateOpen(false);
            setForm(EMPTY_FORM);
            showToast("האולם נוצר בהצלחה");
        } catch (err: unknown) {
            const e = err as { response?: { data?: { error?: string } } };
            showToast(e?.response?.data?.error ?? "שגיאה ביצירת האולם", "error");
        } finally {
            setSaving(false);
        }
    }

    async function handleEdit(e: FormEvent) {
        e.preventDefault();
        if (!editTarget) return;
        setEditing(true);
        try {
            const updated = await updateVenue(editTarget.id, {
                nameHe: editForm.nameHe,
                nameEn: editForm.nameEn || undefined,
                descriptionHe: editForm.descriptionHe || undefined,
                imageUrl: editForm.imageUrl || undefined,
                sortOrder: Number(editForm.sortOrder),
            });
            setVenues((prev) =>
                prev.map((v) => (v.id === updated.id ? updated : v)).sort((a, b) => a.sortOrder - b.sortOrder)
            );
            setEditTarget(null);
            showToast("האולם עודכן בהצלחה");
        } catch (err: unknown) {
            const e = err as { response?: { data?: { error?: string } } };
            showToast(e?.response?.data?.error ?? "שגיאה בעדכון האולם", "error");
        } finally {
            setEditing(false);
        }
    }

    async function toggleActive(v: VenueResponse) {
        setTogglingId(v.id);
        try {
            const updated = await setVenueActive(v.id, !v.active);
            setVenues((prev) => prev.map((x) => (x.id === updated.id ? updated : x)));
            showToast(updated.active ? "האולם הופעל" : "האולם הושבת");
        } catch {
            showToast("שגיאה בעדכון האולם", "error");
        } finally {
            setTogglingId(null);
        }
    }

    async function handleDelete() {
        if (!deleteTarget) return;
        setDeleting(true);
        try {
            await deleteVenue(deleteTarget.id);
            setVenues((prev) => prev.filter((v) => v.id !== deleteTarget.id));
            setDeleteTarget(null);
            showToast("האולם הוסר בהצלחה");
        } catch {
            showToast("שגיאה במחיקת האולם", "error");
        } finally {
            setDeleting(false);
        }
    }

    const VenueFormFields = ({ f, set }: { f: VenueForm; set: (x: VenueForm) => void }) => (
        <div className="form-grid-2">
            <div className="form-field">
                <label>שם האולם (עברית) *</label>
                <input className="input" required value={f.nameHe}
                    onChange={(e) => set({ ...f, nameHe: e.target.value })} />
            </div>
            <div className="form-field">
                <label>שם האולם (אנגלית)</label>
                <input className="input" dir="ltr" value={f.nameEn}
                    onChange={(e) => set({ ...f, nameEn: e.target.value })} />
            </div>
            <div className="form-field" style={{ gridColumn: "1 / -1" }}>
                <label>תיאור</label>
                <textarea className="input" rows={3} value={f.descriptionHe}
                    onChange={(e) => set({ ...f, descriptionHe: e.target.value })} />
            </div>
            <div className="form-field">
                <label>כתובת תמונה (URL)</label>
                <input className="input" dir="ltr" value={f.imageUrl}
                    onChange={(e) => set({ ...f, imageUrl: e.target.value })} />
            </div>
            <div className="form-field">
                <label>סדר תצוגה</label>
                <input className="input" type="number" min="0" value={f.sortOrder}
                    onChange={(e) => set({ ...f, sortOrder: e.target.value })} />
            </div>
        </div>
    );

    return (
        <main className="page">
            <div className="page-header">
                <div>
                    <span className="eyebrow">ניהול</span>
                    <h1>אולמות</h1>
                </div>
                <button className="button" onClick={() => { setCreateOpen(true); setForm(EMPTY_FORM); }}>
                    + הוספת אולם
                </button>
            </div>

            <div className="card" style={{ padding: 0 }}>
                {loading ? (
                    <div className="table-empty">טוען...</div>
                ) : venues.length === 0 ? (
                    <div className="table-empty">אין אולמות עדיין</div>
                ) : (
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>תמונה</th>
                                <th>שם</th>
                                <th>תיאור</th>
                                <th>סדר</th>
                                <th>סטטוס</th>
                                <th>פעולות</th>
                            </tr>
                        </thead>
                        <tbody>
                            {venues.map((v) => (
                                <tr key={v.id}>
                                    <td>
                                        {v.imageUrl ? (
                                            <img src={v.imageUrl} alt={v.nameHe} className="thumb" />
                                        ) : (
                                            <span className="thumb-placeholder">אין</span>
                                        )}
                                    </td>
                                    <td><strong>{v.nameHe}</strong>{v.nameEn && <span className="muted" dir="ltr"> ({v.nameEn})</span>}</td>
                                    <td className="muted truncate">{v.descriptionHe || "—"}</td>
                                    <td>{v.sortOrder}</td>
                                    <td>
                                        <span className={`badge ${v.active ? "badge-active" : "badge-inactive"}`}>
                                            {v.active ? "פעיל" : "מושבת"}
                                        </span>
                                    </td>
                                    <td>
                                        <div className="action-row">
                                            <button className="btn-table" onClick={() => { setEditTarget(v); setEditForm(formFromVenue(v)); }}>עריכה</button>
                                            <button className="btn-table" onClick={() => toggleActive(v)} disabled={togglingId === v.id}>
                                                {v.active ? "השבתה" : "הפעלה"}
                                            </button>
                                            <button className="btn-table btn-danger" onClick={() => setDeleteTarget(v)}>מחיקה</button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

            <Modal open={createOpen} title="הוספת אולם" onClose={() => setCreateOpen(false)}>
                <form onSubmit={handleCreate}>
                    <VenueFormFields f={form} set={setForm} />
                    <div className="modal-actions">
                        <button type="button" className="button button-secondary" onClick={() => setCreateOpen(false)}>ביטול</button>
                        <button type="submit" className="button" disabled={saving}>{saving ? "שומר..." : "יצירה"}</button>
                    </div>
                </form>
            </Modal>

            <Modal open={!!editTarget} title="עריכת אולם" onClose={() => setEditTarget(null)}>
                <form onSubmit={handleEdit}>
                    <VenueFormFields f={editForm} set={setEditForm} />
                    <div className="modal-actions">
                        <button type="button" className="button button-secondary" onClick={() => setEditTarget(null)}>ביטול</button>
                        <button type="submit" className="button" disabled={editing}>{editing ? "שומר..." : "שמירה"}</button>
                    </div>
                </form>
            </Modal>

            <ConfirmDeleteDialog
                open={!!deleteTarget}
                title="מחיקת אולם"
                message={`האם אתה בטוח שברצונך למחוק את האולם "${deleteTarget?.nameHe}"? פעולה זו אינה הפיכה.`}
                onConfirm={handleDelete}
                onCancel={() => setDeleteTarget(null)}
                loading={deleting}
            />
        </main>
    );
}
