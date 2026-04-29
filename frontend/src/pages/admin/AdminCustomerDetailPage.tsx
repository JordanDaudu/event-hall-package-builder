import { useEffect, useState, type FormEvent } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { getCustomer } from "../../api/adminCustomerApi";
import { listPriceOverrides, setPriceOverride, deletePriceOverride } from "../../api/adminPriceOverrideApi";
import { listAllPackageOptions } from "../../api/adminPackageOptionApi";
import { useToast } from "../../contexts/ToastContext";
import Modal from "../../components/Modal";
import ConfirmDeleteDialog from "../../components/ConfirmDeleteDialog";
import { formatILS } from "../../utils/currency";
import type { CustomerResponse, PriceOverrideResponse, PackageOptionResponse } from "../../types/api";

export default function AdminCustomerDetailPage() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { showToast } = useToast();
    const customerId = Number(id);

    const [customer, setCustomer] = useState<CustomerResponse | null>(null);
    const [overrides, setOverrides] = useState<PriceOverrideResponse[]>([]);
    const [options, setOptions] = useState<PackageOptionResponse[]>([]);
    const [loading, setLoading] = useState(true);

    const [addOptionId, setAddOptionId] = useState<string>("");
    const [addPrice, setAddPrice] = useState<string>("");
    const [adding, setAdding] = useState(false);

    const [editOverride, setEditOverride] = useState<PriceOverrideResponse | null>(null);
    const [editPrice, setEditPrice] = useState<string>("");
    const [savingEdit, setSavingEdit] = useState(false);

    const [deleteOverride, setDeleteOverride] = useState<PriceOverrideResponse | null>(null);
    const [deleting, setDeleting] = useState(false);

    async function load() {
        try {
            setLoading(true);
            const [cust, ovr, opts] = await Promise.all([
                getCustomer(customerId),
                listPriceOverrides(customerId),
                listAllPackageOptions(),
            ]);
            setCustomer(cust);
            setOverrides(ovr);
            setOptions(opts.filter((o) => o.active));
        } catch {
            showToast("שגיאה בטעינת נתוני הלקוח", "error");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => { load(); }, [customerId]);

    const optionMap = Object.fromEntries(options.map((o) => [o.id, o]));
    const overrideOptionIds = new Set(overrides.map((r) => r.optionId));
    const availableOptions = options.filter((o) => !overrideOptionIds.has(o.id));

    async function handleAddOverride(e: FormEvent) {
        e.preventDefault();
        if (!addOptionId || !addPrice) return;
        setAdding(true);
        try {
            const saved = await setPriceOverride(customerId, {
                optionId: Number(addOptionId),
                customPrice: Number(addPrice),
            });
            setOverrides((prev) => {
                const filtered = prev.filter((r) => r.optionId !== saved.optionId);
                return [...filtered, saved];
            });
            setAddOptionId("");
            setAddPrice("");
            showToast("המחיר המיוחד נוסף בהצלחה");
        } catch (err: unknown) {
            const e = err as { response?: { data?: { error?: string } } };
            showToast(e?.response?.data?.error ?? "שגיאה בהוספת המחיר המיוחד", "error");
        } finally {
            setAdding(false);
        }
    }

    async function handleEditOverride(e: FormEvent) {
        e.preventDefault();
        if (!editOverride) return;
        setSavingEdit(true);
        try {
            const saved = await setPriceOverride(customerId, {
                optionId: editOverride.optionId,
                customPrice: Number(editPrice),
            });
            setOverrides((prev) =>
                prev.map((r) => (r.optionId === saved.optionId ? saved : r))
            );
            setEditOverride(null);
            setEditPrice("");
            showToast("המחיר המיוחד עודכן בהצלחה");
        } catch (err: unknown) {
            const e = err as { response?: { data?: { error?: string } } };
            showToast(e?.response?.data?.error ?? "שגיאה בעדכון המחיר המיוחד", "error");
        } finally {
            setSavingEdit(false);
        }
    }

    async function handleDeleteOverride() {
        if (!deleteOverride) return;
        setDeleting(true);
        try {
            await deletePriceOverride(customerId, deleteOverride.optionId);
            setOverrides((prev) => prev.filter((r) => r.id !== deleteOverride.id));
            setDeleteOverride(null);
            showToast("המחיר המיוחד הוסר");
        } catch {
            showToast("שגיאה במחיקת המחיר המיוחד", "error");
        } finally {
            setDeleting(false);
        }
    }

    if (loading) {
        return (
            <main className="page">
                <p className="muted">טוען...</p>
            </main>
        );
    }

    if (!customer) {
        return (
            <main className="page">
                <p>הלקוח לא נמצא.</p>
            </main>
        );
    }

    return (
        <main className="page">
            <div className="page-header">
                <div>
                    <span className="eyebrow">פרטי לקוח</span>
                    <h1>{customer.fullName}</h1>
                </div>
                <button className="button button-secondary" onClick={() => navigate("/admin/customers")}>
                    חזרה לרשימה
                </button>
            </div>

            {/* Customer info card */}
            <div className="card">
                <h2 className="section-title" style={{ marginTop: 0 }}>פרטים אישיים</h2>
                <div className="detail-grid">
                    <div className="detail-row">
                        <span className="detail-label">דוא"ל</span>
                        <span dir="ltr">{customer.email}</span>
                    </div>
                    <div className="detail-row">
                        <span className="detail-label">תעודת זהות</span>
                        <span>{customer.customerIdentityNumber ?? "—"}</span>
                    </div>
                    <div className="detail-row">
                        <span className="detail-label">טלפון</span>
                        <span>{customer.phoneNumber ?? "—"}</span>
                    </div>
                    <div className="detail-row">
                        <span className="detail-label">מחיר בסיס</span>
                        <span>{formatILS(customer.basePackagePrice)}</span>
                    </div>
                    <div className="detail-row">
                        <span className="detail-label">סטטוס</span>
                        <span className={`badge ${customer.active ? "badge-active" : "badge-inactive"}`}>
                            {customer.active ? "פעיל" : "מושבת"}
                        </span>
                    </div>
                </div>
            </div>

            {/* Price overrides */}
            <div className="card">
                <h2 className="section-title" style={{ marginTop: 0 }}>מחירים מותאמים ללקוח</h2>

                {overrides.length === 0 ? (
                    <p className="muted">אין מחירים מותאמים עדיין.</p>
                ) : (
                    <table className="data-table" style={{ marginBottom: 24 }}>
                        <thead>
                            <tr>
                                <th>אפשרות חבילה</th>
                                <th>קטגוריה</th>
                                <th>מחיר גלובלי</th>
                                <th>מחיר מותאם</th>
                                <th>פעולות</th>
                            </tr>
                        </thead>
                        <tbody>
                            {overrides.map((r) => {
                                const opt = optionMap[r.optionId];
                                return (
                                    <tr key={r.id}>
                                        <td>{opt?.nameHe ?? `אפשרות #${r.optionId}`}</td>
                                        <td>{opt ? CATEGORY_LABELS[opt.category] : "—"}</td>
                                        <td className="muted">{opt ? formatILS(opt.globalPrice) : "—"}</td>
                                        <td><strong>{formatILS(r.customPrice)}</strong></td>
                                        <td>
                                            <div className="action-row">
                                                <button
                                                    className="btn-table"
                                                    onClick={() => {
                                                        setEditOverride(r);
                                                        setEditPrice(String(r.customPrice));
                                                    }}
                                                >
                                                    עריכה
                                                </button>
                                                <button
                                                    className="btn-table btn-danger"
                                                    onClick={() => setDeleteOverride(r)}
                                                >
                                                    מחיקה
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                )}

                {/* Add override form */}
                <h3 style={{ marginBottom: 12, fontWeight: 600, fontSize: "1rem" }}>הוספת מחיר מותאם</h3>
                {availableOptions.length === 0 ? (
                    <p className="muted">כל האפשרויות הפעילות כבר מוגדרות.</p>
                ) : (
                    <form onSubmit={handleAddOverride} className="inline-form">
                        <div className="form-field" style={{ flex: 2 }}>
                            <label>אפשרות חבילה</label>
                            <select className="input select" value={addOptionId}
                                onChange={(e) => setAddOptionId(e.target.value)} required>
                                <option value="">בחר אפשרות...</option>
                                {availableOptions.map((o) => (
                                    <option key={o.id} value={o.id}>
                                        {o.nameHe} — {CATEGORY_LABELS[o.category]} — {formatILS(o.globalPrice)}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <div className="form-field" style={{ flex: 1 }}>
                            <label>מחיר מותאם (₪)</label>
                            <input className="input" type="number" min="0" required
                                value={addPrice} onChange={(e) => setAddPrice(e.target.value)} />
                        </div>
                        <div className="form-field" style={{ alignSelf: "flex-end", paddingBottom: 14 }}>
                            <button type="submit" className="button" disabled={adding}>
                                {adding ? "שומר..." : "הוספה"}
                            </button>
                        </div>
                    </form>
                )}
            </div>

            {/* Edit override modal */}
            <Modal
                open={!!editOverride}
                title={`עריכת מחיר — ${editOverride ? (optionMap[editOverride.optionId]?.nameHe ?? `אפשרות #${editOverride?.optionId}`) : ""}`}
                onClose={() => setEditOverride(null)}
                width={420}
            >
                <form onSubmit={handleEditOverride}>
                    <div className="form-field">
                        <label>מחיר גלובלי</label>
                        <p className="muted" style={{ margin: "0 0 16px" }}>
                            {editOverride ? formatILS(optionMap[editOverride.optionId]?.globalPrice ?? 0) : "—"}
                        </p>
                        <label>מחיר מותאם חדש (₪) *</label>
                        <input
                            className="input"
                            type="number"
                            min="0"
                            required
                            value={editPrice}
                            onChange={(e) => setEditPrice(e.target.value)}
                        />
                    </div>
                    <div className="modal-actions">
                        <button type="button" className="button button-secondary" onClick={() => setEditOverride(null)}>
                            ביטול
                        </button>
                        <button type="submit" className="button" disabled={savingEdit}>
                            {savingEdit ? "שומר..." : "שמירה"}
                        </button>
                    </div>
                </form>
            </Modal>

            <ConfirmDeleteDialog
                open={!!deleteOverride}
                title="מחיקת מחיר מותאם"
                message={`האם למחוק את המחיר המותאם עבור "${deleteOverride ? (optionMap[deleteOverride.optionId]?.nameHe ?? `אפשרות #${deleteOverride.optionId}`) : ""}"? הלקוח יחזור למחיר הגלובלי.`}
                onConfirm={handleDeleteOverride}
                onCancel={() => setDeleteOverride(null)}
                loading={deleting}
            />
        </main>
    );
}

const CATEGORY_LABELS: Record<string, string> = {
    CATERING: "קייטרינג",
    DECORATION: "קישוטים",
    MUSIC: "מוזיקה",
    PHOTOGRAPHY: "צילום",
    EXTRAS: "תוספות",
};
