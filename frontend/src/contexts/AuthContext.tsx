import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useState,
    type ReactNode,
} from "react";
import { useNavigate } from "react-router-dom";
import { loginApi, getMe } from "../api/authApi";
import { setApiToken, setUnauthorizedHandler } from "../api/apiClient";
import type { AuthUser } from "../types/api";

const TOKEN_KEY = "adama_auth_token";

interface AuthContextValue {
    user: AuthUser | null;
    loading: boolean;
    login: (email: string, password: string) => Promise<void>;
    logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<AuthUser | null>(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    const logout = useCallback(() => {
        sessionStorage.removeItem(TOKEN_KEY);
        setApiToken(null);
        setUser(null);
        navigate("/login", { replace: true });
    }, [navigate]);

    // Register the unauthorized handler so any 401 from any API call
    // clears the session and returns the user to the login page.
    useEffect(() => {
        setUnauthorizedHandler(logout);
    }, [logout]);

    // On mount: restore the session from sessionStorage and validate the token.
    useEffect(() => {
        const stored = sessionStorage.getItem(TOKEN_KEY);
        if (!stored) {
            setLoading(false);
            return;
        }
        setApiToken(stored);
        getMe()
            .then((me) => {
                if (!me.active) {
                    // Account was disabled — treat as logged out.
                    sessionStorage.removeItem(TOKEN_KEY);
                    setApiToken(null);
                    return;
                }
                setUser({
                    id: me.id,
                    email: me.email,
                    fullName: me.fullName,
                    role: me.role,
                    customerIdentityNumber: me.customerIdentityNumber,
                    phoneNumber: me.phoneNumber,
                    basePackagePrice: me.basePackagePrice,
                });
            })
            .catch(() => {
                sessionStorage.removeItem(TOKEN_KEY);
                setApiToken(null);
            })
            .finally(() => setLoading(false));
    }, []);

    const login = async (email: string, password: string): Promise<void> => {
        const res = await loginApi({ email, password });
        sessionStorage.setItem(TOKEN_KEY, res.token);
        setApiToken(res.token);
        const authUser: AuthUser = {
            id: res.userId,
            email: res.email,
            fullName: res.fullName,
            role: res.role,
        };
        setUser(authUser);
        navigate(res.role === "ADMIN" ? "/admin" : "/customer", { replace: true });
    };

    return (
        <AuthContext.Provider value={{ user, loading, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth(): AuthContextValue {
    const ctx = useContext(AuthContext);
    if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
    return ctx;
}
