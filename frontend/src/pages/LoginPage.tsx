import { useState, type FormEvent } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { usePageTitle } from "../hooks/usePageTitle";
import adamaLogo from "../assets/logos/adama-logo.jpeg";

export default function LoginPage() {
    usePageTitle("כניסה לחשבון");
    const { user, loading, login } = useAuth();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    if (loading) return null;
    if (user) {
        return <Navigate to={user.role === "ADMIN" ? "/admin" : "/customer"} replace />;
    }

    async function handleSubmit(e: FormEvent) {
        e.preventDefault();
        setError(null);
        setSubmitting(true);
        try {
            await login(email, password);
        } catch (err: unknown) {
            const axiosErr = err as { response?: { data?: { error?: string } } };
            const msg = axiosErr?.response?.data?.error;
            setError(msg ?? "שגיאה בהתחברות. אנא בדוק את הפרטים ונסה שנית.");
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <div className="login-page">
            <div className="login-card">
                <div className="login-brand">
                    <img src={adamaLogo} alt="אדמה" className="login-logo" />
                    <h1 className="login-title">אדמה — אולמות אירועים</h1>
                    <p className="login-subtitle">פורטל לקוחות</p>
                </div>

                <form onSubmit={handleSubmit} className="login-form" noValidate>
                    <div className="form-field">
                        <label htmlFor="email">כתובת דוא"ל</label>
                        <input
                            id="email"
                            type="email"
                            className="input"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            autoComplete="email"
                            required
                            disabled={submitting}
                            dir="ltr"
                        />
                    </div>

                    <div className="form-field">
                        <label htmlFor="password">סיסמה</label>
                        <input
                            id="password"
                            type="password"
                            className="input"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="••••••••"
                            autoComplete="current-password"
                            required
                            disabled={submitting}
                            dir="ltr"
                        />
                    </div>

                    {error && (
                        <div className="login-error" role="alert">
                            {error}
                        </div>
                    )}

                    <button
                        type="submit"
                        className="button login-submit"
                        disabled={submitting || !email || !password}
                    >
                        {submitting ? "מתחבר..." : "התחברות"}
                    </button>
                </form>
            </div>
        </div>
    );
}
