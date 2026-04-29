import { Outlet } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import adamaLogo from "../assets/logos/adama-logo.jpeg";

const STEPS = [
    "פרטי אירוע",
    "חופה",
    "שדרה",
    "שולחנות",
    "מפיות ומפות",
    "כיסא כלה",
    "סיכום ושליחה",
];

export default function CustomerLayout() {
    const { user, logout } = useAuth();

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
                    <div className="navbar-links" style={{ marginInlineStart: "auto" }}>
                        <button className="btn-ghost" onClick={logout}>
                            התנתקות
                        </button>
                    </div>
                </div>
            </nav>

            <div className="step-bar">
                <div className="step-bar-inner">
                    {STEPS.map((label, i) => (
                        <div key={i} className="step-item step-item-pending">
                            <div className="step-circle">{i + 1}</div>
                            <span className="step-label">{label}</span>
                        </div>
                    ))}
                </div>
            </div>

            <main style={{ flex: 1 }}>
                <Outlet />
            </main>

            <footer className="footer">
                <div className="footer-inner">
                    <span className="footer-copy">
                        © {new Date().getFullYear()} אדמה — אולמות אירועים
                    </span>
                </div>
            </footer>
        </div>
    );
}
