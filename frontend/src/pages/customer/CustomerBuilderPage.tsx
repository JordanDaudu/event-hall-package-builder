import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { usePageTitle } from "../../hooks/usePageTitle";
import { useAuth } from "../../contexts/AuthContext";
import { useBuilderUi } from "../../contexts/BuilderUiContext";
import { useToast } from "../../contexts/ToastContext";
import { listActiveVenues } from "../../api/venueApi";
import { listActivePackageOptions } from "../../api/packageOptionApi";
import { listMyPriceOverrides } from "../../api/customerPriceOverrideApi";
import { submitPackageRequest } from "../../api/packageRequestApi";
import { getChuppahCompatibilityMap } from "../../api/chuppahCompatibilityApi";
import type { ChuppahCompatibilityMap, PackageOptionResponse, VenueResponse } from "../../types/api";

// Helpers

function formatILS(n: number) {
    return n.toLocaleString("he-IL", { style: "currency", currency: "ILS", maximumFractionDigits: 0 });
}

function todayIso() {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    return d.toISOString().substring(0, 10);
}

// Event details

interface EventDetails {
    venueId: number | null;
    eventDate: string;
    eventContactName: string;
    eventCustomerIdentityNumber: string;
    eventContactPhoneNumber: string;
}

// Main builder state

interface TableSubSelections {
    frameId: number | null;
    primaryFlowerId: number | null;
    secondaryFlowerId: number | null;
    candleMode: "random" | "specific";
    candleCount: number;
    candleOptionId: number | null;
}

const EMPTY_TABLE_SUBS: TableSubSelections = {
    frameId: null,
    primaryFlowerId: null,
    secondaryFlowerId: null,
    candleMode: "random",
    candleCount: 1,
    candleOptionId: null,
};

// Preview component

function OptionPreview({
    selected,
    venueImageUrl,
}: {
    selected: PackageOptionResponse | null;
    venueImageUrl?: string | null;
}) {
    return (
        <div className="option-preview">
            {venueImageUrl && (
                <img src={venueImageUrl} alt="רקע" className="option-preview-bg" />
            )}
            <div className="option-preview-overlay" />
            <div className="option-preview-content">
                {selected ? (
                    <>
                        <div className="option-preview-check">✓</div>
                        <div className="option-preview-name">{selected.nameHe}</div>
                        {selected.nameEn && (
                            <div className="option-preview-name-en">{selected.nameEn}</div>
                        )}
                    </>
                ) : (
                    <div className="option-preview-placeholder">לחצו על אפשרות לצפייה בתצוגה מקדימה</div>
                )}
            </div>
        </div>
    );
}

// Generic option grid

function OptionGrid({
    options,
    selectedId,
    onSelect,
    effectivePrice,
    disabledIds,
}: {
    options: PackageOptionResponse[];
    selectedId: number | null;
    onSelect: (id: number) => void;
    effectivePrice: (opt: PackageOptionResponse) => number;
    disabledIds?: number[];
}) {
    if (options.length === 0) {
        return <p className="muted" style={{ padding: "24px 0" }}>אין אפשרויות זמינות.</p>;
    }
    return (
        <div className="option-card-grid">
            {options.map((opt) => {
                const isDisabled = disabledIds?.includes(opt.id);
                return (
                    <button
                        key={opt.id}
                        type="button"
                        className={`option-card${selectedId === opt.id ? " selected" : ""}${isDisabled ? " disabled" : ""}${opt.imageUrl ? " has-thumb" : ""}`}
                        onClick={() => !isDisabled && onSelect(opt.id)}
                        disabled={isDisabled}
                    >
                        {opt.imageUrl && (
                            <img src={opt.imageUrl} alt={opt.nameHe} className="option-card-thumb" />
                        )}
                        <div className="option-card-inner">
                            <div className="option-card-name">{opt.nameHe}</div>
                            {opt.nameEn && <div className="option-card-name-en">{opt.nameEn}</div>}
                            <div className="option-card-price">{formatILS(effectivePrice(opt))}</div>
                        </div>
                        {selectedId === opt.id && <div className="option-card-check">✓</div>}
                    </button>
                );
            })}
        </div>
    );
}

// Step 0 — Event details

