import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router-dom";
import { usePageTitle } from "../../hooks/usePageTitle";
import { useToast } from "../../contexts/ToastContext";
import {
    listChuppahs,
    getChuppahCompatibility,
    setChuppahCompatibility,
} from "../../api/chuppahCompatibilityApi";
import type { ChuppahCompatibilityResponse, PackageOptionResponse } from "../../types/api";
import { formatILS } from "../../utils/currency";

export default function AdminChuppahCompatibilityPage() {
    usePageTitle("התאמות תוספות לחופה");
    const { showToast } = useToast();
    const { id: idParam } = useParams<{ id?: string }>();
    const deepLinkId = idParam ? Number(idParam) : null;
    const didAutoSelect = useRef(false);

    const [chuppahs, setChuppahs] = useState<PackageOptionResponse[]>([]);
    const [selectedChuppahId, setSelectedChuppahId] = useState<number | null>(null);
    const [compatibility, setCompatibility] = useState<ChuppahCompatibilityResponse | null>(null);
    const [checkedIds, setCheckedIds] = useState<number[]>([]);
    const [loadingChuppahs, setLoadingChuppahs] = useState(true);
    const [loadingCompat, setLoadingCompat] = useState(false);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        async function load() {
            try {
                const all = await listChuppahs();
                setChuppahs(all);
            } catch {
                showToast("שגיאה בטעינת רשימת החופות", "error");
            } finally {
                setLoadingChuppahs(false);
            }
        }
        load();
    }, []);

    useEffect(() => {
        if (!loadingChuppahs && deepLinkId != null && !didAutoSelect.current) {
            const found = chuppahs.find((c) => c.id === deepLinkId);
            if (found) {
                didAutoSelect.current = true;
                selectChuppah(found.id);
            }
        }
    }, [loadingChuppahs, chuppahs, deepLinkId]);

    async function selectChuppah(id: number) {
        setSelectedChuppahId(id);
        setCompatibility(null);
        setLoadingCompat(true);
        try {
            const data = await getChuppahCompatibility(id);
            setCompatibility(data);
            setCheckedIds(data.allowedUpgradeIds);
        } catch {
            showToast("שגיאה בטעינת נתוני התאמה", "error");
        } finally {
            setLoadingCompat(false);
        }
    }

    function toggleUpgrade(id: number) {
        setCheckedIds((prev) =>
            prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
        );
    }

    async function handleSave() {
        if (selectedChuppahId == null) return;
        setSaving(true);
        try {
            const updated = await setChuppahCompatibility(selectedChuppahId, checkedIds);
            setCompatibility(updated);
            setCheckedIds(updated.allowedUpgradeIds);
            showToast("ההתאמות נשמרו בהצלחה");
        } catch {
            showToast("שגיאה בשמירת ההתאמות", "error");
        } finally {
            setSaving(false);
        }
    }

    return (
        <main className="page">
            <div className="page-header">
                <div>
                    <span className="eyebrow">ניהול</span>
                    <h1>התאמות תוספות לחופה</h1>
                    <p className="muted" style={{ marginTop: "4px" }}>
                        קבעו אילו תוספות מותרות לכל עיצוב חופה.
                    </p>
                </div>
            </div>

            <div style={{ display: "grid", gridTemplateColumns: "280px 1fr", gap: "24px", alignItems: "start" }}>
                {/* Chuppah list */}
                <div className="card" style={{ padding: 0 }}>
                    <div style={{ padding: "16px 20px", borderBottom: "1px solid var(--border)" }}>
                        <h3 style={{ margin: 0 }}>בחירת חופה</h3>
                    </div>
                    {loadingChuppahs ? (
                        <div style={{ padding: "24px", textAlign: "center" }} className="muted">טוען...</div>
                    ) : chuppahs.length === 0 ? (
                        <div style={{ padding: "24px" }} className="muted">אין חופות מוגדרות.</div>
                    ) : (
                        <ul style={{ listStyle: "none", margin: 0, padding: 0 }}>
                            {chuppahs.map((c) => (
                                <li key={c.id}>
                                    <button
                                        type="button"
                                        style={{
                                            width: "100%",
                                            padding: "14px 20px",
                                            textAlign: "right",
                                            background: selectedChuppahId === c.id ? "var(--surface-2)" : "transparent",
                                            border: "none",
                                            borderBottom: "1px solid var(--border)",
                                            cursor: "pointer",
                                            fontFamily: "inherit",
                                            fontSize: "0.9rem",
                                        }}
                                        onClick={() => selectChuppah(c.id)}
                                    >
                                        <div style={{ fontWeight: 600 }}>{c.nameHe}</div>
                                        <div className="muted" style={{ fontSize: "0.8rem" }}>
                                            {formatILS(c.globalPrice)}
                                            {!c.active && " · מושבת"}
                                        </div>
                                    </button>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                {/* Compatibility panel */}
                <div className="card">
                    {selectedChuppahId == null ? (
                        <p className="muted">בחרו חופה כדי לנהל תוספות זמינות.</p>
                    ) : loadingCompat ? (
                        <p className="muted">טוען...</p>
                    ) : !compatibility ? (
                        <p className="muted">שגיאה בטעינת הנתונים.</p>
                    ) : (
                        <>
                            <div style={{ marginBottom: "20px" }}>
                                <h3 style={{ margin: "0 0 4px" }}>{compatibility.chuppah.nameHe}</h3>
                                <span className="muted" style={{ fontSize: "0.87rem" }}>
                                    תוספות זמינות לחופה זו
                                </span>
                            </div>

                            {compatibility.availableUpgrades.length === 0 ? (
                                <p className="muted">אין תוספות מוגדרות במערכת. הוסיפו תוספות בקטגוריה "תוספת לחופה".</p>
                            ) : (
                                <>
                                    <div style={{ display: "flex", flexDirection: "column", gap: "10px", marginBottom: "24px" }}>
                                        {compatibility.availableUpgrades.map((u) => {
                                            const allowed = checkedIds.includes(u.id);
                                            return (
                                                <label
                                                    key={u.id}
                                                    style={{
                                                        display: "flex",
                                                        alignItems: "center",
                                                        gap: "12px",
                                                        padding: "12px 16px",
                                                        border: `1px solid ${allowed ? "var(--primary)" : "var(--border)"}`,
                                                        borderRadius: "8px",
                                                        background: allowed ? "var(--primary-bg)" : "transparent",
                                                        cursor: "pointer",
                                                        transition: "all 0.15s",
                                                    }}
                                                >
                                                    <input
                                                        type="checkbox"
                                                        checked={allowed}
                                                        onChange={() => toggleUpgrade(u.id)}
                                                        style={{ width: "18px", height: "18px", cursor: "pointer" }}
                                                    />
                                                    <div style={{ flex: 1 }}>
                                                        <div style={{ fontWeight: 600 }}>{u.nameHe}</div>
                                                        {u.nameEn && (
                                                            <div className="muted" style={{ fontSize: "0.8rem" }} dir="ltr">{u.nameEn}</div>
                                                        )}
                                                    </div>
                                                    <div style={{ fontWeight: 500, color: "var(--text-secondary)" }}>
                                                        +{formatILS(u.globalPrice)}
                                                    </div>
                                                    <span
                                                        className={`badge ${allowed ? "badge-active" : "badge-inactive"}`}
                                                        style={{ minWidth: "80px", textAlign: "center" }}
                                                    >
                                                        {allowed ? "תוספת מותרת" : "תוספת לא מותרת"}
                                                    </span>
                                                </label>
                                            );
                                        })}
                                    </div>

                                    <div style={{ display: "flex", justifyContent: "flex-start" }}>
                                        <button
                                            className="button"
                                            onClick={handleSave}
                                            disabled={saving}
                                        >
                                            {saving ? "שומר..." : "שמירת התאמות"}
                                        </button>
                                    </div>
                                </>
                            )}
                        </>
                    )}
                </div>
            </div>
        </main>
    );
}
