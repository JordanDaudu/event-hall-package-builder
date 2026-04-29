import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import adamaLogo from "../assets/logos/adama-logo.jpeg";
import jdLogo from "../assets/logos/jd-logo.png";

const NAV_LINKS = [
    { to: "/admin/customers", label: "לקוחות" },
    { to: "/admin/venues", label: "אולמות" },
    { to: "/admin/package-options", label: "אפשרויות חבילה" },
    { to: "/admin/chuppah-compatibility", label: "התאמות חופה" },
    { to: "/admin/requests", label: "בקשות" },
];

export default function AdminLayout() {
    const { user, logout } = useAuth();

    return (
        <div style={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
            <nav className="navbar">
                <div className="navbar-inner">
                    <NavLink to="/admin" className="navbar-brand" aria-label="אדמה">
                        <img src={adamaLogo} alt="אדמה" />
                    </NavLink>

                    <div className="navbar-links">
                        {NAV_LINKS.map(({ to, label }) => (
                            <NavLink
                                key={to}
                                to={to}
                                className={({ isActive }) =>
                                    isActive ? "nav-link nav-link-active" : "nav-link"
                                }
                            >
                                {label}
                            </NavLink>
                        ))}
                    </div>

                    <div className="navbar-user">
                        <span className="navbar-username">{user?.fullName}</span>
                        <button className="btn-ghost" onClick={logout}>
                            התנתקות
                        </button>
                    </div>
                </div>
            </nav>

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
