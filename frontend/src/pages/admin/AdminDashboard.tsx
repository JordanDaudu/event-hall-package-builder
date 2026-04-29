import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";
import { usePageTitle } from "../../hooks/usePageTitle";
import { listAdminRequests } from "../../api/adminPackageRequestApi";
import { listCustomers } from "../../api/adminCustomerApi";
import { listAllVenues } from "../../api/adminVenueApi";
import { formatILS } from "../../utils/currency";

export default function AdminDashboard() {
    const { user } = useAuth();
    const navigate = useNavigate();
    usePageTitle("לוח בקרה");

    const [pendingCount, setPendingCount] = useState<number | null>(null);
    const [customerCount, setCustomerCount] = useState<number | null>(null);
    const [venueCount, setVenueCount] = useState<number | null>(null);
    const [pendingTotal, setPendingTotal] = useState<number>(0);

    useEffect(() => {
        Promise.all([
            listAdminRequests("PENDING"),
            listCustomers(),
            listAllVenues(),
        ]).then(([pending, customers, venues]) => {
            setPendingCount(pending.length);
            setPendingTotal(pending.reduce((s, r) => s + r.totalPrice, 0));
            setCustomerCount(customers.length);
            setVenueCount(venues.filter((v) => v.active).length);
        }).catch(() => {
            // Non-critical; silently ignore dashboard load errors
        });
    }, []);

    function stat(val: number | null) {
        return val === null ? "..." : val.toLocaleString("he-IL");
    }

    return (
        <main className="page">
            <div className="page-header">
                <div>
                    <span className="eyebrow">לוח בקרה</span>
                    <h1>שלום, {user?.fullName}</h1>
                    <p>ברוך הבא לפאנל הניהול של אדמה — אולמות אירועים.</p>
                </div>
            </div>

            <div className="stat-row">
                <button
                    className="stat-card stat-card-clickable"
                    onClick={() => navigate("/admin/customers")}
                >
                    <span className="stat-label">לקוחות רשומים</span>
                    <strong className="stat-value">{stat(customerCount)}</strong>
                </button>
                <button
                    className="stat-card stat-card-clickable stat-card-accent"
                    onClick={() => navigate("/admin/requests?status=PENDING")}
                >
                    <span className="stat-label">בקשות ממתינות</span>
                    <strong className="stat-value">{stat(pendingCount)}</strong>
                    {pendingCount !== null && pendingCount > 0 && (
                        <span className="stat-sub">{formatILS(pendingTotal)} שווי כולל</span>
                    )}
                </button>
                <button
                    className="stat-card stat-card-clickable"
                    onClick={() => navigate("/admin/venues")}
                >
                    <span className="stat-label">אולמות פעילים</span>
                    <strong className="stat-value">{stat(venueCount)}</strong>
                </button>
            </div>

            <div className="card dashboard-quick-links">
                <h3 style={{ marginBottom: "18px" }}>קישורים מהירים</h3>
                <div className="quick-link-grid">
                    <button className="quick-link-btn" onClick={() => navigate("/admin/customers")}>
                        <span className="quick-link-icon">👥</span>
                        <span>ניהול לקוחות</span>
                    </button>
                    <button className="quick-link-btn" onClick={() => navigate("/admin/requests")}>
                        <span className="quick-link-icon">📋</span>
                        <span>בקשות חבילה</span>
                    </button>
                    <button className="quick-link-btn" onClick={() => navigate("/admin/venues")}>
                        <span className="quick-link-icon">🏛</span>
                        <span>ניהול אולמות</span>
                    </button>
                    <button className="quick-link-btn" onClick={() => navigate("/admin/package-options")}>
                        <span className="quick-link-icon">📦</span>
                        <span>אפשרויות חבילה</span>
                    </button>
                </div>
            </div>
        </main>
    );
}
