import { useAuth } from "../../contexts/AuthContext";

export default function AdminDashboard() {
    const { user } = useAuth();
    return (
        <main className="page">
            <div className="page-header">
                <div>
                    <span className="eyebrow">לוח בקרה</span>
                    <h1>שלום, {user?.fullName}</h1>
                    <p>ברוך הבא לפאנל הניהול של אדמה — אולמות אירועים.</p>
                </div>
            </div>
            <div className="stat-row">
                <div className="stat-card">
                    <span>לקוחות</span>
                    <strong>—</strong>
                </div>
                <div className="stat-card">
                    <span>בקשות פתוחות</span>
                    <strong>—</strong>
                </div>
                <div className="stat-card">
                    <span>אולמות פעילים</span>
                    <strong>—</strong>
                </div>
            </div>
            <div className="card">
                <p className="muted">בחר אחת מהאפשרויות בסרגל הניווט לעיל כדי להתחיל.</p>
            </div>
        </main>
    );
}
