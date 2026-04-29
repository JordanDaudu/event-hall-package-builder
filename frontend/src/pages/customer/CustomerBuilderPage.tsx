import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";
import { useBuilderUi } from "../../contexts/BuilderUiContext";
import { useToast } from "../../contexts/ToastContext";
import { listActiveVenues } from "../../api/venueApi";
import { listActivePackageOptions } from "../../api/packageOptionApi";
import { listMyPriceOverrides } from "../../api/customerPriceOverrideApi";
import { submitPackageRequest } from "../../api/packageRequestApi";
import type {
    PackageOptionCategory,
    PackageOptionResponse,
    VenueResponse,
} from "../../types/api";

// ─── Step / category mapping ─────────────────────────────────────────────────

const STEP_LABELS = [
    "פרטי אירוע",
    "חופה",
    "שדרה",
    "שולחנות",
    "מפיות ומפות",
    "כיסא כלה",
    "סיכום ושליחה",
];

const STEP_CATEGORY: Record<number, PackageOptionCategory | null> = {
    0: null,
    1: "DECORATION",
    2: "MUSIC",
    3: "CATERING",
    4: "PHOTOGRAPHY",
    5: "EXTRAS",
    6: null,
};

const CATEGORY_LABEL: Record<PackageOptionCategory, string> = {
    DECORATION: "עיצוב",
    MUSIC: "מוזיקה",
    CATERING: "קייטרינג",
    PHOTOGRAPHY: "צילום",
    EXTRAS: "תוספות",
};

// ─── Helpers ──────────────────────────────────────────────────────────────────

function formatILS(n: number) {
    return n.toLocaleString("he-IL", { style: "currency", currency: "ILS", maximumFractionDigits: 0 });
}

function todayIso() {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    return d.toISOString().substring(0, 10);
}

// ─── State types ─────────────────────────────────────────────────────────────

interface EventDetails {
    venueId: number | null;
    eventDate: string;
    eventContactName: string;
    eventCustomerIdentityNumber: string;
    eventContactPhoneNumber: string;
}

type SelectedByCategory = Record<PackageOptionCategory, number | null>;

const EMPTY_SELECTED: SelectedByCategory = {
    DECORATION: null,
    MUSIC: null,
    CATERING: null,
    PHOTOGRAPHY: null,
    EXTRAS: null,
};

// ─── Main component ───────────────────────────────────────────────────────────