function Step0({
    venues,
    eventDetails,
    setEventDetails,
}: {
    venues: VenueResponse[];
    eventDetails: EventDetails;
    setEventDetails: (d: EventDetails) => void;
}) {
    function update<K extends keyof EventDetails>(key: K, val: EventDetails[K]) {
        setEventDetails({ ...eventDetails, [key]: val });
    }
    return (
        <div className="builder-step">
            <div className="builder-step-header">
                <span className="eyebrow">שלב 1 / 7</span>
                <h2>פרטי האירוע</h2>
                <p>בחרו את האולם ומלאו את פרטי האירוע.</p>
            </div>

            <div className="card">
                <h3 style={{ marginBottom: "18px" }}>בחירת אולם</h3>
                {venues.length === 0 ? (
                    <p className="muted">אין אולמות זמינים.</p>
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
                                {eventDetails.venueId === v.id && <div className="venue-card-check">✓</div>}
                            </button>
                        ))}
                    </div>
                )}
            </div>

            <div className="card">
                <h3 style={{ marginBottom: "18px" }}>פרטי יצירת קשר</h3>
                <div className="form-grid-2">
                    <div className="form-field">
                        <label>שם ליצירת קשר</label>
                        <input className="input" type="text" placeholder="שם מלא"
                            value={eventDetails.eventContactName}
                            onChange={(e) => update("eventContactName", e.target.value)} />
                    </div>
                    <div className="form-field">
                        <label>מספר טלפון</label>
                        <input className="input" type="tel" placeholder="05X-XXXXXXX"
                            value={eventDetails.eventContactPhoneNumber}
                            onChange={(e) => update("eventContactPhoneNumber", e.target.value)} />
                    </div>
                    <div className="form-field">
                        <label>מספר תעודת זהות</label>
                        <input className="input" type="text" placeholder="9 ספרות"
                            value={eventDetails.eventCustomerIdentityNumber}
                            onChange={(e) => update("eventCustomerIdentityNumber", e.target.value)} />
                    </div>
                    <div className="form-field">
                        <label>תאריך האירוע</label>
                        <input className="input" type="date"
                            min={todayIso()}
                            value={eventDetails.eventDate}
                            onChange={(e) => update("eventDate", e.target.value)} />
                    </div>
                </div>
            </div>
        </div>
    );
}

// Chuppah visual preview (replaces generic OptionPreview for the chuppah step)

function ChuppahPreview({
    selectedChuppah,
    selectedUpgrades,
    venueImageUrl,
}: {
    selectedChuppah: PackageOptionResponse | null;
    selectedUpgrades: PackageOptionResponse[];
    venueImageUrl?: string | null;
}) {
    const chuppahImageUrl =
        selectedChuppah?.visualBehavior === "REPLACE_IMAGE" ? (selectedChuppah.imageUrl ?? null) : null;
    const overlayUpgrades = selectedUpgrades.filter(
        (u) => u.visualBehavior === "OVERLAY_IMAGE" && u.imageUrl,
    );

    return (
        <div className="chuppah-preview">
            {/* Background: chuppah image (REPLACE_IMAGE) or venue image as dim backdrop */}
            {chuppahImageUrl ? (
                <img src={chuppahImageUrl} alt={selectedChuppah?.nameHe ?? "חופה"} className="chuppah-preview-main" />
            ) : venueImageUrl ? (
                <img src={venueImageUrl} alt="רקע" className="option-preview-bg" />
            ) : null}

            {/* Gradient overlay for readability */}
            <div className="option-preview-overlay" />

            {/* Upgrade overlay images */}
            {overlayUpgrades.map((u) => (
                <img
                    key={u.id}
                    src={u.imageUrl!}
                    alt={u.nameHe}
                    className="chuppah-preview-upgrade"
                    style={{
                        top: u.overlayTop ?? "0%",
                        left: u.overlayLeft ?? "0%",
                        width: u.overlayWidth ?? "100%",
                        zIndex: u.overlayZIndex ?? 2,
                    }}
                />
            ))}

            {/* Label */}
            <div className="option-preview-content">
                {selectedChuppah ? (
                    <>
                        <div className="option-preview-check">✓</div>
                        <div className="option-preview-name">{selectedChuppah.nameHe}</div>
                        {selectedChuppah.nameEn && (
                            <div className="option-preview-name-en">{selectedChuppah.nameEn}</div>
                        )}
                    </>
                ) : (
                    <div className="option-preview-placeholder">לחצו על חופה לצפייה בתצוגה מקדימה</div>
                )}
            </div>
        </div>
    );
}

// Step 1 — Chuppah main selection + upgrades

