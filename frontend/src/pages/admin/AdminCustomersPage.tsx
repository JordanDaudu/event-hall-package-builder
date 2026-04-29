import { useEffect, useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { usePageTitle } from "../../hooks/usePageTitle";
import {
    listCustomers,
    createCustomer,
    updateCustomer,
    changeCustomerPassword,
    setCustomerActive,
} from "../../api/adminCustomerApi";
import { useToast } from "../../contexts/ToastContext";
import Modal from "../../components/Modal";
import { formatILS } from "../../utils/currency";
import type { CustomerResponse } from "../../types/api";

const EMPTY_CREATE = {
    fullName: "",
    email: "",
    customerIdentityNumber: "",
    phoneNumber: "",
    password: "",
    basePackagePrice: "",
};

const EMPTY_EDIT = {
    fullName: "",
    customerIdentityNumber: "",
    phoneNumber: "",
    basePackagePrice: "",
};

export default function AdminCustomersPage() {
    usePageTitle("לקוחות");
    const navigate = useNavigate();
    const { showToast } = useToast();

    const [customers, setCustomers] = useState<CustomerResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState("");

    const [createOpen, setCreateOpen] = useState(false);
    const [createForm, setCreateForm] = useState(EMPTY_CREATE);
    const [creating, setCreating] = useState(false);

    const [editTarget, setEditTarget] = useState<CustomerResponse | null>(null);
    const [editForm, setEditForm] = useState(EMPTY_EDIT);
    const [editing, setEditing] = useState(false);

    const [pwTarget, setPwTarget] = useState<CustomerResponse | null>(null);
    const [newPassword, setNewPassword] = useState("");
    const [changingPw, setChangingPw] = useState(false);

    const [togglingId, setTogglingId] = useState<number | null>(null);

    async function load() {
        try {
            setLoading(true);
            const data = await listCustomers();
            setCustomers(data);
        } catch {
            showToast("שגיאה בטעינת הלקוחות", "error");
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => { load(); }, []);

    const filtered = customers.filter((c) =>
        `${c.fullName} ${c.email}`.toLowerCase().includes(search.toLowerCase())
    );

    async function handleCreate(e: FormEvent) {
        e.preventDefault();
        setCreating(true);
        try {
            const created = await createCustomer({
                fullName: createForm.fullName,
                email: createForm.email,
                customerIdentityNumber: createForm.customerIdentityNumber || undefined,
                phoneNumber: createForm.phoneNumber || undefined,
                password: createForm.password,
                basePackagePrice: Number(createForm.basePackagePrice),
            });
            setCustomers((prev) => [...prev, created]);
            setCreateOpen(false);
            setCreateForm(EMPTY_CREATE);
            showToast("הלקוח נוצר בהצלחה");
        } catch (err: unknown) {
            const e = err as { response?: { data?: { error?: string } } };
            showToast(e?.response?.data?.error ?? "שגיאה ביצירת הלקוח", "error");
        } finally {
            setCreating(false);
        }
    }

    function openEdit(c: CustomerResponse) {
        setEditTarget(c);
        setEditForm({
            fullName: c.fullName,
            customerIdentityNumber: c.customerIdentityNumber ?? "",
            phoneNumber: c.phoneNumber ?? "",
            basePackagePrice: String(c.basePackagePrice),
        });
    }

    async function handleEdit(e: FormEvent) {
        e.preventDefault();
        if (!editTarget) return;
        setEditing(true);
        try {
            const updated = await updateCustomer(editTarget.id, {
                fullName: editForm.fullName,
                customerIdentityNumber: editForm.customerIdentityNumber || undefined,
                phoneNumber: editForm.phoneNumber || undefined,
                basePackagePrice: Number(editForm.basePackagePrice),
            });
            setCustomers((prev) => prev.map((c) => (c.id === updated.id ? updated : c)));
            setEditTarget(null);
            showToast("פרטי הלקוח עודכנו בהצלחה");
        } catch (err: unknown) {
            const e = err as { response?: { data?: { error?: string } } };
            showToast(e?.response?.data?.error ?? "שגיאה בעדכון הלקוח", "error");
        } finally {
            setEditing(false);
        }
    }

    async function handleChangePw(e: FormEvent) {
        e.preventDefault();
        if (!pwTarget) return;
        setChangingPw(true);
        try {
            await changeCustomerPassword(pwTarget.id, { newPassword });
            setPwTarget(null);
            setNewPassword("");
            showToast("הסיסמה שונתה בהצלחה");
        } catch {
            showToast("שגיאה בשינוי הסיסמה", "error");
        } finally {
            setChangingPw(false);
        }
    }

    async function toggleActive(c: CustomerResponse) {
        setTogglingId(c.id);
        try {
            const updated = await setCustomerActive(c.id, !c.active);
            setCustomers((prev) => prev.map((x) => (x.id === updated.id ? updated : x)));
            showToast(updated.active ? "הלקוח הופעל" : "הלקוח הושבת");
        } catch {
            showToast("שגיאה בעדכון סטטוס הלקוח", "error");
        } finally {
            setTogglingId(null);
        }
    }

    return (
        <main className="page">
            <div className="page-header">
                <div>
                    <span className="eyebrow">ניהול</span>
                    <h1>לקוחות</h1>
                </div>
                <button className="button" onClick={() => setCreateOpen(true)}>
                    + הוספת לקוח
                </button>
            </div>

            <div className="card" style={{ padding: "16px 20px" }}>
                <input
                    className="input"
                    style={{ maxWidth: "100%", margin: 0 }}
                    placeholder='חיפוש לפי שם או דוא"ל...'
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
            </div>

            <div className="card" style={{ padding: 0 }}>
                {loading ? (
                    <div className="table-empty">טוען...</div>
                ) : filtered.length === 0 ? (
                    <div className="table-empty">לא נמצאו לקוחות</div>
                ) : (
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>שם מלא</th>
                                <th>דוא"ל</th>
                                <th>טלפון</th>
                                <th>מחיר בסיס</th>
                                <th>סטטוס</th>
                                <th>פעולות</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filtered.map((c) => (
                                <tr key={c.id}>
                                    <td>
                                        <button
                                            className="link-btn"
                                            onClick={() => navigate(`/admin/customers/${c.id}`)}
                                        >
                                            {c.fullName}
                                        </button>
                                    </td>
                                    <td dir="ltr">{c.email}</td>
                                    <td>{c.phoneNumber ?? "—"}</td>
                                    <td>{formatILS(c.basePackagePrice)}</td>
                                    <td>
                                        <span className={`badge ${c.active ? "badge-active" : "badge-inactive"}`}>
                                            {c.active ? "פעיל" : "מושבת"}
                                        </span>
                                    </td>
                                    <td>
                                        <div className="action-row">
                                            <button className="btn-table" onClick={() => openEdit(c)}>עריכה</button>
                                            <button className="btn-table" onClick={() => { setPwTarget(c); setNewPassword(""); }}>סיסמה</button>
                                            <button
                                                className="btn-table"
                                                onClick={() => toggleActive(c)}
                                                disabled={togglingId === c.id}
                                            >
                                                {c.active ? "השבתה" : "הפעלה"}
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

            {/* Create modal */}
            <Modal open={createOpen} title="הוספת לקוח חדש" onClose={() => setCreateOpen(false)}>
                <form onSubmit={handleCreate}>
                    <div className="form-grid-2">
                        <div className="form-field">
                            <label>שם מלא *</label>
                            <input className="input" required value={createForm.fullName}
                                onChange={(e) => setCreateForm({ ...createForm, fullName: e.target.value })} />
                        </div>
                        <div className="form-field">
                            <label>דוא"ל *</label>
                            <input className="input" type="email" required dir="ltr" value={createForm.email}
                                onChange={(e) => setCreateForm({ ...createForm, email: e.target.value })} />
                        </div>
                        <div className="form-field">
                            <label>תעודת זהות</label>
                            <input className="input" value={createForm.customerIdentityNumber}
                                onChange={(e) => setCreateForm({ ...createForm, customerIdentityNumber: e.target.value })} />
                        </div>
                        <div className="form-field">
                            <label>טלפון</label>
                            <input className="input" value={createForm.phoneNumber}
                                onChange={(e) => setCreateForm({ ...createForm, phoneNumber: e.target.value })} />
                        </div>
                        <div className="form-field">
                            <label>מחיר בסיס (₪) *</label>
                            <input className="input" type="number" min="0" required value={createForm.basePackagePrice}
                                onChange={(e) => setCreateForm({ ...createForm, basePackagePrice: e.target.value })} />
                        </div>
                        <div className="form-field">
                            <label>סיסמה *</label>
                            <input className="input" type="password" required minLength={8} dir="ltr" value={createForm.password}
                                onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })} />
                        </div>
                    </div>
                    <div className="modal-actions">
                        <button type="button" className="button button-secondary" onClick={() => setCreateOpen(false)}>ביטול</button>
                        <button type="submit" className="button" disabled={creating}>{creating ? "יוצר..." : "יצירה"}</button>
                    </div>
                </form>
            </Modal>

            {/* Edit modal */}
            <Modal open={!!editTarget} title="עריכת לקוח" onClose={() => setEditTarget(null)}>
                <form onSubmit={handleEdit}>
                    <div className="form-grid-2">
                        <div className="form-field">
                            <label>שם מלא *</label>
                            <input className="input" required value={editForm.fullName}
                                onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })} />
                        </div>
                        <div className="form-field">
                            <label>תעודת זהות</label>
                            <input className="input" value={editForm.customerIdentityNumber}
                                onChange={(e) => setEditForm({ ...editForm, customerIdentityNumber: e.target.value })} />
                        </div>
                        <div className="form-field">
                            <label>טלפון</label>
                            <input className="input" value={editForm.phoneNumber}
                                onChange={(e) => setEditForm({ ...editForm, phoneNumber: e.target.value })} />
                        </div>
                        <div className="form-field">
                            <label>מחיר בסיס (₪) *</label>
                            <input className="input" type="number" min="0" required value={editForm.basePackagePrice}
                                onChange={(e) => setEditForm({ ...editForm, basePackagePrice: e.target.value })} />
                        </div>
                    </div>
                    <div className="modal-actions">
                        <button type="button" className="button button-secondary" onClick={() => setEditTarget(null)}>ביטול</button>
                        <button type="submit" className="button" disabled={editing}>{editing ? "שומר..." : "שמירה"}</button>
                    </div>
                </form>
            </Modal>

            {/* Change password modal */}
            <Modal open={!!pwTarget} title={`שינוי סיסמה — ${pwTarget?.fullName ?? ""}`} onClose={() => setPwTarget(null)} width={400}>
                <form onSubmit={handleChangePw}>
                    <div className="form-field">
                        <label>סיסמה חדשה (מינ. 8 תווים) *</label>
                        <input className="input" type="password" required minLength={8} dir="ltr"
                            value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
                    </div>
                    <div className="modal-actions">
                        <button type="button" className="button button-secondary" onClick={() => setPwTarget(null)}>ביטול</button>
                        <button type="submit" className="button" disabled={changingPw}>{changingPw ? "שומר..." : "שמירה"}</button>
                    </div>
                </form>
            </Modal>
        </main>
    );
}
