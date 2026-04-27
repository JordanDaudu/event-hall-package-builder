import { BrowserRouter, Routes, Route, Link } from "react-router-dom";
import HomePage from "./pages/HomePage";
import PackageBuilderPage from "./pages/PackageBuilderPage";
import QuoteSummaryPage from "./pages/QuoteSummaryPage";
import AdminQuotesPage from "./pages/AdminQuotesPage";
import AdminUpgradesPage from "./pages/AdminUpgradesPage";

function App() {
    return (
        <BrowserRouter>
            <nav className="navbar">
                <div className="navbar-inner">
                    <Link to="/">Home</Link>
                    <Link to="/builder">Package Builder</Link>
                    <Link to="/admin/quotes">Admin Quotes</Link>
                    <Link to="/admin/upgrades">Admin Upgrades</Link>
                </div>
            </nav>

            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/builder" element={<PackageBuilderPage />} />
                <Route path="/quote/:id" element={<QuoteSummaryPage />} />
                <Route path="/admin/quotes" element={<AdminQuotesPage />} />
                <Route path="/admin/upgrades" element={<AdminUpgradesPage />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;