function Step1Chuppah({
    chuppahOpts,
    upgradeOpts,
    selectedId,
    selectedUpgradeIds,
    compatibilityMap,
    onSelectChuppah,
    onToggleUpgrade,
    effectivePrice,
    venueImageUrl,
}: {
    chuppahOpts: PackageOptionResponse[];
    upgradeOpts: PackageOptionResponse[];
    selectedId: number | null;
    selectedUpgradeIds: number[];
    compatibilityMap: ChuppahCompatibilityMap;
    onSelectChuppah: (id: number) => void;
    onToggleUpgrade: (id: number) => void;
    effectivePrice: (opt: PackageOptionResponse) => number;
    venueImageUrl?: string | null;
}) {
    const selectedOpt = chuppahOpts.find((o) => o.id === selectedId) ?? null;
    const compatibleUpgradeIds: number[] = selectedId != null ? (compatibilityMap[selectedId] ?? []) : [];
    const visibleUpgrades = upgradeOpts.filter((u) => compatibleUpgradeIds.includes(u.id));
    const activeUpgrades = upgradeOpts.filter(
        (u) => selectedUpgradeIds.includes(u.id) && compatibleUpgradeIds.includes(u.id),
    );

    return (
        <div className="builder-step">
            <div className="builder-step-header">
                <span className="eyebrow">שלב 2 / 7 · עיצוב חופה</span>
                <h2>חופה</h2>
                <p>בחרו את עיצוב החופה לאירועכם.</p>
            </div>
            <ChuppahPreview
                selectedChuppah={selectedOpt}
                selectedUpgrades={activeUpgrades}
                venueImageUrl={venueImageUrl}
            />
            <div className="card">
                <OptionGrid
                    options={chuppahOpts}
                    selectedId={selectedId}
                    onSelect={onSelectChuppah}
                    effectivePrice={effectivePrice}
                />
            </div>

            <div className="card" style={{ marginTop: "20px" }}>
                <h3 style={{ marginBottom: "8px" }}>תוספות לחופה</h3>
                <p className="muted" style={{ marginBottom: "16px", fontSize: "0.9rem" }}>
                    בחרו תוספות זמינות עבור החופה שנבחרה.
                </p>
                {selectedId == null ? (
                    <p className="muted">בחרו חופה כדי לראות תוספות זמינות.</p>
                ) : visibleUpgrades.length === 0 ? (
                    <p className="muted">אין תוספות זמינות לחופה זו.</p>
                ) : (
                    <div className="option-card-grid">
                        {visibleUpgrades.map((u) => {
                            const isSelected = selectedUpgradeIds.includes(u.id);
                            return (
                                <button
                                    key={u.id}
                                    type="button"
                                    className={`option-card${isSelected ? " selected" : ""}${u.imageUrl ? " has-thumb" : ""}`}
                                    onClick={() => onToggleUpgrade(u.id)}
                                >
                                    {u.imageUrl && (
                                        <img src={u.imageUrl} alt={u.nameHe} className="option-card-thumb" />
                                    )}
                                    <div className="option-card-inner">
                                        <div className="option-card-name">{u.nameHe}</div>
                                        {u.nameEn && <div className="option-card-name-en">{u.nameEn}</div>}
                                        <div className="option-card-price">+{formatILS(effectivePrice(u))}</div>
                                    </div>
                                    {isSelected && <div className="option-card-check">✓</div>}
                                </button>
                            );
                        })}
                    </div>
                )}
            </div>
        </div>
    );
}

// Step 2 — Aisle (AISLE category) with filter tabs

function Step2Aisle({
    options,
    selectedId,
    onSelect,
    effectivePrice,
    venueImageUrl,
}: {
    options: PackageOptionResponse[];
    selectedId: number | null;
    onSelect: (id: number) => void;
    effectivePrice: (opt: PackageOptionResponse) => number;
    venueImageUrl?: string | null;
}) {
    const [activeTab, setActiveTab] = useState<string>("ALL");
    const selectedOpt = options.find((o) => o.id === selectedId) ?? null;

    // Derive filter tabs from nameEn prefix tokens (optional grouping within the AISLE category)
    const tabs = useMemo(() => {
        const groups = new Set(options.map((o) => (o.nameEn ? o.nameEn.split(" ")[0] : null)).filter(Boolean) as string[]);
        return ["ALL", ...Array.from(groups).slice(0, 3)];
    }, [options]);

    const visibleOptions = activeTab === "ALL"
        ? options
        : options.filter((o) => o.nameEn?.startsWith(activeTab));

    return (
        <div className="builder-step">
            <div className="builder-step-header">
                <span className="eyebrow">שלב 3 / 7 · שדרה</span>
                <h2>שדרה</h2>
                <p>בחרו את סגנון השדרה לכניסה לאולם.</p>
            </div>
            <OptionPreview selected={selectedOpt} venueImageUrl={venueImageUrl} />
            <div className="card">
                {tabs.length > 1 && (
                    <div className="tab-bar" style={{ marginBottom: "18px" }}>
                        {tabs.map((tab) => (
                            <button
                                key={tab}
                                className={`tab-btn${activeTab === tab ? " tab-btn-active" : ""}`}
                                onClick={() => setActiveTab(tab)}
                            >
                                {tab === "ALL" ? "הכל" : tab}
                            </button>
                        ))}
                    </div>
                )}
                <OptionGrid
                    options={visibleOptions}
                    selectedId={selectedId}
                    onSelect={onSelect}
                    effectivePrice={effectivePrice}
                />
            </div>
        </div>
    );
}

