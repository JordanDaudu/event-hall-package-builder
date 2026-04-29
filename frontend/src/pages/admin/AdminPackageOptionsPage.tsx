import { useEffect, useState, type FormEvent } from "react";
import { usePageTitle } from "../../hooks/usePageTitle";
import {
    listAllPackageOptions,
    createPackageOption,
    updatePackageOption,
    setPackageOptionActive,
    deletePackageOption,
} from "../../api/adminPackageOptionApi";
import { useToast } from "../../contexts/ToastContext";
import Modal from "../../components/Modal";
import ConfirmDeleteDialog from "../../components/ConfirmDeleteDialog";
import { formatILS } from "../../utils/currency";
import type { PackageOptionResponse, PackageOptionCategory, VisualBehavior } from "../../types/api";

const CATEGORIES: { value: PackageOptionCategory | "ALL"; label: string }[] = [
    { value: "ALL", label: "הכול" },
    { value: "CHUPPAH", label: "חופה" },
    { value: "CHUPPAH_UPGRADE", label: "תוספת לחופה" },
    { value: "AISLE", label: "שדרה" },
    { value: "TABLE_FRAME", label: "מסגרת שולחן" },
    { value: "TABLE_FLOWER", label: "פרח שולחן" },
    { value: "TABLE_CANDLE", label: "פמוט" },
    { value: "NAPKIN", label: "מפית" },
    { value: "TABLECLOTH", label: "מפה" },
    { value: "BRIDE_CHAIR", label: "כיסא כלה" },
    { value: "CATERING", label: "קייטרינג" },
    { value: "DECORATION", label: "קישוטים" },
    { value: "MUSIC", label: "מוזיקה" },
    { value: "PHOTOGRAPHY", label: "צילום" },
    { value: "EXTRAS", label: "תוספות" },
];

const CATEGORY_LABELS: Record<PackageOptionCategory, string> = {
    CHUPPAH: "חופה",
    CHUPPAH_UPGRADE: "תוספת לחופה",
    AISLE: "שדרה",
    TABLE_FRAME: "מסגרת שולחן",
    TABLE_FLOWER: "פרח שולחן",
    TABLE_CANDLE: "פמוט",
    NAPKIN: "מפית",
    TABLECLOTH: "מפה",
    BRIDE_CHAIR: "כיסא כלה",
    CATERING: "קייטרינג",
    DECORATION: "קישוטים",
    MUSIC: "מוזיקה",
    PHOTOGRAPHY: "צילום",
    EXTRAS: "תוספות",
};

const EMPTY_FORM = {
    nameHe: "",
    nameEn: "",
    category: "CATERING" as PackageOptionCategory,
    globalPrice: "",
    sortOrder: "0",
    imageUrl: "",
    visualBehavior: "" as "" | "REPLACE_IMAGE" | "OVERLAY_IMAGE" | "NO_VISUAL",
    overlayTop: "",
    overlayLeft: "",
    overlayWidth: "",
    overlayZIndex: "",
};

type OptionForm = typeof EMPTY_FORM;

