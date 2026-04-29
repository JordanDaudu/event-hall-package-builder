import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider, useAuth } from "./contexts/AuthContext";
import { ToastProvider } from "./contexts/ToastContext";
import ProtectedRoute from "./components/ProtectedRoute";
import AdminLayout from "./layouts/AdminLayout";
import CustomerLayout from "./layouts/CustomerLayout";
import LoginPage from "./pages/LoginPage";
import AdminDashboard from "./pages/admin/AdminDashboard";
import AdminCustomersPage from "./pages/admin/AdminCustomersPage";
import AdminCustomerDetailPage from "./pages/admin/AdminCustomerDetailPage";
import AdminVenuesPage from "./pages/admin/AdminVenuesPage";
import AdminPackageOptionsPage from "./pages/admin/AdminPackageOptionsPage";
import AdminChuppahCompatibilityPage from "./pages/admin/AdminChuppahCompatibilityPage";
import AdminRequestsPage from "./pages/admin/AdminRequestsPage";
import AdminRequestDetailPage from "./pages/admin/AdminRequestDetailPage";
import CustomerBuilderPage from "./pages/customer/CustomerBuilderPage";
import CustomerMyRequestsPage from "./pages/customer/CustomerMyRequestsPage";

function RootRedirect() {
    const { user, loading } = useAuth();
    if (loading) return null;
    if (!user) return <Navigate to="/login" replace />;
    return <Navigate to={user.role === "ADMIN" ? "/admin" : "/customer"} replace />;
}

function App() {
    return (
        <BrowserRouter>
            <AuthProvider>
                <ToastProvider>
                    <Routes>
                        {/* Public */}
                        <Route path="/login" element={<LoginPage />} />

                        {/* Root — role-aware redirect */}
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
                            <Route path="customers/:id" element={<AdminCustomerDetailPage />} />
                            <Route path="venues" element={<AdminVenuesPage />} />
                            <Route path="package-options" element={<AdminPackageOptionsPage />} />
                            <Route path="chuppah-compatibility" element={<AdminChuppahCompatibilityPage />} />
                            <Route path="requests" element={<AdminRequestsPage />} />
                            <Route path="requests/:id" element={<AdminRequestDetailPage />} />
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
                            <Route path="my-requests" element={<CustomerMyRequestsPage />} />
                        </Route>

                        {/* Fallback */}
                        <Route path="*" element={<Navigate to="/login" replace />} />
                    </Routes>
                </ToastProvider>
            </AuthProvider>
        </BrowserRouter>
    );
}

export default App;