// Step 3 — Tables with sub-steps (domain-specific pools)

function Step3Tables({
    frameOpts,
    flowerOpts,
    candleOpts,
    knightTableCount,
    setKnightTableCount,
    subs,
    setSubs,
    effectivePrice,
}: {
    frameOpts: PackageOptionResponse[];
    flowerOpts: PackageOptionResponse[];
    candleOpts: PackageOptionResponse[];
    knightTableCount: number;
    setKnightTableCount: (n: number) => void;
    subs: TableSubSelections;
    setSubs: (s: TableSubSelections) => void;
    effectivePrice: (opt: PackageOptionResponse) => number;
}) {

    function update<K extends keyof TableSubSelections>(key: K, val: TableSubSelections[K]) {
        setSubs({ ...subs, [key]: val });
    }

    return (
        <div className="builder-step">
            <div className="builder-step-header">
                <span className="eyebrow">שלב 4 / 7 · שולחנות ועיצוב</span>
                <h2>שולחנות</h2>
                <p>בחרו מספר שולחנות אבירים ואת עיצוב השולחנות.</p>
            </div>

            <div className="card">
                <h3 style={{ marginBottom: "14px" }}>שולחנות אבירים</h3>
                <p className="muted" style={{ fontSize: "0.88rem", marginBottom: "16px" }}>
                    0 = ללא שולחנות אבירים
                </p>
                <div className="knight-count-row">
                    {[0, 1, 2, 3, 4].map((n) => (
                        <button
                            key={n}
                            type="button"
                            className={`knight-count-btn${knightTableCount === n ? " selected" : ""}`}
                            onClick={() => {
                                setKnightTableCount(n);
                                if (n === 0) setSubs({ ...EMPTY_TABLE_SUBS });
                            }}
                        >
                            {n}
                        </button>
                    ))}
                </div>
            </div>

            {knightTableCount > 0 && (
                <>
                    {/* Sub-step A: Frame */}
                    {frameOpts.length > 0 && (
                        <div className="card">
                            <h3 style={{ marginBottom: "12px" }}>מסגרת שולחן</h3>
                            <OptionGrid
                                options={frameOpts}
                                selectedId={subs.frameId}
                                onSelect={(id) => update("frameId", subs.frameId === id ? null : id)}
                                effectivePrice={effectivePrice}
                            />
                        </div>
                    )}

                    {/* Sub-step B: Primary flower */}
                    {flowerOpts.length > 0 && (
                        <div className="card">
                            <h3 style={{ marginBottom: "12px" }}>פרח ראשי</h3>
                            <OptionGrid
                                options={flowerOpts}
                                selectedId={subs.primaryFlowerId}
                                onSelect={(id) => {
                                    const newId = subs.primaryFlowerId === id ? null : id;
                                    setSubs({ ...subs, primaryFlowerId: newId, secondaryFlowerId: null });
                                }}
                                effectivePrice={effectivePrice}
                            />
                        </div>
                    )}

                    {/* Sub-step C: Secondary flower (available after primary selected, different option required) */}
                    {subs.primaryFlowerId !== null && flowerOpts.length > 1 && (
                        <div className="card">
                            <h3 style={{ marginBottom: "6px" }}>פרח משני <span className="muted" style={{ fontSize: "0.82rem", fontWeight: 400 }}>(לא חובה)</span></h3>
                            <p className="muted" style={{ fontSize: "0.84rem", marginBottom: "14px" }}>
                                בחרו פרח משני להשלמת העיצוב, או דלגו לשלב הבא.
                            </p>
                            <OptionGrid
                                options={flowerOpts}
                                selectedId={subs.secondaryFlowerId}
                                onSelect={(id) => update("secondaryFlowerId", subs.secondaryFlowerId === id ? null : id)}
                                effectivePrice={effectivePrice}
                                disabledIds={subs.primaryFlowerId ? [subs.primaryFlowerId] : []}
                            />
                        </div>
                    )}

                    {/* Sub-step D: Candle holder mode */}
                    <div className="card">
                        <h3 style={{ marginBottom: "14px" }}>מצב פמוט</h3>
                        <div className="candle-mode-row">
                            <button
                                type="button"
                                className={`candle-mode-btn${subs.candleMode === "random" ? " selected" : ""}`}
                                onClick={() => update("candleMode", "random")}
                            >
                                <span>🕯 אקראי</span>
                                <small>מיקום אקראי</small>
                            </button>
                            <button
                                type="button"
                                className={`candle-mode-btn${subs.candleMode === "specific" ? " selected" : ""}`}
                                onClick={() => update("candleMode", "specific")}
                            >
                                <span>🕯 ספירה</span>
                                <small>בחרו כמות</small>
                            </button>
                        </div>
                        {subs.candleMode === "specific" && (
                            <div style={{ marginTop: "16px" }}>
                                <p className="muted" style={{ fontSize: "0.86rem", marginBottom: "10px" }}>
                                    מספר פמוטים לשולחן:
                                </p>
                                <div className="knight-count-row">
                                    {[1, 2, 3].map((n) => (
                                        <button
                                            key={n}
                                            type="button"
                                            className={`knight-count-btn${subs.candleCount === n ? " selected" : ""}`}
                                            onClick={() => update("candleCount", n)}
                                        >
                                            {n}
                                        </button>
                                    ))}
                                </div>
                            </div>
                        )}
                        {/* Candle option selection */}
                        {candleOpts.length > 0 && (
                            <div style={{ marginTop: "18px" }}>
                                <p className="muted" style={{ fontSize: "0.86rem", marginBottom: "12px" }}>
                                    בחרו פמוט:
                                </p>
                                <OptionGrid
                                    options={candleOpts}
                                    selectedId={subs.candleOptionId}
                                    onSelect={(id) => update("candleOptionId", subs.candleOptionId === id ? null : id)}
                                    effectivePrice={effectivePrice}
                                />
                            </div>
                        )}
                    </div>
                </>
            )}
        </div>
    );
}

