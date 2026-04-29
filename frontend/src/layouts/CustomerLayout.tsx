import { Link, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { BuilderUiProvider, useBuilderUi } from "../contexts/BuilderUiContext";
import adamaLogo from "../assets/logos/adama-logo.jpeg";
import jdLogo from "../assets/logos/jd-logo.png";

const STEPS = [
    "פרטי אירוע",
    "חופה",
    "שדרה",
    "שולחנות",
    "מפיות ומפות",
    "כיסא כלה",
    "סיכום ושליחה",
];

function formatILS(n: number) {
    return n.toLocaleString("he-IL", { style: "currency", currency: "ILS", maximumFractionDigits: 0 });
}

function CustomerLayoutInner() {
    const { user, logout } = useAuth();
    const location = useLocation();
    const { currentStep, runningTotal, isBuilderActive } = useBuilderUi();
    const isBuilder = location.pathname === "/customer" || location.pathname === "/customer/";

    function stepClass(i: number) {
        if (!isBuilder || !isBuilderActive) return "step-item step-item-pending";
        if (i < currentStep) return "step-item step-item-done";
        if (i === currentStep) return "step-item step-item-active";
        return "step-item step-item-pending";
    }

    return (
        <div style={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
            <nav className="navbar">
                <div className="navbar-inner">
                    <div className="navbar-brand">
                        <img src={adamaLogo} alt="אדמה" />
                    </div>
                    <div className="navbar-greeting">
                        שלום, <strong>{user?.fullName}</strong>
                    </div>
                    <div className="navbar-links" style={{ marginInlineStart: "auto", gap: "16px" }}>
                        <Link to="/customer" className="navbar-customer-link">
                            בניית חבילה
                        </Link>
                        <Link to="/customer/my-requests" className="navbar-customer-link">
                            הבקשות שלי
                        </Link>
                        <button className="btn-ghost" onClick={logout}>
                            התנתקות
                        </button>
                    </div>
                </div>
            </nav>

            {isBuilder && (
                <div className="step-bar">
                    <div className="step-bar-inner">
                        {STEPS.map((label, i) => (
                            <div key={i} className={stepClass(i)}>
                                <div className="step-circle">
                                    {isBuilderActive && i < currentStep ? "✓" : i + 1}
                                </div>
                                <span className="step-label">{label}</span>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {isBuilder && isBuilderActive && (
                <div className="running-total-bar">
                    <span className="running-total-label">סה"כ משוער:</span>
                    <span className="running-total-amount">{formatILS(runningTotal)}</span>
                </div>
            )}

            <main style={{ flex: 1 }}>
                <Outlet />
            </main>

            <footer className="footer">
                <div className="footer-inner">
                    <span className="footer-copy">
                        © {new Date().getFullYear()} אדמה — אולמות אירועים
                    </span>
                    <div className="footer-credit">
                        <span>Designed &amp; Built by</span>
                        <img src={jdLogo} alt="JD" />
                    </div>
                </div>
            </footer>
        </div>
    );
}

export default function CustomerLayout() {
    return (
        <BuilderUiProvider>
            <CustomerLayoutInner />
        </BuilderUiProvider>
    );
}
