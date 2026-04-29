import { Fragment, useEffect, useState } from "react";
import { listOwnRequests, getOwnRequestById } from "../../api/packageRequestApi";
import { usePageTitle } from "../../hooks/usePageTitle";
import { useToast } from "../../contexts/ToastContext";
import type { PackageRequestSummaryResponse, PackageRequestDetailResponse } from "../../types/api";

const STATUS_LABELS: Record<string, string> = {
    PENDING: "ממתין לאישור",
    APPROVED: "אושר",
    REJECTED: "נדחה",
};

const STATUS_BADGE: Record<string, string> = {
    PENDING: "badge badge-pending",
    APPROVED: "badge badge-approved",
    REJECTED: "badge badge-rejected",
};

function formatILS(n: number) {
    return n.toLocaleString("he-IL", { style: "currency", currency: "ILS", maximumFractionDigits: 0 });
}

function formatDate(d: string) {
    return new Date(d).toLocaleDateString("he-IL", { year: "numeric", month: "long", day: "numeric" });
}

function ChuppahDetail({ detail }: { detail: PackageRequestDetailResponse }) {
    const chuppahItem = detail.items.find((i) => i.category === "CHUPPAH");
    const chuppahUpgrades = detail.items.filter((i) => i.category === "CHUPPAH_UPGRADE");

    if (!chuppahItem && chuppahUpgrades.length === 0) {
        return <p className="muted" style={{ fontSize: "0.9rem" }}>אין מידע על חופה.</p>;
    }

    return (
        <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
            {chuppahItem && (
                <div style={{ display: "flex", gap: "8px", flexWrap: "wrap", alignItems: "center" }}>
                    <span style={{ fontWeight: 600 }}>חופה:</span>
                    <span>{chuppahItem.optionNameSnapshot}</span>
                    <span style={{ color: "var(--color-muted)", fontSize: "0.9rem" }}>
                        {formatILS(chuppahItem.finalPrice)}
                    </span>
                    {chuppahItem.hasCustomerOverride && (
                        <span className="badge badge-active" style={{ fontSize: "0.72rem" }}>מחיר מותאם</span>
                    )}
                </div>
            )}
            {chuppahUpgrades.length > 0 && (
                <div style={{ display: "flex", gap: "8px", flexWrap: "wrap", alignItems: "flex-start" }}>
                    <span style={{ fontWeight: 600 }}>תוספות:</span>
                    <ul style={{ margin: 0, padding: 0, listStyle: "none", display: "flex", flexDirection: "column", gap: "4px" }}>
                        {chuppahUpgrades.map((u) => (
                            <li key={u.id} style={{ display: "flex", gap: "8px", flexWrap: "wrap", alignItems: "center" }}>
                                <span>{u.optionNameSnapshot}</span>
                                <span style={{ color: "var(--color-muted)", fontSize: "0.9rem" }}>
                                    {formatILS(u.finalPrice)}
                                </span>
                                {u.hasCustomerOverride && (
                                    <span className="badge badge-active" style={{ fontSize: "0.72rem" }}>מחיר מותאם</span>
                                )}
                            </li>
                        ))}
                    </ul>
                </div>
            )}
            {chuppahUpgrades.length === 0 && chuppahItem && (
                <div style={{ display: "flex", gap: "8px" }}>
                    <span style={{ fontWeight: 600 }}>תוספות:</span>
                    <span className="muted">ללא תוספות</span>
                </div>
            )}
        </div>
    );
}

export default function CustomerMyRequestsPage() {
    usePageTitle("הבקשות שלי");
    const { showToast } = useToast();
    const [requests, setRequests] = useState<PackageRequestSummaryResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [expandedId, setExpandedId] = useState<number | null>(null);
    const [detailCache, setDetailCache] = useState<Record<number, PackageRequestDetailResponse>>({});
    const [detailLoading, setDetailLoading] = useState<number | null>(null);

    useEffect(() => {
        listOwnRequests()
            .then(setRequests)
            .catch(() => showToast("שגיאה בטעינת הבקשות", "error"))
            .finally(() => setLoading(false));
    }, []);

    async function handleToggle(id: number) {
        if (expandedId === id) {
            setExpandedId(null);
            return;
        }
        setExpandedId(id);
        if (!detailCache[id]) {
            setDetailLoading(id);
            try {
                const detail = await getOwnRequestById(id);
                setDetailCache((prev) => ({ ...prev, [id]: detail }));
            } catch {
                showToast("שגיאה בטעינת פרטי הבקשה", "error");
            } finally {
                setDetailLoading(null);
            }
        }
    }

    if (loading) {
        return (
            <main className="page">
                <p className="muted">טוען...</p>
            </main>
        );
    }

    return (
        <main className="page">
            <div className="page-header">
                <div>
                    <span className="eyebrow">הבקשות שלי</span>
                    <h1>בקשות החבילה שלי</h1>
                    <p>רשימת כל בקשות האירוע שהגשת.</p>
                </div>
            </div>

            {requests.length === 0 ? (
                <div className="card" style={{ textAlign: "center", padding: "48px 24px" }}>
                    <p className="muted" style={{ fontSize: "1.05rem" }}>
                        עדיין לא הגשת בקשות. לחץ על "בניית חבילה" כדי להתחיל.
                    </p>
                </div>
            ) : (
                <div className="card table-card">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>תאריך אירוע</th>
                                <th>אולם</th>
                                <th>שם ליצירת קשר</th>
                                <th>סה"כ</th>
                                <th>סטטוס</th>
                                <th>הוגש ב</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {requests.map((r) => (
                                <Fragment key={r.id}>
                                    <tr>
                                        <td>{r.id}</td>
                                        <td>{formatDate(r.eventDate)}</td>
                                        <td>{r.venueNameSnapshot}</td>
                                        <td>{r.eventContactName}</td>
                                        <td style={{ fontWeight: 600 }}>{formatILS(r.totalPrice)}</td>
                                        <td>
                                            <span className={STATUS_BADGE[r.status] ?? "badge"}>
                                                {STATUS_LABELS[r.status] ?? r.status}
                                            </span>
                                        </td>
                                        <td>{formatDate(r.submittedAt ?? r.createdAt)}</td>
                                        <td>
                                            <button
                                                className="button button-secondary button-sm"
                                                onClick={() => handleToggle(r.id)}
                                                style={{ whiteSpace: "nowrap" }}
                                            >
                                                {expandedId === r.id ? "▲ סגור" : "▼ חופה"}
                                            </button>
                                        </td>
                                    </tr>
                                    {expandedId === r.id && (
                                        <tr>
                                            <td colSpan={8} style={{ background: "var(--color-surface-alt, var(--color-surface))", padding: "16px 20px" }}>
                                                <div style={{ fontWeight: 700, marginBottom: "10px", fontSize: "0.95rem" }}>
                                                    חופה + תוספות
                                                </div>
                                                {detailLoading === r.id ? (
                                                    <p className="muted" style={{ fontSize: "0.9rem" }}>טוען...</p>
                                                ) : detailCache[r.id] ? (
                                                    <ChuppahDetail detail={detailCache[r.id]} />
                                                ) : null}
                                            </td>
                                        </tr>
                                    )}
                                </Fragment>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </main>
    );
}