// Step 4 — Napkins & Tablecloths (sequential, domain-specific pools)

function Step4NapkinsTablecloths({
    napkinOpts,
    tableclothOpts,
    napkinId,
    setNapkinId,
    tableclothId,
    setTableclothId,
    effectivePrice,
}: {
    napkinOpts: PackageOptionResponse[];
    tableclothOpts: PackageOptionResponse[];
    napkinId: number | null;
    setNapkinId: (id: number | null) => void;
    tableclothId: number | null;
    setTableclothId: (id: number | null) => void;
    effectivePrice: (opt: PackageOptionResponse) => number;
}) {

    return (
        <div className="builder-step">
            <div className="builder-step-header">
                <span className="eyebrow">שלב 5 / 7 · מפיות ומפות</span>
                <h2>מפיות ומפות</h2>
                <p>בחרו מפית ומפה לשולחנות האירוע.</p>
            </div>

            <div className="card">
                <h3 style={{ marginBottom: "14px" }}>מפית</h3>
                <p className="muted" style={{ fontSize: "0.88rem", marginBottom: "14px" }}>
                    בחרו את סגנון המפית.
                </p>
                <OptionGrid
                    options={napkinOpts}
                    selectedId={napkinId}
                    onSelect={(id) => setNapkinId(napkinId === id ? null : id)}
                    effectivePrice={effectivePrice}
                />
            </div>

            <div className="card" style={{ opacity: napkinId ? 1 : 0.55, transition: "opacity 0.3s" }}>
                <h3 style={{ marginBottom: "14px" }}>
                    מפה
                    {!napkinId && (
                        <span className="muted" style={{ fontSize: "0.8rem", fontWeight: 400, marginInlineStart: "8px" }}>
                            (בחרו מפית תחילה)
                        </span>
                    )}
                </h3>
                <OptionGrid
                    options={tableclothOpts}
                    selectedId={tableclothId}
                    onSelect={(id) => napkinId && setTableclothId(tableclothId === id ? null : id)}
                    effectivePrice={effectivePrice}
                />
            </div>
        </div>
    );
}

// Step 5 — Bride Chair (BRIDE_CHAIR category)

function Step5BrideChair({
    options,
    selectedId,
    onSelect,
    effectivePrice,
}: {
    options: PackageOptionResponse[];
    selectedId: number | null;
    onSelect: (id: number) => void;
    effectivePrice: (opt: PackageOptionResponse) => number;
}) {
    return (
        <div className="builder-step">
            <div className="builder-step-header">
                <span className="eyebrow">שלב 6 / 7 · כיסא כלה</span>
                <h2>כיסא כלה</h2>
                <p>בחרו את עיצוב כיסא הכלה.</p>
            </div>
            <div className="card">
                <OptionGrid
                    options={options}
                    selectedId={selectedId}
                    onSelect={onSelect}
                    effectivePrice={effectivePrice}
                />
            </div>
        </div>
    );
}

// Step 6 — Summary

