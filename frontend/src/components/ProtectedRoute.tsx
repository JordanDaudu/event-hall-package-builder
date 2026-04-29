import { Navigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import type { UserRole } from "../types/api";
import type { ReactNode } from "react";

interface ProtectedRouteProps {
    requiredRole: UserRole;
    children: ReactNode;
}

/**
 * Wraps a route so that:
 * - Unauthenticated users are redirected to /login.
 * - Users whose role does not match requiredRole are redirected to their
 *   correct area (/admin or /customer).
 */
export default function ProtectedRoute({ requiredRole, children }: ProtectedRouteProps) {
    const { user, loading } = useAuth();

    if (loading) {
        return (
            <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "100vh" }}>
                <p style={{ color: "var(--color-muted)", fontSize: "1rem" }}>טוען...</p>
            </div>
        );
    }

    if (!user) {
        return <Navigate to="/login" replace />;
    }

    if (user.role !== requiredRole) {
        return <Navigate to={user.role === "ADMIN" ? "/admin" : "/customer"} replace />;
    }

    return <>{children}</>;
}