export default function CustomerBuilderPage() {
    const { user } = useAuth();
    const { showToast } = useToast();
    const { setCurrentStep, setRunningTotal, setBuilderActive } = useBuilderUi();
    const navigate = useNavigate();

    // ── Data state ────────────────────────────────────────────────────────────
    const [loading, setLoading] = useState(true);
    const [venues, setVenues] = useState<VenueResponse[]>([]);
    const [options, setOptions] = useState<PackageOptionResponse[]>([]);
    const [overrideMap, setOverrideMap] = useState<Record<number, number>>({});

    // ── Builder state ─────────────────────────────────────────────────────────
    const [step, setStep] = useState(0);
    const [eventDetails, setEventDetails] = useState<EventDetails>({
        venueId: null,
        eventDate: "",
        eventContactName: user?.fullName ?? "",
        eventCustomerIdentityNumber: user?.customerIdentityNumber ?? "",
        eventContactPhoneNumber: user?.phoneNumber ?? "",
    });
    const [knightTableCount, setKnightTableCount] = useState(0);
    const [selectedByCategory, setSelectedByCategory] = useState<SelectedByCategory>({ ...EMPTY_SELECTED });

    // ── Submission state ──────────────────────────────────────────────────────
    const [stepError, setStepError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);
    const [submitted, setSubmitted] = useState(false);
    const [submitError, setSubmitError] = useState<string | null>(null);

    // ── Load data on mount ────────────────────────────────────────────────────
    useEffect(() => {
        async function load() {
            try {
                const [v, opts, overrides] = await Promise.all([
                    listActiveVenues(),
                    listActivePackageOptions(),
                    listMyPriceOverrides(),
                ]);
                setVenues(v);
                setOptions(opts);
                const map: Record<number, number> = {};
                for (const o of overrides) map[o.optionId] = o.customPrice;
                setOverrideMap(map);
            } catch {
                showToast("שגיאה בטעינת נתוני הבונה", "error");
            } finally {
                setLoading(false);
                setBuilderActive(true);
            }
        }
        load();
        return () => setBuilderActive(false);
    }, []);

    // ── Pricing helpers ───────────────────────────────────────────────────────
    function effectivePrice(opt: PackageOptionResponse): number {
        return overrideMap[opt.id] !== undefined ? overrideMap[opt.id] : opt.globalPrice;
    }

    const selectedOptionObjects = useMemo(() => {
        return Object.values(selectedByCategory)
            .filter((id): id is number => id !== null)
            .map((id) => options.find((o) => o.id === id))
            .filter((o): o is PackageOptionResponse => o !== undefined);
    }, [selectedByCategory, options]);

    const runningTotal = useMemo(() => {
        const base = user?.basePackagePrice ?? 0;
        const optionSum = selectedOptionObjects.reduce((s, o) => s + effectivePrice(o), 0);
        return base + optionSum;
    }, [selectedOptionObjects, overrideMap, user]);

    // Sync context
    useEffect(() => { setCurrentStep(step); }, [step]);
    useEffect(() => { setRunningTotal(runningTotal); }, [runningTotal]);

    // ── Validation ────────────────────────────────────────────────────────────
    function validateCurrentStep(): string | null {
        if (step === 0) {
            if (!eventDetails.venueId) return "נא לבחור אולם";
            if (!eventDetails.eventContactName.trim()) return "נא להזין שם ליצירת קשר";
            if (!eventDetails.eventCustomerIdentityNumber.trim()) return "נא להזין מספר זהות";
            if (!eventDetails.eventContactPhoneNumber.trim()) return "נא להזין מספר טלפון";
            if (!eventDetails.eventDate) return "נא לבחור תאריך אירוע";
            if (eventDetails.eventDate < todayIso()) return "תאריך האירוע חייב להיות בעתיד";
            return null;
        }
        const cat = STEP_CATEGORY[step];
        if (cat) {
            const catOptions = options.filter((o) => o.category === cat);
            if (catOptions.length > 0 && selectedByCategory[cat] === null) {
                return `נא לבחור אפשרות עבור ${STEP_LABELS[step]}`;
            }
        }
        return null;
    }

    function goNext() {
        const err = validateCurrentStep();
        if (err) { setStepError(err); return; }
        setStepError(null);
        setStep((s) => Math.min(s + 1, 6));
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    function goBack() {
        setStepError(null);
        setStep((s) => Math.max(s - 1, 0));
        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    function selectOption(cat: PackageOptionCategory, optionId: number) {
        setSelectedByCategory((prev) => ({
            ...prev,
            [cat]: prev[cat] === optionId ? null : optionId,
        }));
    }

    // ── Submission ────────────────────────────────────────────────────────────
    async function handleSubmit() {
        if (!eventDetails.venueId) return;
        setSubmitting(true);
        setSubmitError(null);
        try {
            await submitPackageRequest({
                venueId: eventDetails.venueId,
                eventContactName: eventDetails.eventContactName,
                eventCustomerIdentityNumber: eventDetails.eventCustomerIdentityNumber,
                eventContactPhoneNumber: eventDetails.eventContactPhoneNumber,
                eventDate: eventDetails.eventDate,
                knightTableCount: knightTableCount > 0 ? knightTableCount : null,
                optionIds: Object.values(selectedByCategory).filter((id): id is number => id !== null),
            });
            setSubmitted(true);
        } catch (err: unknown) {
            const msg =
                (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
                "שגיאה בשליחת הבקשה. אנא נסה שוב.";
            if ((err as { response?: { status?: number } })?.response?.status === 403) {
                setSubmitError("חשבונך אינו פעיל. פנה אל מנהל המערכת.");
            } else {
                setSubmitError(msg);
            }
        } finally {
            setSubmitting(false);
        }
    }

    // ─── Loading state ─────────────────────────────────────────────────────────
    if (loading) {
        return (
            <main className="page builder-page">
                <p className="muted">טוען את בונה החבילות...</p>
            </main>
        );
    }

    // ─── Success screen ────────────────────────────────────────────────────────
    if (submitted) {
        return (
            <main className="page builder-page">
                <div className="builder-success-card">
                    <div className="builder-success-icon">✓</div>
                    <h2>הבקשה נשלחה בהצלחה!</h2>
                    <p>בקשת החבילה שלך נשלחה לאישור. נציגנו יצרו עמך קשר בהקדם.</p>
                    <div style={{ display: "flex", gap: "12px", justifyContent: "center", flexWrap: "wrap" }}>
                        <button
                            className="button"
                            onClick={() => {
                                setStep(0);
                                setSelectedByCategory({ ...EMPTY_SELECTED });
                                setKnightTableCount(0);
                                setEventDetails({
                                    venueId: null,
                                    eventDate: "",
                                    eventContactName: user?.fullName ?? "",
                                    eventCustomerIdentityNumber: user?.customerIdentityNumber ?? "",
                                    eventContactPhoneNumber: user?.phoneNumber ?? "",
                                });
                                setSubmitted(false);
                            }}
                        >
                            בנה חבילה חדשה
                        </button>
                        <button className="button-secondary" onClick={() => navigate("/customer/my-requests")}>
                            הבקשות שלי
                        </button>
                    </div>
                </div>
            </main>
        );
    }

    // ─── Step rendering ────────────────────────────────────────────────────────

    function renderStepContent() {
        switch (step) {
            case 0:
                return <Step0 venues={venues} eventDetails={eventDetails} setEventDetails={setEventDetails} />;
            case 1:
            case 2:
            case 4:
            case 5: {
                const cat = STEP_CATEGORY[step]!;
                return (
                    <StepOptionGrid
                        stepLabel={STEP_LABELS[step]}
                        category={cat}
                        options={options.filter((o) => o.category === cat)}
                        selectedId={selectedByCategory[cat]}
                        onSelect={(id) => selectOption(cat, id)}
                        effectivePrice={effectivePrice}
                    />
                );
            }
            case 3:
                return (
                    <Step3Tables
                        options={options.filter((o) => o.category === "CATERING")}
                        selectedId={selectedByCategory["CATERING"]}
                        onSelect={(id) => selectOption("CATERING", id)}
                        knightTableCount={knightTableCount}
                        setKnightTableCount={setKnightTableCount}
                        effectivePrice={effectivePrice}
                    />
                );
            case 6:
                return (
                    <Step6Summary
                        eventDetails={eventDetails}
                        venues={venues}
                        selectedOptions={selectedOptionObjects}
                        knightTableCount={knightTableCount}
                        runningTotal={runningTotal}
                        effectivePrice={effectivePrice}
                        basePackagePrice={user?.basePackagePrice ?? 0}
                        submitError={submitError}
                        submitting={submitting}
                        onSubmit={handleSubmit}
                    />
                );
            default:
                return null;
        }
    }

    return (
        <main className="page builder-page">
            {renderStepContent()}

            {stepError && (
                <div className="builder-step-error">{stepError}</div>
            )}

            {step < 6 && (
                <div className="step-nav">
                    {step > 0 && (
                        <button className="button-secondary" onClick={goBack}>
                            ← הקודם
                        </button>
                    )}
                    <button className="button" onClick={goNext} style={{ marginInlineStart: "auto" }}>
                        {step === 5 ? "לסיכום ושליחה" : "הבא →"}
                    </button>
                </div>
            )}
        </main>
    );
}

// ─── Step 0 — Event Details ───────────────────────────────────────────────────

interface Step0Props {
    venues: VenueResponse[];
    eventDetails: EventDetails;
    setEventDetails: (d: EventDetails) => void;
}

function Step0({ venues, eventDetails, setEventDetails }: Step0Props) {
    function update<K extends keyof EventDetails>(key: K, value: EventDetails[K]) {
        setEventDetails({ ...eventDetails, [key]: value });
    }

    return (
        <div className="builder-step">
            <div className="builder-step-header">
                <span className="eyebrow">שלב 1 / 7</span>
                <h2>פרטי האירוע</h2>
                <p>מלאו את פרטי הקשר ובחרו אולם לאירוע.</p>
            </div>

            <div className="card">
                <h3 style={{ marginBottom: "20px" }}>בחירת אולם</h3>
                {venues.length === 0 ? (
                    <p className="muted">אין אולמות זמינים כרגע.</p>
                ) : (
                    <div className="venue-card-grid">
                        {venues.map((v) => (
                            <button
                                key={v.id}
                                type="button"
                                className={`venue-card${eventDetails.venueId === v.id ? " selected" : ""}`}
                                onClick={() => update("venueId", v.id)}
                            >
                                {v.imageUrl ? (
                                    <img src={v.imageUrl} alt={v.nameHe} className="venue-card-img" />
                                ) : (
                                    <div className="venue-card-placeholder">🏛</div>
                                )}
                                <div className="venue-card-body">
                                    <strong>{v.nameHe}</strong>
                                    {v.descriptionHe && (
                                        <span className="venue-card-desc">{v.descriptionHe}</span>
                                    )}
                                </div>
                                {eventDetails.venueId === v.id && (
                                    <div className="venue-card-check">✓</div>
                                )}
                            </button>
                        ))}
                    </div>
                )}
            </div>

            <div className="card">
                <h3 style={{ marginBottom: "20px" }}>פרטי יצירת קשר</h3>
                <div className="form-grid-2">
                    <div className="form-field">
                        <label>שם ליצירת קשר</label>
                        <input
                            className="input"
                            type="text"
                            placeholder="שם מלא"
                            value={eventDetails.eventContactName}
                            onChange={(e) => update("eventContactName", e.target.value)}
                        />
                    </div>
                    <div className="form-field">
                        <label>מספר טלפון</label>
                        <input
                            className="input"
                            type="tel"
                            placeholder="05X-XXXXXXX"
                            value={eventDetails.eventContactPhoneNumber}
                            onChange={(e) => update("eventContactPhoneNumber", e.target.value)}
                        />
                    </div>
                    <div className="form-field">
                        <label>מספר תעודת זהות</label>
                        <input
                            className="input"
                            type="text"
                            placeholder="9 ספרות"
                            value={eventDetails.eventCustomerIdentityNumber}
                            onChange={(e) => update("eventCustomerIdentityNumber", e.target.value)}
                        />
                    </div>
                    <div className="form-field">
                        <label>תאריך האירוע</label>
                        <input
                            className="input"
                            type="date"
                            min={new Date().toISOString().substring(0, 10)}
                            value={eventDetails.eventDate}
                            onChange={(e) => update("eventDate", e.target.value)}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
}

// ─── Reusable Option Grid Step ────────────────────────────────────────────────

interface StepOptionGridProps {
    stepLabel: string;
    category: PackageOptionCategory;
    options: PackageOptionResponse[];
    selectedId: number | null;
    onSelect: (id: number) => void;
    effectivePrice: (opt: PackageOptionResponse) => number;
}

function StepOptionGrid({ stepLabel, category, options, selectedId, onSelect, effectivePrice }: StepOptionGridProps) {
    const stepIndex = Object.entries(STEP_CATEGORY).find(([, c]) => c === category)?.[0];
    const humanStep = stepIndex ? `שלב ${Number(stepIndex) + 1} / 7` : "";

    if (options.length === 0) {
        return (
            <div className="builder-step">
                <div className="builder-step-header">
                    <span className="eyebrow">{humanStep}</span>
                    <h2>{stepLabel}</h2>
                </div>
                <div className="card" style={{ textAlign: "center", padding: "40px" }}>
                    <p className="muted">אין אפשרויות זמינות לשלב זה.</p>
                </div>
            </div>
        );
    }

    return (
        <div className="builder-step">
            <div className="builder-step-header">
                <span className="eyebrow">{humanStep} · {CATEGORY_LABEL[category]}</span>
                <h2>{stepLabel}</h2>
                <p>בחרו אפשרות אחת.</p>
            </div>
            <div className="option-card-grid">
                {options.map((opt) => (
                    <button
                        key={opt.id}
                        type="button"
                        className={`option-card${selectedId === opt.id ? " selected" : ""}`}
                        onClick={() => onSelect(opt.id)}
                    >
                        <div className="option-card-inner">
                            <div className="option-card-name">{opt.nameHe}</div>
                            {opt.nameEn && (
                                <div className="option-card-name-en">{opt.nameEn}</div>
                            )}
                            <div className="option-card-price">{formatILS(effectivePrice(opt))}</div>
                        </div>
                        {selectedId === opt.id && (
                            <div className="option-card-check">✓</div>
                        )}
                    </button>
                ))}
            </div>
        </div>
    );
}

// ─── Step 3 — Tables (Catering + Knight Table Count) ──────────────────────────

interface Step3TablesProps {
    options: PackageOptionResponse[];
    selectedId: number | null;
    onSelect: (id: number) => void;
    knightTableCount: number;
    setKnightTableCount: (n: number) => void;
    effectivePrice: (opt: PackageOptionResponse) => number;
}

function Step3Tables({ options, selectedId, onSelect, knightTableCount, setKnightTableCount, effectivePrice }: Step3TablesProps) {
    return (
        <div className="builder-step">
            <div className="builder-step-header">
                <span className="eyebrow">שלב 4 / 7 · קייטרינג ושולחנות</span>
                <h2>שולחנות</h2>
                <p>בחרו חבילת קייטרינג ומספר שולחנות אבירים.</p>
            </div>

            <div className="card">
                <h3 style={{ marginBottom: "16px" }}>שולחנות אבירים</h3>
                <p className="muted" style={{ marginBottom: "16px", fontSize: "0.9rem" }}>
                    בחרו את מספר שולחנות האבירים הרצויים (0 = ללא שולחנות אבירים).
                </p>
                <div className="knight-count-row">
                    {[0, 1, 2, 3, 4].map((n) => (
                        <button
                            key={n}
                            type="button"
                            className={`knight-count-btn${knightTableCount === n ? " selected" : ""}`}
                            onClick={() => setKnightTableCount(n)}
                        >
                            {n}
                        </button>
                    ))}
                </div>
                {knightTableCount > 0 && (
                    <p style={{ marginTop: "12px", fontSize: "0.88rem", color: "var(--color-accent)" }}>
                        נבחרו {knightTableCount} שולחנות אבירים
                    </p>
                )}
            </div>

            {options.length > 0 && (
                <>
                    <div className="builder-step-header" style={{ marginTop: "8px" }}>
                        <h3>חבילת קייטרינג</h3>
                        <p>בחרו אפשרות אחת.</p>
                    </div>
                    <div className="option-card-grid">
                        {options.map((opt) => (
                            <button
                                key={opt.id}
                                type="button"
                                className={`option-card${selectedId === opt.id ? " selected" : ""}`}
                                onClick={() => onSelect(opt.id)}
                            >
                                <div className="option-card-inner">
                                    <div className="option-card-name">{opt.nameHe}</div>
                                    {opt.nameEn && (
                                        <div className="option-card-name-en">{opt.nameEn}</div>
                                    )}
                                    <div className="option-card-price">{formatILS(effectivePrice(opt))}</div>
                                </div>
                                {selectedId === opt.id && (
                                    <div className="option-card-check">✓</div>
                                )}
                            </button>
                        ))}
                    </div>
                </>
            )}
        </div>
    );
}

// ─── Step 6 — Summary & Submit ────────────────────────────────────────────────

interface Step6SummaryProps {
    eventDetails: EventDetails;
    venues: VenueResponse[];
    selectedOptions: PackageOptionResponse[];
    knightTableCount: number;
    runningTotal: number;
    effectivePrice: (opt: PackageOptionResponse) => number;
    basePackagePrice: number;
    submitError: string | null;
    submitting: boolean;
    onSubmit: () => void;
}

function Step6Summary({
    eventDetails,
    venues,
    selectedOptions,
    knightTableCount,
    runningTotal,
    effectivePrice,
    basePackagePrice,
    submitError,
    submitting,
    onSubmit,
}: Step6SummaryProps) {
    const venue = venues.find((v) => v.id === eventDetails.venueId);

    return (
        <div className="builder-step">
            <div className="builder-step-header">
                <span className="eyebrow">שלב 7 / 7</span>
                <h2>סיכום ושליחה</h2>
                <p>בדקו את הפרטים ושלחו את הבקשה לאישור.</p>
            </div>

            <div className="card summary-card">
                <h3 className="summary-section-title">פרטי האירוע</h3>
                <div className="summary-grid">
                    <div className="summary-row">
                        <span className="summary-label">אולם</span>
                        <span className="summary-value">{venue?.nameHe ?? "—"}</span>
                    </div>
                    <div className="summary-row">
                        <span className="summary-label">תאריך</span>
                        <span className="summary-value">
                            {eventDetails.eventDate
                                ? new Date(eventDetails.eventDate).toLocaleDateString("he-IL", { year: "numeric", month: "long", day: "numeric" })
                                : "—"}
                        </span>
                    </div>
                    <div className="summary-row">
                        <span className="summary-label">שם ליצירת קשר</span>
                        <span className="summary-value">{eventDetails.eventContactName}</span>
                    </div>
                    <div className="summary-row">
                        <span className="summary-label">מספר זהות</span>
                        <span className="summary-value">{eventDetails.eventCustomerIdentityNumber}</span>
                    </div>
                    <div className="summary-row">
                        <span className="summary-label">טלפון</span>
                        <span className="summary-value">{eventDetails.eventContactPhoneNumber}</span>
                    </div>
                    {knightTableCount > 0 && (
                        <div className="summary-row">
                            <span className="summary-label">שולחנות אבירים</span>
                            <span className="summary-value">{knightTableCount}</span>
                        </div>
                    )}
                </div>
            </div>

            <div className="card summary-card">
                <h3 className="summary-section-title">אפשרויות שנבחרו</h3>
                {selectedOptions.length === 0 ? (
                    <p className="muted">לא נבחרו אפשרויות.</p>
                ) : (
                    <div className="summary-options-list">
                        {selectedOptions.map((opt) => (
                            <div key={opt.id} className="summary-option-row">
                                <div>
                                    <div className="summary-option-name">{opt.nameHe}</div>
                                    <div className="summary-option-category">
                                        {CATEGORY_LABEL[opt.category]}
                                    </div>
                                </div>
                                <div className="summary-option-price">{formatILS(effectivePrice(opt))}</div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            <div className="card summary-total-card">
                <div className="summary-price-row">
                    <span>חבילה בסיסית</span>
                    <span>{formatILS(basePackagePrice)}</span>
                </div>
                {selectedOptions.map((opt) => (
                    <div key={opt.id} className="summary-price-row summary-price-row-sub">
                        <span>{opt.nameHe}</span>
                        <span>{formatILS(effectivePrice(opt))}</span>
                    </div>
                ))}
                <hr />
                <div className="summary-price-row summary-total-row">
                    <span>סה"כ משוער</span>
                    <span>{formatILS(runningTotal)}</span>
                </div>

                {submitError && (
                    <div className="builder-submit-error">{submitError}</div>
                )}

                <button
                    className="button builder-submit-btn"
                    onClick={onSubmit}
                    disabled={submitting}
                >
                    {submitting ? "שולח..." : "שלח לאישור"}
                </button>

                <p className="muted" style={{ marginTop: "12px", fontSize: "0.84rem", textAlign: "center" }}>
                    לאחר השליחה, נציגנו יצרו עמך קשר לאישור סופי של הזמנה.
                </p>
            </div>
        </div>
    );
}