function Step6Summary({
    eventDetails,
    venues,
    selectedOptions,
    knightTableCount,
    tableSubs,
    runningTotal,
    effectivePrice,
    basePackagePrice,
    submitError,
    submitting,
    onSubmit,
}: {
    eventDetails: EventDetails;
    venues: VenueResponse[];
    selectedOptions: PackageOptionResponse[];
    knightTableCount: number;
    tableSubs: TableSubSelections;
    runningTotal: number;
    effectivePrice: (opt: PackageOptionResponse) => number;
    basePackagePrice: number;
    submitError: string | null;
    submitting: boolean;
    onSubmit: () => void;
}) {
    const venue = venues.find((v) => v.id === eventDetails.venueId);
    return (
        <div className="builder-step">
            <div className="builder-step-header">
                <span className="eyebrow">שלב 7 / 7</span>
                <h2>סיכום ושליחה</h2>
                <p>בדקו את הפרטים לפני השליחה.</p>
            </div>

            <div className="card summary-card">
                <h3 className="summary-section-title">פרטי האירוע</h3>
                <div className="summary-grid">
                    {[
                        ["אולם", venue?.nameHe ?? "—"],
                        ["תאריך", eventDetails.eventDate
                            ? new Date(eventDetails.eventDate).toLocaleDateString("he-IL", { year: "numeric", month: "long", day: "numeric" })
                            : "—"],
                        ["שם ליצירת קשר", eventDetails.eventContactName],
                        ["מספר זהות", eventDetails.eventCustomerIdentityNumber],
                        ["טלפון", eventDetails.eventContactPhoneNumber],
                        ...(knightTableCount > 0 ? [["שולחנות אבירים", String(knightTableCount)]] : []),
                        ...(tableSubs.candleMode !== "random" && knightTableCount > 0
                            ? [["פמוטים לשולחן", String(tableSubs.candleCount)]]
                            : []),
                    ].map(([label, val]) => (
                        <div key={label} className="summary-row">
                            <span className="summary-label">{label}</span>
                            <span className="summary-value">{val}</span>
                        </div>
                    ))}
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
                {submitError && <div className="builder-submit-error">{submitError}</div>}
                <button className="button builder-submit-btn" onClick={onSubmit} disabled={submitting}>
                    {submitting ? "שולח..." : "שלח לאישור"}
                </button>
                <p className="muted" style={{ marginTop: "12px", fontSize: "0.84rem", textAlign: "center" }}>
                    לאחר השליחה נציגנו יצרו עמך קשר לאישור סופי.
                </p>
            </div>
        </div>
    );
}

// Main component