export default function AdminPackageOptionsPage() {
    usePageTitle("אפשרויות חבילה");
    const { showToast } = useToast();
    const [options, setOptions] = useState<PackageOptionResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState<PackageOptionCategory | "ALL">("ALL");

    const [createOpen, setCreateOpen] = useState(false);
    const [form, setForm] = useState<OptionForm>(EMPTY_FORM);
    const [saving, setSaving] = useState(false);

    const [editTarget, setEditTarget] = useState<PackageOptionResponse | null>(null);
    const [editForm, setEditForm] = useState<OptionForm>(EMPTY_FORM);
    const [editing, setEditing] = useState(false);

    const [deleteTarget, setDeleteTarget] = useState<PackageOptionResponse | null>(null);
    const [deleting, setDeleting] = useState(false);

    const [togglingId, setTogglingId] = useState<number | null>(null);

    async function load() {
        try {
            setLoading(true);
            setOptions(await listAllPackageOptions());
        } catch {
            showToast("שגיאה בטעינת אפשרויות החבילה", "error");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => { load(); }, []);

    const filtered = activeTab === "ALL" ? options : options.filter((o) => o.category === activeTab);

    function formFromOption(o: PackageOptionResponse): OptionForm {
        return {
            nameHe: o.nameHe,
            nameEn: o.nameEn ?? "",
            category: o.category,
            globalPrice: String(o.globalPrice),
            sortOrder: String(o.sortOrder),
            imageUrl: o.imageUrl ?? "",
            visualBehavior: (o.visualBehavior ?? "") as OptionForm["visualBehavior"],
            overlayTop: o.overlayTop ?? "",
            overlayLeft: o.overlayLeft ?? "",
            overlayWidth: o.overlayWidth ?? "",
            overlayZIndex: o.overlayZIndex != null ? String(o.overlayZIndex) : "",
        };
    }

    async function handleCreate(e: FormEvent) {
        e.preventDefault();
        setSaving(true);
        try {
            const created = await createPackageOption({
                nameHe: form.nameHe,
                nameEn: form.nameEn || undefined,
                category: form.category,
                globalPrice: Number(form.globalPrice),
                sortOrder: Number(form.sortOrder),
                imageUrl: form.imageUrl || undefined,
                visualBehavior: (form.visualBehavior || undefined) as VisualBehavior | undefined,
                overlayTop: form.overlayTop || undefined,
                overlayLeft: form.overlayLeft || undefined,
                overlayWidth: form.overlayWidth || undefined,
                overlayZIndex: form.overlayZIndex ? Number(form.overlayZIndex) : undefined,
            });
            setOptions((prev) => [...prev, created]);
            setCreateOpen(false);
            setForm(EMPTY_FORM);
            showToast("האפשרות נוצרה בהצלחה");
        } catch (err: unknown) {
            const e = err as { response?: { data?: { error?: string } } };
            showToast(e?.response?.data?.error ?? "שגיאה ביצירת האפשרות", "error");
        } finally {
            setSaving(false);
        }
    }

    async function handleEdit(e: FormEvent) {
        e.preventDefault();
        if (!editTarget) return;
        setEditing(true);
        try {
            const updated = await updatePackageOption(editTarget.id, {
                nameHe: editForm.nameHe || undefined,
                nameEn: editForm.nameEn || undefined,
                category: editForm.category,
                globalPrice: Number(editForm.globalPrice),
                sortOrder: Number(editForm.sortOrder),
                imageUrl: editForm.imageUrl || undefined,
                visualBehavior: (editForm.visualBehavior || undefined) as VisualBehavior | undefined,
                overlayTop: editForm.overlayTop || undefined,
                overlayLeft: editForm.overlayLeft || undefined,
                overlayWidth: editForm.overlayWidth || undefined,
                overlayZIndex: editForm.overlayZIndex ? Number(editForm.overlayZIndex) : undefined,
            });
            setOptions((prev) => prev.map((o) => (o.id === updated.id ? updated : o)));
            setEditTarget(null);
            showToast("האפשרות עודכנה בהצלחה");
        } catch (err: unknown) {
            const e = err as { response?: { data?: { error?: string } } };
            showToast(e?.response?.data?.error ?? "שגיאה בעדכון האפשרות", "error");
        } finally {
            setEditing(false);
        }
    }

    async function toggleActive(o: PackageOptionResponse) {
        setTogglingId(o.id);
        try {
            const updated = await setPackageOptionActive(o.id, !o.active);
            setOptions((prev) => prev.map((x) => (x.id === updated.id ? updated : x)));
            showToast(updated.active ? "האפשרות הופעלה" : "האפשרות הושבתה");
        } catch {
            showToast("שגיאה בעדכון האפשרות", "error");
        } finally {
            setTogglingId(null);
        }
    }

    async function handleDelete() {
        if (!deleteTarget) return;
        setDeleting(true);
        try {
            await deletePackageOption(deleteTarget.id);
            setOptions((prev) => prev.filter((o) => o.id !== deleteTarget.id));
            setDeleteTarget(null);
            showToast("האפשרות הוסרה בהצלחה");
        } catch {
            showToast("שגיאה במחיקת האפשרות", "error");
        } finally {
            setDeleting(false);
        }
    }

    const OptionFormFields = ({ f, set }: { f: OptionForm; set: (x: OptionForm) => void }) => (
        <div className="form-grid-2">
            <div className="form-field">
                <label>שם (עברית) *</label>
                <input className="input" required value={f.nameHe}
                    onChange={(e) => set({ ...f, nameHe: e.target.value })} />
            </div>
            <div className="form-field">
                <label>שם (אנגלית)</label>
                <input className="input" dir="ltr" value={f.nameEn}
                    onChange={(e) => set({ ...f, nameEn: e.target.value })} />
            </div>
            <div className="form-field">
                <label>קטגוריה *</label>
                <select className="input select" value={f.category}
                    onChange={(e) => set({ ...f, category: e.target.value as PackageOptionCategory })}>
                    {CATEGORIES.filter((c) => c.value !== "ALL").map((c) => (
                        <option key={c.value} value={c.value}>{c.label}</option>
                    ))}
                </select>
            </div>
            <div className="form-field">
                <label>מחיר גלובלי (₪) *</label>
                <input className="input" type="number" min="0" required value={f.globalPrice}
                    onChange={(e) => set({ ...f, globalPrice: e.target.value })} />
            </div>
            <div className="form-field">
                <label>סדר תצוגה</label>
                <input className="input" type="number" min="0" value={f.sortOrder}
                    onChange={(e) => set({ ...f, sortOrder: e.target.value })} />
            </div>
            <div className="form-field" style={{ gridColumn: "1 / -1" }}>
                <label>כתובת תמונה (URL)</label>
                <input className="input" dir="ltr" placeholder="/images/chuppah.png" value={f.imageUrl}
                    onChange={(e) => set({ ...f, imageUrl: e.target.value })} />
            </div>
            <div className="form-field">
                <label>התנהגות ויזואלית</label>
                <select className="input select" value={f.visualBehavior}
                    onChange={(e) => set({ ...f, visualBehavior: e.target.value as OptionForm["visualBehavior"] })}>
                    <option value="">— ללא —</option>
                    <option value="REPLACE_IMAGE">החלפת תמונה</option>
                    <option value="OVERLAY_IMAGE">שכבת על</option>
                    <option value="NO_VISUAL">ללא תצוגה</option>
                </select>
            </div>
            {f.visualBehavior === "OVERLAY_IMAGE" && (
                <>
                    <div className="form-field">
                        <label>מיקום שכבת על — top</label>
                        <input className="input" dir="ltr" placeholder="10%" value={f.overlayTop}
                            onChange={(e) => set({ ...f, overlayTop: e.target.value })} />
                    </div>
                    <div className="form-field">
                        <label>מיקום שכבת על — left</label>
                        <input className="input" dir="ltr" placeholder="5%" value={f.overlayLeft}
                            onChange={(e) => set({ ...f, overlayLeft: e.target.value })} />
                    </div>
                    <div className="form-field">
                        <label>רוחב שכבת על</label>
                        <input className="input" dir="ltr" placeholder="90%" value={f.overlayWidth}
                            onChange={(e) => set({ ...f, overlayWidth: e.target.value })} />
                    </div>
                    <div className="form-field">
                        <label>סדר שכבה (z-index)</label>
                        <input className="input" type="number" min="0" value={f.overlayZIndex}
                            onChange={(e) => set({ ...f, overlayZIndex: e.target.value })} />
                    </div>
                </>
            )}
        </div>
    );

    return (
        <main className="page">
            <div className="page-header">
                <div>
                    <span className="eyebrow">ניהול</span>
                    <h1>אפשרויות חבילה</h1>
                </div>
                <button className="button" onClick={() => { setCreateOpen(true); setForm(EMPTY_FORM); }}>
                    + הוספת אפשרות
                </button>
            </div>

            {/* Category tabs */}
            <div className="tab-bar">
                {CATEGORIES.map((c) => (
                    <button
                        key={c.value}
                        className={`tab-btn${activeTab === c.value ? " tab-btn-active" : ""}`}
                        onClick={() => setActiveTab(c.value)}
                    >
                        {c.label}
                        <span className="tab-count">
                            {c.value === "ALL"
                                ? options.length
                                : options.filter((o) => o.category === c.value).length}
                        </span>
                    </button>
                ))}
            </div>

            <div className="card" style={{ padding: 0 }}>
                {loading ? (
                    <div className="table-empty">טוען...</div>
                ) : filtered.length === 0 ? (
                    <div className="table-empty">אין אפשרויות בקטגוריה זו</div>
                ) : (
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>שם</th>
                                <th>קטגוריה</th>
                                <th>מחיר גלובלי</th>
                                <th>סדר</th>
                                <th>סטטוס</th>
                                <th>פעולות</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filtered.map((o) => (
                                <tr key={o.id}>
                                    <td>
                                        <div><strong>{o.nameHe}</strong></div>
                                        {o.nameEn && <div className="muted" dir="ltr" style={{ fontSize: "0.82rem" }}>{o.nameEn}</div>}
                                    </td>
                                    <td>
                                        <span className="badge">{CATEGORY_LABELS[o.category]}</span>
                                    </td>
                                    <td>{formatILS(o.globalPrice)}</td>
                                    <td>{o.sortOrder}</td>
                                    <td>
                                        <span className={`badge ${o.active ? "badge-active" : "badge-inactive"}`}>
                                            {o.active ? "פעיל" : "מושבת"}
                                        </span>
                                    </td>
                                    <td>
                                        <div className="action-row">
                                            <button className="btn-table" onClick={() => { setEditTarget(o); setEditForm(formFromOption(o)); }}>עריכה</button>
                                            <button className="btn-table" onClick={() => toggleActive(o)} disabled={togglingId === o.id}>
                                                {o.active ? "השבתה" : "הפעלה"}
                                            </button>
                                            <button className="btn-table btn-danger" onClick={() => setDeleteTarget(o)}>מחיקה</button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

            <Modal open={createOpen} title="הוספת אפשרות חבילה" onClose={() => setCreateOpen(false)}>
                <form onSubmit={handleCreate}>
                    <OptionFormFields f={form} set={setForm} />
                    <div className="modal-actions">
                        <button type="button" className="button button-secondary" onClick={() => setCreateOpen(false)}>ביטול</button>
                        <button type="submit" className="button" disabled={saving}>{saving ? "שומר..." : "יצירה"}</button>
                    </div>
                </form>
            </Modal>

            <Modal open={!!editTarget} title="עריכת אפשרות חבילה" onClose={() => setEditTarget(null)}>
                <form onSubmit={handleEdit}>
                    <OptionFormFields f={editForm} set={setEditForm} />
                    <div className="modal-actions">
                        <button type="button" className="button button-secondary" onClick={() => setEditTarget(null)}>ביטול</button>
                        <button type="submit" className="button" disabled={editing}>{editing ? "שומר..." : "שמירה"}</button>
                    </div>
                </form>
            </Modal>

            <ConfirmDeleteDialog
                open={!!deleteTarget}
                title="מחיקת אפשרות חבילה"
                message={`האם אתה בטוח שברצונך למחוק את "${deleteTarget?.nameHe}"?`}
                onConfirm={handleDelete}
                onCancel={() => setDeleteTarget(null)}
                loading={deleting}
            />
        </main>
    );
}
