import { useEffect, useState } from "react";
import { getAdminDashboard } from "../api/adminDashboardApi";
import type { AdminDashboardResponse } from "../types/api";

const monthNames = [
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
];

function AdminDashboardPage() {
    const currentYear = new Date().getFullYear();

    const [year, setYear] = useState(currentYear);
    const [dashboard, setDashboard] = useState<AdminDashboardResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        async function loadDashboard() {
            try {
                setLoading(true);
                setError(null);

                const data = await getAdminDashboard(year);
                setDashboard(data);
            } catch {
                setError("Failed to load dashboard data.");
            } finally {
                setLoading(false);
            }
        }

        loadDashboard();
    }, [year]);

    if (loading) {
        return (
            <main className="page">
                <p>Loading dashboard...</p>
            </main>
        );
    }

    if (error) {
        return (
            <main className="page">
                <section className="card">
                    <p>{error}</p>
                </section>
            </main>
        );
    }

    if (!dashboard) {
        return (
            <main className="page">
                <p>Dashboard data not found.</p>
            </main>
        );
    }

    const currentMonth = new Date().getMonth() + 1;

    const thisMonthRevenue =
        dashboard.revenueByMonth[String(currentMonth)] ??
        dashboard.revenueByMonth[currentMonth] ??
        0;

    const conversionRate =
        dashboard.totalQuotes === 0
            ? 0
            : Math.round((dashboard.approvedQuotes / dashboard.totalQuotes) * 100);

    return (
        <main className="page">
            <div className="page-header">
                <div>
                    <h1>Admin Dashboard</h1>
                    <p>Track quote activity and approved revenue.</p>
                </div>

                <label>
                    Year{" "}
                    <select
                        className="select table-select"
                        value={year}
                        onChange={(event) => setYear(Number(event.target.value))}
                    >
                        <option value={currentYear}>{currentYear}</option>
                        <option value={currentYear - 1}>{currentYear - 1}</option>
                        <option value={currentYear - 2}>{currentYear - 2}</option>
                    </select>
                </label>
            </div>

            <section className="stat-row">
                <div className="stat-card">
                    <span>Total Quotes</span>
                    <strong>{dashboard.totalQuotes}</strong>
                </div>

                <div className="stat-card">
                    <span>Approved Quotes</span>
                    <strong>{dashboard.approvedQuotes}</strong>
                </div>

                <div className="stat-card">
                    <span>Approved Revenue</span>
                    <strong>{dashboard.totalRevenue.toLocaleString()}</strong>
                </div>

                <div className="stat-card">
                    <span>This Month Revenue</span>
                    <strong>{thisMonthRevenue.toLocaleString()}</strong>
                </div>

                <div className="stat-card">
                    <span>Conversion Rate</span>
                    <strong>{conversionRate}%</strong>
                </div>
            </section>

            <section className="card">
                <h2>Revenue by Month</h2>

                <div className="month-grid">
                    {monthNames.map((month, index) => {
                        const monthNumber = String(index + 1);
                        const revenue =
                            dashboard.revenueByMonth[monthNumber] ??
                            dashboard.revenueByMonth[index + 1] ??
                            0;

                        return (
                            <div key={month} className="month-card">
                                <span>{month}</span>
                                <strong>{revenue.toLocaleString()}</strong>
                            </div>
                        );
                    })}
                </div>
            </section>

            <section className="card">
                <h2>Revenue by Event Type</h2>

                {dashboard.revenueByEventType.length === 0 ? (
                    <p className="muted">No data available.</p>
                ) : (
                    <div className="list-grid">
                        {dashboard.revenueByEventType.map((item) => (
                            <div key={item.eventTypeName} className="list-item">
                                <span>{item.eventTypeName}</span>
                                <strong>{item.revenue.toLocaleString()}</strong>
                            </div>
                        ))}
                    </div>
                )}
            </section>

            <section className="card">
                <h2>Top Selected Upgrades</h2>

                {dashboard.topUpgrades.length === 0 ? (
                    <p className="muted">No data available.</p>
                ) : (
                    <div className="list-grid">
                        {dashboard.topUpgrades.map((item) => (
                            <div key={item.upgradeName} className="list-item">
                                <span>{item.upgradeName}</span>
                                <strong>{item.count}</strong>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </main>
    );
}

export default AdminDashboardPage;