export default function CustomerBuilderPage() {
    usePageTitle("בניית חבילת אירוע");
    const { user } = useAuth();
    const { showToast } = useToast();
    const { setCurrentStep, setRunningTotal, setBuilderActive } = useBuilderUi();
    const navigate = useNavigate();

    // ── Loaded data ───────────────────────────────────────────────────────────
    const [loading, setLoading] = useState(true);
    const [venues, setVenues] = useState<VenueResponse[]>([]);
    const [options, setOptions] = useState<PackageOptionResponse[]>([]);
    const [overrideMap, setOverrideMap] = useState<Record<number, number>>({});
    const [compatibilityMap, setCompatibilityMap] = useState<ChuppahCompatibilityMap>({});

    // ── Step state ────────────────────────────────────────────────────────────
    const [step, setStep] = useState(0);

    // Step 0 — Event details
    const [eventDetails, setEventDetails] = useState<EventDetails>({
        venueId: null,
        eventDate: "",
        eventContactName: user?.fullName ?? "",
        eventCustomerIdentityNumber: user?.customerIdentityNumber ?? "",
        eventContactPhoneNumber: user?.phoneNumber ?? "",
    });

    const [chuppahId, setChuppahId] = useState<number | null>(null);
    const [selectedChuppahUpgradeIds, setSelectedChuppahUpgradeIds] = useState<number[]>([]);
    const [aisleId, setAisleId] = useState<number | null>(null);
    const [knightTableCount, setKnightTableCount] = useState(0);
    const [tableSubs, setTableSubs] = useState<TableSubSelections>({ ...EMPTY_TABLE_SUBS });
    const [napkinId, setNapkinId] = useState<number | null>(null);
    const [tableclothId, setTableclothId] = useState<number | null>(null);
    const [brideChairId, setBrideChairId] = useState<number | null>(null);

    const [stepError, setStepError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);
    const [submitted, setSubmitted] = useState(false);
    const [submitError, setSubmitError] = useState<string | null>(null);

    const chuppahOpts = useMemo(() => options.filter((o) => o.category === "CHUPPAH"), [options]);
    const chuppahUpgradeOpts = useMemo(() => options.filter((o) => o.category === "CHUPPAH_UPGRADE"), [options]);
    const aisleOpts = useMemo(() => options.filter((o) => o.category === "AISLE"), [options]);
    const tableFrameOpts = useMemo(() => options.filter((o) => o.category === "TABLE_FRAME"), [options]);
    const tableFlowerOpts = useMemo(() => options.filter((o) => o.category === "TABLE_FLOWER"), [options]);
    const tableCandleOpts = useMemo(() => options.filter((o) => o.category === "TABLE_CANDLE"), [options]);
    const napkinOpts = useMemo(() => options.filter((o) => o.category === "NAPKIN"), [options]);
    const tableclothOpts = useMemo(() => options.filter((o) => o.category === "TABLECLOTH"), [options]);
    const brideChairOpts = useMemo(() => options.filter((o) => o.category === "BRIDE_CHAIR"), [options]);

    // The venue image for preview background
    const selectedVenueImageUrl = useMemo(() =>
        venues.find((v) => v.id === eventDetails.venueId)?.imageUrl ?? null,
        [venues, eventDetails.venueId]
    );

    // ── Load ──────────────────────────────────────────────────────────────────
    useEffect(() => {
        async function load() {
            try {
                const [v, opts, overrides, compat] = await Promise.all([
                    listActiveVenues(),
                    listActivePackageOptions(),
                    listMyPriceOverrides(),
                    getChuppahCompatibilityMap(),
                ]);
                setVenues(v);
                setOptions(opts);
                const map: Record<number, number> = {};
                for (const o of overrides) map[o.optionId] = o.customPrice;
                setOverrideMap(map);
                setCompatibilityMap(compat);
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

    // ── Pricing ───────────────────────────────────────────────────────────────
    function effectivePrice(opt: PackageOptionResponse): number {
        return overrideMap[opt.id] !== undefined ? overrideMap[opt.id] : opt.globalPrice;
    }

    const allSelectedIds = useMemo(() => [
        chuppahId,
        ...selectedChuppahUpgradeIds,
        aisleId,
        tableSubs.frameId,
        tableSubs.primaryFlowerId,
        tableSubs.secondaryFlowerId,
        tableSubs.candleOptionId,
        napkinId,
        tableclothId,
        brideChairId,
    ].filter((id): id is number => id !== null), [
        chuppahId, selectedChuppahUpgradeIds, aisleId, tableSubs, napkinId, tableclothId, brideChairId
    ]);

    const selectedOptionObjects = useMemo(() =>
        allSelectedIds.map((id) => options.find((o) => o.id === id)).filter((o): o is PackageOptionResponse => o !== undefined),
        [allSelectedIds, options]
    );

    const runningTotal = useMemo(() => {
        const base = user?.basePackagePrice ?? 0;
        const sum = selectedOptionObjects.reduce((s, o) => s + effectivePrice(o), 0);
        return base + sum;
    }, [selectedOptionObjects, overrideMap, user]);

    // Sync context
    useEffect(() => { setCurrentStep(step); }, [step]);
    useEffect(() => { setRunningTotal(runningTotal); }, [runningTotal]);

    function validateCurrentStep(): string | null {
        switch (step) {
            case 0:
                if (!eventDetails.venueId) return "נא לבחור אולם";
                if (!eventDetails.eventContactName.trim()) return "נא להזין שם ליצירת קשר";
                if (!eventDetails.eventCustomerIdentityNumber.trim()) return "נא להזין מספר זהות";
                if (!eventDetails.eventContactPhoneNumber.trim()) return "נא להזין מספר טלפון";
                if (!eventDetails.eventDate) return "נא לבחור תאריך אירוע";
                if (eventDetails.eventDate < todayIso()) return "תאריך האירוע חייב להיות בעתיד";
                return null;
            case 1:
                if (chuppahOpts.length > 0 && !chuppahId) return "נא לבחור עיצוב חופה";
                return null;
            case 2:
                if (aisleOpts.length > 0 && !aisleId) return "נא לבחור סגנון שדרה";
                return null;
            case 3:
                if (knightTableCount > 0) {
                    if (tableFrameOpts.length > 0 && !tableSubs.frameId) return "נא לבחור מסגרת שולחן";
                    if (tableFlowerOpts.length > 0 && !tableSubs.primaryFlowerId) return "נא לבחור פרח ראשי";
                    if (tableCandleOpts.length > 0 && !tableSubs.candleOptionId) return "נא לבחור פמוט";
                }
                return null;
            case 4:
                if (napkinOpts.length > 0 && !napkinId) return "נא לבחור מפית";
                if (tableclothOpts.length > 0 && !tableclothId) return "נא לבחור מפה";
                return null;
            case 5:
                if (brideChairOpts.length > 0 && !brideChairId) return "נא לבחור כיסא כלה";
                return null;
            default:
                return null;
        }
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

    // ── Submission ────────────────────────────────────────────────────────────
    async function handleSubmit() {
        if (!eventDetails.venueId || !chuppahId) return;
        setSubmitting(true);
        setSubmitError(null);

        // Build optionIds for remaining sections (exclude chuppah, upgrades, aisle — sent explicitly)
        const otherOptionIds = [
            tableSubs.frameId,
            tableSubs.primaryFlowerId,
            tableSubs.secondaryFlowerId,
            tableSubs.candleOptionId,
            napkinId,
            tableclothId,
            brideChairId,
        ].filter((id): id is number => id !== null);

        try {
            await submitPackageRequest({
                venueId: eventDetails.venueId,
                chuppahOptionId: chuppahId,
                chuppahUpgradeIds: selectedChuppahUpgradeIds,
                aisleOptionId: aisleId,
                eventContactName: eventDetails.eventContactName,
                eventCustomerIdentityNumber: eventDetails.eventCustomerIdentityNumber,
                eventContactPhoneNumber: eventDetails.eventContactPhoneNumber,
                eventDate: eventDetails.eventDate,
                knightTableCount: knightTableCount > 0 ? knightTableCount : null,
                optionIds: otherOptionIds,
            });
            setSubmitted(true);
        } catch (err: unknown) {
            const status = (err as { response?: { status?: number } })?.response?.status;
            const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
            if (status === 403) {
                setSubmitError("חשבונך אינו פעיל. פנה אל מנהל המערכת.");
            } else {
                setSubmitError(msg ?? "שגיאה בשליחת הבקשה. אנא נסה שוב.");
            }
        } finally {
            setSubmitting(false);
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────

    if (loading) {
        return (
            <main className="page builder-page">
                <p className="muted">טוען את בונה החבילות...</p>
            </main>
        );
    }

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
                                setStep(0); setChuppahId(null); setSelectedChuppahUpgradeIds([]); setAisleId(null);
                                setKnightTableCount(0); setTableSubs({ ...EMPTY_TABLE_SUBS });
                                setNapkinId(null); setTableclothId(null); setBrideChairId(null);
                                setEventDetails({
                                    venueId: null, eventDate: "",
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

    function renderStep() {
        switch (step) {
            case 0:
                return <Step0 venues={venues} eventDetails={eventDetails} setEventDetails={setEventDetails} />;
            case 1:
                return (
                    <Step1Chuppah
                        chuppahOpts={chuppahOpts}
                        upgradeOpts={chuppahUpgradeOpts}
                        selectedId={chuppahId}
                        selectedUpgradeIds={selectedChuppahUpgradeIds}
                        compatibilityMap={compatibilityMap}
                        onSelectChuppah={(id) => {
                            setChuppahId(chuppahId === id ? null : id);
                            setSelectedChuppahUpgradeIds([]);
                        }}
                        onToggleUpgrade={(id) => {
                            setSelectedChuppahUpgradeIds((prev) =>
                                prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
                            );
                        }}
                        effectivePrice={effectivePrice}
                        venueImageUrl={selectedVenueImageUrl}
                    />
                );
            case 2:
                return (
                    <Step2Aisle
                        options={aisleOpts}
                        selectedId={aisleId}
                        onSelect={(id) => setAisleId(aisleId === id ? null : id)}
                        effectivePrice={effectivePrice}
                        venueImageUrl={selectedVenueImageUrl}
                    />
                );
            case 3:
                return (
                    <Step3Tables
                        frameOpts={tableFrameOpts}
                        flowerOpts={tableFlowerOpts}
                        candleOpts={tableCandleOpts}
                        knightTableCount={knightTableCount}
                        setKnightTableCount={setKnightTableCount}
                        subs={tableSubs}
                        setSubs={setTableSubs}
                        effectivePrice={effectivePrice}
                    />
                );
            case 4:
                return (
                    <Step4NapkinsTablecloths
                        napkinOpts={napkinOpts}
                        tableclothOpts={tableclothOpts}
                        napkinId={napkinId}
                        setNapkinId={setNapkinId}
                        tableclothId={tableclothId}
                        setTableclothId={setTableclothId}
                        effectivePrice={effectivePrice}
                    />
                );
            case 5:
                return (
                    <Step5BrideChair
                        options={brideChairOpts}
                        selectedId={brideChairId}
                        onSelect={(id) => setBrideChairId(brideChairId === id ? null : id)}
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
                        tableSubs={tableSubs}
                        runningTotal={runningTotal}
                        effectivePrice={effectivePrice}
                        basePackagePrice={user?.basePackagePrice ?? 0}
                        submitError={submitError}
                        submitting={submitting}
                        onSubmit={handleSubmit}
                    />
                );
        }
    }

    return (
        <main className="page builder-page">
            {renderStep()}

            {stepError && <div className="builder-step-error">{stepError}</div>}

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
