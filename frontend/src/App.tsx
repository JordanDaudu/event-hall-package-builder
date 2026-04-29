import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
import HomePage from "./pages/HomePage";
import PackageBuilderPage from "./pages/PackageBuilderPage";
import QuoteSummaryPage from "./pages/QuoteSummaryPage";
import AdminQuotesPage from "./pages/AdminQuotesPage";
import AdminUpgradesPage from "./pages/AdminUpgradesPage";
import AdminDashboardPage from "./pages/AdminDashboardPage";
import adamaLogo from "./assets/logos/adama-logo.jpeg";
import jdLogo from "./assets/logos/jd-logo.png";

function App() {
    return (
        <BrowserRouter>
            <nav className="navbar">
                <div className="navbar-inner">
                    <Link to="/" className="navbar-brand" aria-label="Adama">
                        <img src={adamaLogo} alt="Adama" />
                    </Link>
                    <div className="navbar-links">
                        <Link to="/">Home</Link>
                        <Link to="/builder">Package Builder</Link>
                        <Link to="/admin/quotes">Quotes</Link>
                        <Link to="/admin/upgrades">Upgrades</Link>
                        <Link to="/admin/dashboard">Dashboard</Link>
                    </div>
                </div>
            </nav>

            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/builder" element={<PackageBuilderPage />} />
                <Route path="/quote/:id" element={<QuoteSummaryPage />} />
                <Route path="/admin/quotes" element={<AdminQuotesPage />} />
                <Route path="/admin/upgrades" element={<AdminUpgradesPage />} />
                <Route path="/admin/dashboard" element={<AdminDashboardPage />} />
            </Routes>

            <footer className="footer">
                <div className="footer-inner">
                    <div className="footer-copy">
                        © {new Date().getFullYear()} Adama. All rights reserved.
                    </div>
                    <div className="footer-credit" title="Crafted by JD">
                        <img src={jdLogo} alt="JD" />
                        <span>Crafted by JD</span>
                    </div>
                </div>
            </footer>
        </BrowserRouter>
    );
}

export default App;
