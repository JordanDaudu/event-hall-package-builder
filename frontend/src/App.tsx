import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./contexts/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import AdminLayout from "./layouts/AdminLayout";
import CustomerLayout from "./layouts/CustomerLayout";
import LoginPage from "./pages/LoginPage";
import AdminDashboard from "./pages/admin/AdminDashboard";
import AdminCustomersPage from "./pages/admin/AdminCustomersPage";
import AdminVenuesPage from "./pages/admin/AdminVenuesPage";
import AdminPackageOptionsPage from "./pages/admin/AdminPackageOptionsPage";
import AdminRequestsPage from "./pages/admin/AdminRequestsPage";
import CustomerBuilderPage from "./pages/customer/CustomerBuilderPage";

function RootRedirect() {
    return <Navigate to="/login" replace />;
}

function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <Routes>
                    {/* Public */}
                    <Route path="/login" element={<LoginPage />} />

                    {/* Root — redirect to login (AuthContext will redirect further if logged in) */}
                    <Route path="/" element={<RootRedirect />} />

                    {/* Admin area */}
                    <Route
                        path="/admin"
                        element={
                            <ProtectedRoute requiredRole="ADMIN">
                                <AdminLayout />
                            </ProtectedRoute>
                        }
                    >
                        <Route index element={<AdminDashboard />} />
                        <Route path="customers" element={<AdminCustomersPage />} />
                        <Route path="venues" element={<AdminVenuesPage />} />
                        <Route path="package-options" element={<AdminPackageOptionsPage />} />
                        <Route path="requests" element={<AdminRequestsPage />} />
                    </Route>

                    {/* Customer area */}
                    <Route
                        path="/customer"
                        element={
                            <ProtectedRoute requiredRole="CUSTOMER">
                                <CustomerLayout />
                            </ProtectedRoute>
                        }
                    >
                        <Route index element={<CustomerBuilderPage />} />
                    </Route>

                    {/* Fallback */}
                    <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;
