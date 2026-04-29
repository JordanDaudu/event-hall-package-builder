import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { usePageTitle } from "../../hooks/usePageTitle";
import { listAdminRequests } from "../../api/adminPackageRequestApi";
import { useToast } from "../../contexts/ToastContext";
import { formatILS } from "../../utils/currency";
import type { PackageRequestSummaryResponse, RequestStatus } from "../../types/api";

const STATUS_LABELS: Record<string, string> = {
    PENDING: "ממתין",
    APPROVED: "אושר",
    REJECTED: "נדחה",
};

const STATUS_BADGE: Record<string, string> = {
    PENDING: "badge badge-pending",
    APPROVED: "badge badge-approved",
    REJECTED: "badge badge-rejected",
};

type FilterTab = RequestStatus | "ALL";

const TABS: { value: FilterTab; label: string }[] = [
    { value: "ALL", label: "הכל" },
    { value: "PENDING", label: "ממתין" },
    { value: "APPROVED", label: "אושר" },
    { value: "REJECTED", label: "נדחה" },
];

function formatDate(d: string) {
    return new Date(d).toLocaleDateString("he-IL", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
    });
}

export default function AdminRequestsPage() {
    usePageTitle("בקשות חבילה");
    const { showToast } = useToast();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const initialTab = (searchParams.get("status") as FilterTab | null) ?? "ALL";
    const [requests, setRequests] = useState<PackageRequestSummaryResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState<FilterTab>(initialTab);

    useEffect(() => {
        setLoading(true);
        const status = activeTab === "ALL" ? undefined : activeTab;
        listAdminRequests(status)
            .then(setRequests)
            .catch(() => showToast("שגיאה בטעינת הבקשות", "error"))
            .finally(() => setLoading(false));
    }, [activeTab]);

    return (
        <main className="page">
            <div className="page-header">
                <div>
                    <span className="eyebrow">ניהול</span>
                    <h1>בקשות לקוחות</h1>
                    <p>סקירת כל בקשות החבילה שהוגשו.</p>
                </div>
            </div>

            <div className="tab-bar" style={{ marginBottom: "20px" }}>
                {TABS.map((tab) => (
                    <button
                        key={tab.value}
                        className={`tab-btn${activeTab === tab.value ? " tab-btn-active" : ""}`}
                        onClick={() => setActiveTab(tab.value)}
                    >
                        {tab.label}
                    </button>
                ))}
            </div>

            {loading ? (
                <div className="card">
                    <p className="muted">טוען...</p>
                </div>
            ) : requests.length === 0 ? (
                <div className="card" style={{ textAlign: "center", padding: "48px 24px" }}>
                    <p className="muted" style={{ fontSize: "1.05rem" }}>
                        {activeTab === "ALL" ? "אין בקשות עדיין." : `אין בקשות בסטטוס "${STATUS_LABELS[activeTab]}".`}
                    </p>
                </div>
            ) : (
                <div className="card table-card">
                    <table className="data-table requests-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>שם לקוח</th>
                                <th>תאריך אירוע</th>
                                <th>אולם</th>
                                <th>סה"כ</th>
                                <th>הוגש ב</th>
                                <th>סטטוס</th>
                            </tr>
                        </thead>
                        <tbody>
                            {requests.map((r) => (
                                <tr
                                    key={r.id}
                                    className="clickable-row"
                                    onClick={() => navigate(`/admin/requests/${r.id}`)}
                                >
                                    <td>{r.id}</td>
                                    <td>{r.eventContactName}</td>
                                    <td>{formatDate(r.eventDate)}</td>
                                    <td>{r.venueNameSnapshot}</td>
                                    <td style={{ fontWeight: 600 }}>{formatILS(r.totalPrice)}</td>
                                    <td>{formatDate(r.submittedAt ?? r.createdAt)}</td>
                                    <td>
                                        <span className={STATUS_BADGE[r.status] ?? "badge"}>
                                            {STATUS_LABELS[r.status] ?? r.status}
                                        </span>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </main>
    );
}
