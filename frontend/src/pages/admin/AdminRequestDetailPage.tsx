import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { usePageTitle } from "../../hooks/usePageTitle";
import { getAdminRequestById, updateAdminRequestStatus } from "../../api/adminPackageRequestApi";
import { useToast } from "../../contexts/ToastContext";
import { formatILS } from "../../utils/currency";
import Modal from "../../components/Modal";
import type { PackageRequestDetailResponse, RequestStatus } from "../../types/api";

const STATUS_LABELS: Record<RequestStatus, string> = {
    PENDING: "ממתין",
    APPROVED: "אושר",
    REJECTED: "נדחה",
};

const STATUS_BADGE: Record<RequestStatus, string> = {
    PENDING: "badge badge-new",
    APPROVED: "badge badge-approved",
    REJECTED: "badge badge-rejected",
};

function formatDate(d: string | null | undefined) {
    if (!d) return "—";
    return new Date(d).toLocaleDateString("he-IL", {
        year: "numeric",
        month: "long",
        day: "numeric",
    });
}

function formatDatetime(d: string | null | undefined) {
    if (!d) return "—";
    return new Date(d).toLocaleString("he-IL", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
    });
}

export default function AdminRequestDetailPage() {
    usePageTitle("פרטי בקשה");
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { showToast } = useToast();

    const [request, setRequest] = useState<PackageRequestDetailResponse | null>(null);
    const [loading, setLoading] = useState(true);

    const [modalAction, setModalAction] = useState<"APPROVED" | "REJECTED" | null>(null);
    const [notes, setNotes] = useState("");
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        if (!id) return;
        getAdminRequestById(Number(id))
            .then(setRequest)
            .catch(() => showToast("שגיאה בטעינת הבקשה", "error"))
            .finally(() => setLoading(false));
    }, [id]);

    async function handleConfirm() {
        if (!request || !modalAction) return;
        setSubmitting(true);
        try {
            const updated = await updateAdminRequestStatus(
                request.id,
                modalAction,
                notes.trim() || undefined
            );
            setRequest(updated);
            setModalAction(null);
            setNotes("");
            showToast(
                modalAction === "APPROVED" ? "הבקשה אושרה בהצלחה" : "הבקשה נדחתה בהצלחה"
            );
        } catch (err: unknown) {
            const status = (err as { response?: { status?: number } })?.response?.status;
            if (status === 409) {
                showToast("הבקשה כבר עודכנה — לא ניתן לשנות סטטוס שוב", "error");
            } else {
                showToast("שגיאה בעדכון הסטטוס", "error");
            }
        } finally {
            setSubmitting(false);
        }
    }

    if (loading) {
        return (
            <main className="page">
                <p className="muted">טוען...</p>
            </main>
        );
    }

    if (!request) {
        return (
            <main className="page">
                <p className="muted">הבקשה לא נמצאה.</p>
            </main>
        );
    }

    const isPending = request.status === "PENDING";

    return (
        <main className="page">
            <div className="page-header">
                <div style={{ display: "flex", alignItems: "center", gap: "12px", flexWrap: "wrap" }}>
                    <button className="button button-secondary button-sm" onClick={() => navigate("/admin/requests")}>
                        ← חזרה לרשימה
                    </button>
                    <div>
                        <span className="eyebrow">בקשה #{request.id}</span>
                        <h1 style={{ margin: 0 }}>
                            {request.eventContactName}
                            <span
                                className={STATUS_BADGE[request.status]}
                                style={{ marginInlineStart: "12px", fontSize: "0.8em", verticalAlign: "middle" }}
                            >
                                {STATUS_LABELS[request.status]}
                            </span>
                        </h1>
                    </div>
                </div>
                {isPending && (
                    <div style={{ display: "flex", gap: "10px", flexShrink: 0 }}>
                        <button
                            className="button button-success"
                            onClick={() => { setModalAction("APPROVED"); setNotes(""); }}
                        >
                            אשר ✓
                        </button>
                        <button
                            className="button button-danger"
                            onClick={() => { setModalAction("REJECTED"); setNotes(""); }}
                        >
                            דחה ✕
                        </button>
                    </div>
                )}
            </div>

            <div className="request-detail-grid">
                <div className="card">
                    <h3 className="card-section-title">פרטי האירוע</h3>
                    <div className="detail-list">
                        <div className="detail-row">
                            <span className="detail-label">שם ליצירת קשר</span>
                            <span className="detail-value">{request.eventContactName}</span>
                        </div>
                        <div className="detail-row">
                            <span className="detail-label">מספר זהות</span>
                            <span className="detail-value">{request.eventCustomerIdentityNumber}</span>
                        </div>
                        <div className="detail-row">
                            <span className="detail-label">טלפון</span>
                            <span className="detail-value">{request.eventContactPhoneNumber}</span>
                        </div>
                        <div className="detail-row">
                            <span className="detail-label">תאריך האירוע</span>
                            <span className="detail-value">{formatDate(request.eventDate)}</span>
                        </div>
                        <div className="detail-row">
                            <span className="detail-label">אולם</span>
                            <span className="detail-value">{request.venueNameSnapshot}</span>
                        </div>
                        {request.knightTableCount != null && request.knightTableCount > 0 && (
                            <div className="detail-row">
                                <span className="detail-label">שולחנות אבירים</span>
                                <span className="detail-value">{request.knightTableCount}</span>
                            </div>
                        )}
                    </div>
                </div>

                <div className="card">
                    <h3 className="card-section-title">פרטי הלקוח</h3>
                    <div className="detail-list">
                        <div className="detail-row">
                            <span className="detail-label">שם מלא</span>
                            <span className="detail-value">{request.customerFullName}</span>
                        </div>
                        <div className="detail-row">
                            <span className="detail-label">אימייל</span>
                            <span className="detail-value" dir="ltr">{request.customerEmail}</span>
                        </div>
                        <div className="detail-row">
                            <span className="detail-label">הוגש ב</span>
                            <span className="detail-value">{formatDatetime(request.submittedAt ?? request.createdAt)}</span>
                        </div>
                        {request.status === "APPROVED" && request.approvedAt && (
                            <div className="detail-row">
                                <span className="detail-label">אושר ב</span>
                                <span className="detail-value detail-value-success">{formatDatetime(request.approvedAt)}</span>
                            </div>
                        )}
                        {request.status === "REJECTED" && request.rejectedAt && (
                            <div className="detail-row">
                                <span className="detail-label">נדחה ב</span>
                                <span className="detail-value detail-value-danger">{formatDatetime(request.rejectedAt)}</span>
                            </div>
                        )}
                        {request.summaryNotes && (
                            <div className="detail-row detail-row-notes">
                                <span className="detail-label">הערות מנהל</span>
                                <span className="detail-value">{request.summaryNotes}</span>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            <div className="card table-card" style={{ marginTop: "24px" }}>
                <h3 className="card-section-title" style={{ marginBottom: "16px" }}>אפשרויות שנבחרו</h3>
                {request.items.length === 0 ? (
                    <p className="muted">לא נבחרו אפשרויות.</p>
                ) : (
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>שם האפשרות</th>
                                <th>מחיר גלובלי</th>
                                <th>מחיר סופי</th>
                                <th>הערה</th>
                            </tr>
                        </thead>
                        <tbody>
                            {request.items.map((item) => (
                                <tr key={item.id}>
                                    <td>{item.optionNameSnapshot}</td>
                                    <td>{formatILS(item.globalPriceSnapshot)}</td>
                                    <td style={{ fontWeight: 600 }}>{formatILS(item.finalPrice)}</td>
                                    <td>
                                        {item.hasCustomerOverride && (
                                            <span className="badge badge-new" style={{ fontSize: "0.75rem" }}>
                                                מחיר מותאם ללקוח
                                            </span>
                                        )}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                        <tfoot>
                            <tr className="table-total-row">
                                <td colSpan={2}>חבילה בסיסית</td>
                                <td style={{ fontWeight: 600 }}>{formatILS(request.basePackagePriceSnapshot)}</td>
                                <td />
                            </tr>
                            <tr className="table-total-row table-grand-total-row">
                                <td colSpan={2}>סה"כ</td>
                                <td style={{ fontWeight: 700 }}>{formatILS(request.totalPrice)}</td>
                                <td />
                            </tr>
                        </tfoot>
                    </table>
                )}
            </div>

            <Modal
                open={modalAction !== null}
                title={modalAction === "APPROVED" ? "אישור הבקשה" : "דחיית הבקשה"}
                onClose={() => !submitting && setModalAction(null)}
            >
                <p style={{ marginBottom: "16px" }}>
                    {modalAction === "APPROVED"
                        ? "האם לאשר את הבקשה? ניתן להוסיף הערה ללקוח."
                        : "האם לדחות את הבקשה? ניתן להוסיף הסבר ללקוח."}
                </p>
                <div className="form-field">
                    <label>הערות מנהל (לא חובה)</label>
                    <textarea
                        className="input"
                        rows={3}
                        placeholder="הערות אופציונליות..."
                        value={notes}
                        onChange={(e) => setNotes(e.target.value)}
                        disabled={submitting}
                        style={{ resize: "vertical" }}
                    />
                </div>
                <div className="modal-actions" style={{ marginTop: "20px" }}>
                    <button
                        className="button button-secondary"
                        onClick={() => setModalAction(null)}
                        disabled={submitting}
                    >
                        ביטול
                    </button>
                    <button
                        className={modalAction === "APPROVED" ? "button button-success" : "button button-danger"}
                        onClick={handleConfirm}
                        disabled={submitting}
                    >
                        {submitting
                            ? "מעדכן..."
                            : modalAction === "APPROVED"
                            ? "אשר"
                            : "דחה"}
                    </button>
                </div>
            </Modal>
        </main>
    );
}
