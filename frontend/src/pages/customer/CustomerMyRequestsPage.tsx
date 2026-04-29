import { useEffect, useState } from "react";
import { listOwnRequests } from "../../api/packageRequestApi";
import { usePageTitle } from "../../hooks/usePageTitle";
import { useToast } from "../../contexts/ToastContext";
import type { PackageRequestSummaryResponse } from "../../types/api";

const STATUS_LABELS: Record<string, string> = {
    PENDING: "ממתין לאישור",
    APPROVED: "אושר",
    REJECTED: "נדחה",
};

const STATUS_BADGE: Record<string, string> = {
    PENDING: "badge badge-new",
    APPROVED: "badge badge-approved",
    REJECTED: "badge badge-rejected",
};

function formatILS(n: number) {
    return n.toLocaleString("he-IL", { style: "currency", currency: "ILS", maximumFractionDigits: 0 });
}

function formatDate(d: string) {
    return new Date(d).toLocaleDateString("he-IL", { year: "numeric", month: "long", day: "numeric" });
}

export default function CustomerMyRequestsPage() {
    usePageTitle("הבקשות שלי");
    const { showToast } = useToast();
    const [requests, setRequests] = useState<PackageRequestSummaryResponse[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        listOwnRequests()
            .then(setRequests)
            .catch(() => showToast("שגיאה בטעינת הבקשות", "error"))
            .finally(() => setLoading(false));
    }, []);

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
                            </tr>
                        </thead>
                        <tbody>
                            {requests.map((r) => (
                                <tr key={r.id}>
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
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </main>
    );
}
