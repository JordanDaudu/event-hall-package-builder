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

interface TableDesignState {
    frameId: number | null;
    primaryFlowerSize: "LARGE" | "SMALL" | null;
    primaryFlowerId: number | null;
    secondaryFlowerId: number | null;
    candleMode: "RANDOM" | "SELECTED";
    candleHolderIds: number[];
}

const EMPTY_TABLE_DESIGN: TableDesignState = {
    frameId: null,
    primaryFlowerSize: null,
    primaryFlowerId: null,
    secondaryFlowerId: null,
    candleMode: "RANDOM",
    candleHolderIds: [],
};

// Preview component

function OptionPreview({
    selected,
    isHoverPreview,
}: {
    selected: PackageOptionResponse | null;
    venueImageUrl?: string | null;
    isHoverPreview?: boolean;
}) {
    const imageUrl = selected?.imageUrl ?? null;
    const isHover = isHoverPreview === true && selected !== null;

    return (
        <div className="selected-preview">
            {imageUrl ? (
                <img
                    src={imageUrl}
                    alt={selected?.nameHe ?? "תמונה"}
                    className="selected-preview-image"
                />
            ) : (
                <div className="selected-preview-fallback">
                    <span>{selected ? "אין תמונה זמינה" : "העבירו את העכבר על אפשרות לצפייה"}</span>
                </div>
            )}

            {isHover ? (
                <div className="preview-overlay">
                    <span>תצוגה מקדימה</span>
                    <strong>{selected!.nameHe}</strong>
                </div>
            ) : selected ? (
                <>
                    <div className="selected-preview-badge">הבחירה הנוכחית</div>
                    <div className="selected-preview-footer">
                        <span>הבחירה הנוכחית:</span>
                        <strong>{selected.nameHe}</strong>
                    </div>
                </>
            ) : null}
        </div>
    );
}

// TableComposedPreview — three-part composed preview (frame + flowers + candle)

function TableComposedPreview({
    frameOpt,
    primaryFlowerOpt,
    secondaryFlowerOpt,
    candleMode,
    selectedCandleOpts,
    previewCandleOpt,
}: {
    frameOpt: PackageOptionResponse | null;
    primaryFlowerOpt: PackageOptionResponse | null;
    secondaryFlowerOpt: PackageOptionResponse | null;
    candleMode: "RANDOM" | "SELECTED";
    selectedCandleOpts: PackageOptionResponse[];
    previewCandleOpt: PackageOptionResponse | null;
}) {
    const displayCandle = previewCandleOpt ?? (selectedCandleOpts.length > 0 ? selectedCandleOpts[0] : null);
    const hasContent = frameOpt || primaryFlowerOpt || candleMode === "RANDOM" || selectedCandleOpts.length > 0;
    if (!hasContent) return null;

    return (
        <div className="table-composed-preview">
            {/* Frame part */}
            <div className="table-preview-part">
                <div className="table-preview-part-img">
                    {frameOpt?.imageUrl ? (
                        <img src={frameOpt.imageUrl} alt={frameOpt.nameHe} />
                    ) : (
                        <div className="table-preview-placeholder-icon">🖼</div>
                    )}
                </div>
                <div className="table-preview-part-label">מסגרת</div>
                <div className="table-preview-part-name">{frameOpt?.nameHe ?? "—"}</div>
            </div>

            {/* Flower part */}
            <div className="table-preview-part">
                <div className="table-preview-part-img">
                    {primaryFlowerOpt?.imageUrl ? (
                        <img src={primaryFlowerOpt.imageUrl} alt={primaryFlowerOpt.nameHe} />
                    ) : (
                        <div className="table-preview-placeholder-icon">🌸</div>
                    )}
                </div>
                <div className="table-preview-part-label">פרחים</div>
                <div className="table-preview-part-name">
                    {primaryFlowerOpt
                        ? secondaryFlowerOpt
                            ? `${primaryFlowerOpt.nameHe} + ${secondaryFlowerOpt.nameHe}`
                            : primaryFlowerOpt.nameHe
                        : "—"}
                </div>
            </div>

            {/* Candle part */}
            <div className="table-preview-part">
                <div className="table-preview-part-img">
                    {candleMode === "RANDOM" ? (
                        <div className="table-preview-placeholder-icon">🕯</div>
                    ) : displayCandle?.imageUrl ? (
                        <img src={displayCandle.imageUrl} alt={displayCandle.nameHe} />
                    ) : (
                        <div className="table-preview-placeholder-icon">🕯</div>
                    )}
                </div>
                <div className="table-preview-part-label">פמוטים</div>
                <div className="table-preview-part-name">
                    {candleMode === "RANDOM"
                        ? "בחירה רנדומלית"
                        : selectedCandleOpts.length > 0
                            ? `נבחרו ${selectedCandleOpts.length} פמוטים`
                            : "—"}
                </div>
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
    onHover,
}: {
    options: PackageOptionResponse[];
    selectedId: number | null;
    onSelect: (id: number) => void;
    effectivePrice: (opt: PackageOptionResponse) => number;
    disabledIds?: number[];
    onHover?: (id: number | null) => void;
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
                        onMouseEnter={() => onHover?.(opt.id)}
                        onMouseLeave={() => onHover?.(null)}
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
    hoveredChuppah,
    hoveredUpgrade,
}: {
    selectedChuppah: PackageOptionResponse | null;
    selectedUpgrades: PackageOptionResponse[];
    venueImageUrl?: string | null;
    hoveredChuppah?: PackageOptionResponse | null;
    hoveredUpgrade?: PackageOptionResponse | null;
}) {
    const displayChuppah = hoveredChuppah ?? selectedChuppah;
    const chuppahImageUrl =
        displayChuppah?.visualBehavior === "REPLACE_IMAGE" ? (displayChuppah.imageUrl ?? null) : null;

    const overlayUpgrades = hoveredUpgrade
        ? [hoveredUpgrade].filter((u) => u.visualBehavior === "OVERLAY_IMAGE" && u.imageUrl)
        : selectedUpgrades.filter((u) => u.visualBehavior === "OVERLAY_IMAGE" && u.imageUrl);

    const isHoverPreview = hoveredChuppah !== null || hoveredUpgrade !== null;
    const hoveredName = hoveredChuppah?.nameHe ?? hoveredUpgrade?.nameHe ?? null;

    return (
        <div className="chuppah-preview">
            {/* Background: chuppah image (REPLACE_IMAGE) or venue image as dim backdrop */}
            {chuppahImageUrl ? (
                <img src={chuppahImageUrl} alt={displayChuppah?.nameHe ?? "חופה"} className="chuppah-preview-main" />
            ) : venueImageUrl ? (
                <img src={venueImageUrl} alt="רקע" className="chuppah-preview-venue-bg" />
            ) : null}

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

            {/* Labels: hover overlay or selected badge */}
            {isHoverPreview && hoveredName ? (
                <div className="preview-overlay">
                    <span>תצוגה מקדימה</span>
                    <strong>{hoveredName}</strong>
                </div>
            ) : selectedChuppah ? (
                <>
                    <div className="selected-preview-badge">הבחירה הנוכחית</div>
                    <div className="selected-preview-footer">
                        <span>הבחירה הנוכחית:</span>
                        <strong>{selectedChuppah.nameHe}</strong>
                    </div>
                </>
            ) : (
                <div className="chuppah-preview-placeholder">לחצו על חופה לצפייה בתצוגה מקדימה</div>
            )}
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
    const [hoveredChuppahId, setHoveredChuppahId] = useState<number | null>(null);
    const [hoveredUpgradeId, setHoveredUpgradeId] = useState<number | null>(null);

    const selectedOpt = chuppahOpts.find((o) => o.id === selectedId) ?? null;
    const hoveredChuppahOpt = hoveredChuppahId !== null ? (chuppahOpts.find((o) => o.id === hoveredChuppahId) ?? null) : null;
    const hoveredUpgradeOpt = hoveredUpgradeId !== null ? (upgradeOpts.find((o) => o.id === hoveredUpgradeId) ?? null) : null;

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
                hoveredChuppah={hoveredChuppahOpt}
                hoveredUpgrade={hoveredUpgradeOpt}
            />
            <div className="card">
                <OptionGrid
                    options={chuppahOpts}
                    selectedId={selectedId}
                    onSelect={onSelectChuppah}
                    effectivePrice={effectivePrice}
                    onHover={setHoveredChuppahId}
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
                                    onMouseEnter={() => setHoveredUpgradeId(u.id)}
                                    onMouseLeave={() => setHoveredUpgradeId(null)}
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
    const [hoveredId, setHoveredId] = useState<number | null>(null);
    const selectedOpt = options.find((o) => o.id === selectedId) ?? null;
    const hoveredOpt = hoveredId !== null ? (options.find((o) => o.id === hoveredId) ?? null) : null;
    const previewOpt = hoveredOpt ?? selectedOpt;

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
            <OptionPreview selected={previewOpt} isHoverPreview={hoveredId !== null} />
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
                    onHover={setHoveredId}
                />
            </div>
        </div>
    );
}

// TableDesignPanel — reusable sub-component for regular/knight table design selections
// Each panel manages its own internal hover state per section (no shared hover).

function TableDesignPanel({
    label,
    eyebrow,
    frameOpts,
    flowerOpts,
    candleOpts,
    subs,
    setSubs,
    effectivePrice,
}: {
    label: string;
    eyebrow: string;
    frameOpts: PackageOptionResponse[];
    flowerOpts: PackageOptionResponse[];
    candleOpts: PackageOptionResponse[];
    subs: TableDesignState;
    setSubs: (s: TableDesignState) => void;
    effectivePrice: (opt: PackageOptionResponse) => number;
}) {
    function update<K extends keyof TableDesignState>(key: K, val: TableDesignState[K]) {
        setSubs({ ...subs, [key]: val });
    }

    // Per-section hover states — isolated so panels don't bleed into each other
    const [hoveredFrameId, setHoveredFrameId] = useState<number | null>(null);
    const [hoveredPrimaryFlowerId, setHoveredPrimaryFlowerId] = useState<number | null>(null);
    const [hoveredSecondaryFlowerId, setHoveredSecondaryFlowerId] = useState<number | null>(null);
    const [hoveredCandleId, setHoveredCandleId] = useState<number | null>(null);

    // Resolve display opts: hover takes precedence over selected
    const selectedFrameOpt = frameOpts.find((o) => o.id === subs.frameId) ?? null;
    const previewFrameOpt = (hoveredFrameId !== null ? frameOpts.find((o) => o.id === hoveredFrameId) : null) ?? selectedFrameOpt;

    const largeFlowerOpts = flowerOpts.filter((o) => o.flowerSize === "LARGE" || !o.flowerSize);
    const smallFlowerOpts = flowerOpts.filter((o) => o.flowerSize === "SMALL");
    const primaryFlowerPool =
        subs.primaryFlowerSize === "SMALL" ? smallFlowerOpts
        : subs.primaryFlowerSize === "LARGE" ? largeFlowerOpts
        : flowerOpts;

    const selectedPrimaryFlowerOpt = flowerOpts.find((o) => o.id === subs.primaryFlowerId) ?? null;
    const previewPrimaryFlowerOpt =
        (hoveredPrimaryFlowerId !== null ? flowerOpts.find((o) => o.id === hoveredPrimaryFlowerId) : null)
        ?? selectedPrimaryFlowerOpt;

    const selectedSecondaryFlowerOpt = subs.secondaryFlowerId !== null
        ? flowerOpts.find((o) => o.id === subs.secondaryFlowerId) ?? null
        : null;
    const previewSecondaryFlowerOpt =
        (hoveredSecondaryFlowerId !== null ? flowerOpts.find((o) => o.id === hoveredSecondaryFlowerId) : null)
        ?? selectedSecondaryFlowerOpt;

    const selectedCandleOpts = candleOpts.filter((o) => subs.candleHolderIds.includes(o.id));
    const previewCandleOpt = hoveredCandleId !== null ? candleOpts.find((o) => o.id === hoveredCandleId) ?? null : null;

    // Secondary flower is only visible when primaryFlowerSize is LARGE and a primary is selected
    const showSecondaryFlower = subs.primaryFlowerSize === "LARGE" && subs.primaryFlowerId !== null && smallFlowerOpts.length > 0;

    function handleFlowerSizeChange(size: "LARGE" | "SMALL") {
        setSubs({ ...subs, primaryFlowerSize: size, primaryFlowerId: null, secondaryFlowerId: null });
    }

    function handleCandleModeChange(mode: "RANDOM" | "SELECTED") {
        // Always clear candleHolderIds when switching modes
        setSubs({ ...subs, candleMode: mode, candleHolderIds: [] });
    }

    function handlePrimaryFlowerSelect(id: number) {
        const newId = subs.primaryFlowerId === id ? null : id;
        setSubs({ ...subs, primaryFlowerId: newId, secondaryFlowerId: null });
    }

    return (
        <div style={{ marginBottom: "0" }}>
            <div style={{ padding: "18px 0 6px", borderTop: "2px solid var(--color-border, #e5e7eb)" }}>
                <h3 style={{ marginBottom: "4px" }}>{label}</h3>
                <p className="muted" style={{ fontSize: "0.85rem", marginBottom: "18px" }}>{eyebrow}</p>
            </div>

            {/* Composed preview — shows current selections / hover state */}
            <TableComposedPreview
                frameOpt={previewFrameOpt}
                primaryFlowerOpt={previewPrimaryFlowerOpt}
                secondaryFlowerOpt={showSecondaryFlower ? previewSecondaryFlowerOpt : null}
                candleMode={subs.candleMode}
                selectedCandleOpts={selectedCandleOpts}
                previewCandleOpt={previewCandleOpt}
            />

            {/* Frame */}
            {frameOpts.length > 0 && (
                <div className="card">
                    <h4 style={{ marginBottom: "12px" }}>
                        מסגרת שולחן <span style={{ color: "var(--color-error, #ef4444)", fontSize: "0.8rem" }}>*</span>
                    </h4>
                    <OptionGrid
                        options={frameOpts}
                        selectedId={subs.frameId}
                        onSelect={(id) => update("frameId", subs.frameId === id ? null : id)}
                        effectivePrice={effectivePrice}
                        onHover={setHoveredFrameId}
                    />
                </div>
            )}

            {/* Flower section */}
            {flowerOpts.length > 0 && (
                <div className="card">
                    <h4 style={{ marginBottom: "14px" }}>בחירת פרחים</h4>

                    {/* Flower size picker — must choose before seeing flower options */}
                    <p className="muted" style={{ fontSize: "0.86rem", marginBottom: "10px" }}>בחרו סוג פרח ראשי</p>
                    <div className="flower-size-picker">
                        <button
                            type="button"
                            className={`flower-size-btn${subs.primaryFlowerSize === "LARGE" ? " selected" : ""}`}
                            onClick={() => handleFlowerSizeChange("LARGE")}
                        >
                            🌸 פרח גדול
                        </button>
                        <button
                            type="button"
                            className={`flower-size-btn${subs.primaryFlowerSize === "SMALL" ? " selected" : ""}`}
                            onClick={() => handleFlowerSizeChange("SMALL")}
                        >
                            🌼 פרח קטן
                        </button>
                    </div>

                    {/* Primary flower — shown only after size is chosen */}
                    {subs.primaryFlowerSize !== null && (
                        <>
                            <h5 style={{ margin: "18px 0 10px" }}>
                                פרח ראשי <span style={{ color: "var(--color-error, #ef4444)", fontSize: "0.8rem" }}>*</span>
                            </h5>
                            <OptionGrid
                                options={primaryFlowerPool}
                                selectedId={subs.primaryFlowerId}
                                onSelect={handlePrimaryFlowerSelect}
                                effectivePrice={effectivePrice}
                                onHover={setHoveredPrimaryFlowerId}
                            />
                        </>
                    )}
                </div>
            )}

            {/* Secondary small flower — only when primary is LARGE and primary is chosen */}
            {showSecondaryFlower && (
                <div className="card">
                    <h4 style={{ marginBottom: "6px" }}>
                        תוספת פרח קטן
                        <span className="muted" style={{ fontSize: "0.82rem", fontWeight: 400, marginInlineStart: "8px" }}>(לא חובה)</span>
                    </h4>
                    <p className="muted" style={{ fontSize: "0.84rem", marginBottom: "14px" }}>
                        בחרו פרח קטן משני להשלמת העיצוב.
                    </p>
                    <OptionGrid
                        options={smallFlowerOpts}
                        selectedId={subs.secondaryFlowerId}
                        onSelect={(id) => update("secondaryFlowerId", subs.secondaryFlowerId === id ? null : id)}
                        effectivePrice={effectivePrice}
                        onHover={setHoveredSecondaryFlowerId}
                    />
                </div>
            )}

            {/* Candle mode */}
            <div className="card">
                <h4 style={{ marginBottom: "14px" }}>פמוטים</h4>
                <div className="candle-mode-row">
                    <button
                        type="button"
                        className={`candle-mode-btn${subs.candleMode === "RANDOM" ? " selected" : ""}`}
                        onClick={() => handleCandleModeChange("RANDOM")}
                    >
                        <span>🕯 בחירה רנדומלית</span>
                        <small>האולם בוחר את הפמוטים</small>
                    </button>
                    <button
                        type="button"
                        className={`candle-mode-btn${subs.candleMode === "SELECTED" ? " selected" : ""}`}
                        onClick={() => handleCandleModeChange("SELECTED")}
                    >
                        <span>🕯 בחירה ידנית</span>
                        <small>בחרו פמוטים</small>
                    </button>
                </div>
                {subs.candleMode === "SELECTED" && candleOpts.length > 0 && (
                    <div style={{ marginTop: "18px" }}>
                        <p className="muted" style={{ fontSize: "0.86rem", marginBottom: "12px" }}>
                            בחרו עד 3 פמוטים ({subs.candleHolderIds.length}/3 נבחרו):
                        </p>
                        <div className="option-card-grid">
                            {candleOpts.map((opt) => {
                                const isSelected = subs.candleHolderIds.includes(opt.id);
                                const isDisabled = !isSelected && subs.candleHolderIds.length >= 3;
                                return (
                                    <button
                                        key={opt.id}
                                        type="button"
                                        className={`option-card${isSelected ? " selected" : ""}${isDisabled ? " disabled" : ""}${opt.imageUrl ? " has-thumb" : ""}`}
                                        disabled={isDisabled}
                                        onClick={() => {
                                            if (isSelected) {
                                                update("candleHolderIds", subs.candleHolderIds.filter((id) => id !== opt.id));
                                            } else if (!isDisabled) {
                                                update("candleHolderIds", [...subs.candleHolderIds, opt.id]);
                                            }
                                        }}
                                        onMouseEnter={() => setHoveredCandleId(opt.id)}
                                        onMouseLeave={() => setHoveredCandleId(null)}
                                    >
                                        {opt.imageUrl && (
                                            <img src={opt.imageUrl} alt={opt.nameHe} className="option-card-thumb" />
                                        )}
                                        <div className="option-card-inner">
                                            <div className="option-card-name">{opt.nameHe}</div>
                                            {opt.nameEn && <div className="option-card-name-en">{opt.nameEn}</div>}
                                            <div className="option-card-price">{formatILS(effectivePrice(opt))}</div>
                                        </div>
                                        {isSelected && <div className="option-card-check">✓</div>}
                                    </button>
                                );
                            })}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}

// Step 3 — Tables: knight count + regular design (always) + knight design (if count > 0)
// Each TableDesignPanel manages its own internal hover state — no shared state here.

function Step3Tables({
    regularFrameOpts, regularFlowerOpts, regularCandleOpts,
    knightFrameOpts, knightFlowerOpts, knightCandleOpts,
    knightTableCount, setKnightTableCount,
    regularSubs, setRegularSubs,
    knightSubs, setKnightSubs,
    effectivePrice,
}: {
    regularFrameOpts: PackageOptionResponse[];
    regularFlowerOpts: PackageOptionResponse[];
    regularCandleOpts: PackageOptionResponse[];
    knightFrameOpts: PackageOptionResponse[];
    knightFlowerOpts: PackageOptionResponse[];
    knightCandleOpts: PackageOptionResponse[];
    knightTableCount: number;
    setKnightTableCount: (n: number) => void;
    regularSubs: TableDesignState;
    setRegularSubs: (s: TableDesignState) => void;
    knightSubs: TableDesignState;
    setKnightSubs: (s: TableDesignState) => void;
    effectivePrice: (opt: PackageOptionResponse) => number;
}) {
    return (
        <div className="builder-step">
            <div className="builder-step-header">
                <span className="eyebrow">שלב 4 / 7 · שולחנות ועיצוב</span>
                <h2>שולחנות</h2>
                <p>הגדירו את עיצוב השולחנות ואת מספר שולחנות האבירים.</p>
            </div>

            {/* Knight count selector */}
            <div className="card">
                <h3 style={{ marginBottom: "14px" }}>שולחנות אבירים</h3>
                <p className="muted" style={{ fontSize: "0.88rem", marginBottom: "16px" }}>
                    בחרו את מספר שולחנות האבירים (שולחנות הקריאה המרכזיים). 0 = ללא שולחנות אבירים.
                </p>
                <div className="knight-count-row">
                    {[0, 1, 2, 3, 4].map((n) => (
                        <button
                            key={n}
                            type="button"
                            className={`knight-count-btn${knightTableCount === n ? " selected" : ""}`}
                            onClick={() => {
                                setKnightTableCount(n);
                                if (n === 0) setKnightSubs({ ...EMPTY_TABLE_DESIGN });
                            }}
                        >
                            {n}
                        </button>
                    ))}
                </div>
            </div>

            {/* Regular table design — always required */}
            <TableDesignPanel
                label="עיצוב שולחן רגיל"
                eyebrow="בחרו את העיצוב לשולחנות הרגילים של האירוע. שדות אלה הם חובה."
                frameOpts={regularFrameOpts}
                flowerOpts={regularFlowerOpts}
                candleOpts={regularCandleOpts}
                subs={regularSubs}
                setSubs={setRegularSubs}
                effectivePrice={effectivePrice}
            />

            {/* Knight table design — only when count > 0 */}
            {knightTableCount > 0 && (
                <TableDesignPanel
                    label="עיצוב שולחן אביר"
                    eyebrow="בחרו עיצוב ייחודי לשולחנות האבירים. ניתן לבחור עיצוב שונה מהשולחנות הרגילים."
                    frameOpts={knightFrameOpts}
                    flowerOpts={knightFlowerOpts}
                    candleOpts={knightCandleOpts}
                    subs={knightSubs}
                    setSubs={setKnightSubs}
                    effectivePrice={effectivePrice}
                />
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
    // Separate hover states — napkin hover never affects tablecloth preview and vice versa
    const [hoveredNapkinId, setHoveredNapkinId] = useState<number | null>(null);
    const [hoveredTableclothId, setHoveredTableclothId] = useState<number | null>(null);

    const hoveredNapkinOpt = hoveredNapkinId !== null ? (napkinOpts.find((o) => o.id === hoveredNapkinId) ?? null) : null;
    const selectedNapkinOpt = napkinOpts.find((o) => o.id === napkinId) ?? null;
    const napkinPreviewOpt = hoveredNapkinOpt ?? selectedNapkinOpt;

    const hoveredTableclothOpt = hoveredTableclothId !== null ? (tableclothOpts.find((o) => o.id === hoveredTableclothId) ?? null) : null;
    const selectedTableclothOpt = tableclothOpts.find((o) => o.id === tableclothId) ?? null;
    const tableclothPreviewOpt = hoveredTableclothOpt ?? selectedTableclothOpt;

    return (
        <div className="builder-step">
            <div className="builder-step-header">
                <span className="eyebrow">שלב 5 / 7 · מפיות ומפות</span>
                <h2>מפיות ומפות</h2>
                <p>בחרו מפית ומפה לשולחנות האירוע.</p>
            </div>

            {/* Napkin section — own preview, own hover */}
            <OptionPreview selected={napkinPreviewOpt} isHoverPreview={hoveredNapkinId !== null} />
            <div className="card">
                <h3 style={{ marginBottom: "14px" }}>בחירת מפיות</h3>
                <p className="muted" style={{ fontSize: "0.88rem", marginBottom: "14px" }}>
                    בחרו את סגנון המפית.
                </p>
                <OptionGrid
                    options={napkinOpts}
                    selectedId={napkinId}
                    onSelect={(id) => setNapkinId(napkinId === id ? null : id)}
                    effectivePrice={effectivePrice}
                    onHover={setHoveredNapkinId}
                />
            </div>

            {/* Tablecloth section — own preview, own hover */}
            <OptionPreview selected={tableclothPreviewOpt} isHoverPreview={hoveredTableclothId !== null} />
            <div className="card">
                <h3 style={{ marginBottom: "14px" }}>בחירת מפות</h3>
                <p className="muted" style={{ fontSize: "0.88rem", marginBottom: "14px" }}>
                    בחרו את סגנון המפה.
                </p>
                <OptionGrid
                    options={tableclothOpts}
                    selectedId={tableclothId}
                    onSelect={(id) => setTableclothId(tableclothId === id ? null : id)}
                    effectivePrice={effectivePrice}
                    onHover={setHoveredTableclothId}
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
    venueImageUrl,
}: {
    options: PackageOptionResponse[];
    selectedId: number | null;
    onSelect: (id: number) => void;
    effectivePrice: (opt: PackageOptionResponse) => number;
    venueImageUrl?: string | null;
}) {
    const [hoveredId, setHoveredId] = useState<number | null>(null);
    const hoveredOpt = hoveredId !== null ? (options.find((o) => o.id === hoveredId) ?? null) : null;
    const selectedOpt = options.find((o) => o.id === selectedId) ?? null;
    const previewOpt = hoveredOpt ?? selectedOpt;

    return (
        <div className="builder-step">
            <div className="builder-step-header">
                <span className="eyebrow">שלב 6 / 7 · כיסא כלה</span>
                <h2>כיסא כלה</h2>
                <p>בחרו את עיצוב כיסא הכלה.</p>
            </div>
            <OptionPreview selected={previewOpt} isHoverPreview={hoveredId !== null} />
            <div className="card">
                <OptionGrid
                    options={options}
                    selectedId={selectedId}
                    onSelect={onSelect}
                    effectivePrice={effectivePrice}
                    onHover={setHoveredId}
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
    runningTotal,
    effectivePrice,
    basePackagePrice,
}: {
    eventDetails: EventDetails;
    venues: VenueResponse[];
    selectedOptions: PackageOptionResponse[];
    knightTableCount: number;
    runningTotal: number;
    effectivePrice: (opt: PackageOptionResponse) => number;
    basePackagePrice: number;
}) {
    const venue = venues.find((v) => v.id === eventDetails.venueId);
    const displayedBasePrice = Number.isFinite(basePackagePrice) && basePackagePrice > 0 ? basePackagePrice : 0;
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
                    <span>{formatILS(displayedBasePrice)}</span>
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
    const [regularTableSubs, setRegularTableSubs] = useState<TableDesignState>({ ...EMPTY_TABLE_DESIGN });
    const [knightTableSubs, setKnightTableSubs] = useState<TableDesignState>({ ...EMPTY_TABLE_DESIGN });
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

    function matchesContext(tc: string | null | undefined, required: "REGULAR" | "KNIGHT") {
        return !tc || tc === "BOTH" || tc === required;
    }

    const regularFrameOpts = useMemo(() => options.filter((o) => o.category === "TABLE_FRAME" && matchesContext(o.tableContext, "REGULAR")), [options]);
    const regularFlowerOpts = useMemo(() => options.filter((o) => o.category === "TABLE_FLOWER" && matchesContext(o.tableContext, "REGULAR")), [options]);
    const regularCandleOpts = useMemo(() => options.filter((o) => o.category === "TABLE_CANDLE" && matchesContext(o.tableContext, "REGULAR")), [options]);
    const knightFrameOpts = useMemo(() => options.filter((o) => o.category === "TABLE_FRAME" && matchesContext(o.tableContext, "KNIGHT")), [options]);
    const knightFlowerOpts = useMemo(() => options.filter((o) => o.category === "TABLE_FLOWER" && matchesContext(o.tableContext, "KNIGHT")), [options]);
    const knightCandleOpts = useMemo(() => options.filter((o) => o.category === "TABLE_CANDLE" && matchesContext(o.tableContext, "KNIGHT")), [options]);

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
        regularTableSubs.frameId,
        regularTableSubs.primaryFlowerId,
        regularTableSubs.secondaryFlowerId,
        ...regularTableSubs.candleHolderIds,
        ...(knightTableCount > 0 ? [
            knightTableSubs.frameId,
            knightTableSubs.primaryFlowerId,
            knightTableSubs.secondaryFlowerId,
            ...knightTableSubs.candleHolderIds,
        ] : []),
        napkinId,
        tableclothId,
        brideChairId,
    ].filter((id): id is number => id !== null), [
        chuppahId, selectedChuppahUpgradeIds, aisleId,
        regularTableSubs, knightTableSubs, knightTableCount,
        napkinId, tableclothId, brideChairId
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
                if (regularFrameOpts.length > 0 && !regularTableSubs.frameId) return "נא לבחור מסגרת לשולחן הרגיל";
                if (regularFlowerOpts.length > 0 && !regularTableSubs.primaryFlowerSize) return "נא לבחור סוג פרח (גדול/קטן) לשולחן הרגיל";
                if (regularFlowerOpts.length > 0 && !regularTableSubs.primaryFlowerId) return "נא לבחור פרח ראשי לשולחן הרגיל";
                if (regularTableSubs.candleMode === "SELECTED") {
                    if (regularTableSubs.candleHolderIds.length === 0) return "נא לבחור לפחות פמוט אחד לשולחן הרגיל";
                    if (regularTableSubs.candleHolderIds.length > 3) return "ניתן לבחור עד 3 פמוטים לשולחן הרגיל";
                }
                if (knightTableCount > 0) {
                    if (knightFrameOpts.length > 0 && !knightTableSubs.frameId) return "נא לבחור מסגרת לשולחן האביר";
                    if (knightFlowerOpts.length > 0 && !knightTableSubs.primaryFlowerSize) return "נא לבחור סוג פרח (גדול/קטן) לשולחן האביר";
                    if (knightFlowerOpts.length > 0 && !knightTableSubs.primaryFlowerId) return "נא לבחור פרח ראשי לשולחן האביר";
                    if (knightTableSubs.candleMode === "SELECTED") {
                        if (knightTableSubs.candleHolderIds.length === 0) return "נא לבחור לפחות פמוט אחד לשולחן האביר";
                        if (knightTableSubs.candleHolderIds.length > 3) return "ניתן לבחור עד 3 פמוטים לשולחן האביר";
                    }
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
        if (!regularTableSubs.frameId || !regularTableSubs.primaryFlowerSize || !regularTableSubs.primaryFlowerId) return;
        setSubmitting(true);
        setSubmitError(null);

        try {
            await submitPackageRequest({
                venueId: eventDetails.venueId,
                chuppahOptionId: chuppahId,
                chuppahUpgradeIds: selectedChuppahUpgradeIds,
                aisleOptionId: aisleId,
                regularTableDesign: {
                    frameOptionId: regularTableSubs.frameId,
                    primaryFlowerSize: regularTableSubs.primaryFlowerSize,
                    primaryFlowerOptionId: regularTableSubs.primaryFlowerId,
                    secondarySmallFlowerOptionId: regularTableSubs.secondaryFlowerId,
                    candleSelectionMode: regularTableSubs.candleMode,
                    candleHolderOptionIds: regularTableSubs.candleHolderIds,
                },
                knightTableCount: knightTableCount > 0 ? knightTableCount : 0,
                knightTableDesign: knightTableCount > 0 && knightTableSubs.frameId && knightTableSubs.primaryFlowerSize && knightTableSubs.primaryFlowerId
                    ? {
                        frameOptionId: knightTableSubs.frameId,
                        primaryFlowerSize: knightTableSubs.primaryFlowerSize,
                        primaryFlowerOptionId: knightTableSubs.primaryFlowerId,
                        secondarySmallFlowerOptionId: knightTableSubs.secondaryFlowerId,
                        candleSelectionMode: knightTableSubs.candleMode,
                        candleHolderOptionIds: knightTableSubs.candleHolderIds,
                    }
                    : null,
                napkinOptionId: napkinId,
                tableclothOptionId: tableclothId,
                brideChairOptionId: brideChairId,
                eventContactName: eventDetails.eventContactName,
                eventCustomerIdentityNumber: eventDetails.eventCustomerIdentityNumber,
                eventContactPhoneNumber: eventDetails.eventContactPhoneNumber,
                eventDate: eventDetails.eventDate,
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
                                setKnightTableCount(0);
                                setRegularTableSubs({ ...EMPTY_TABLE_DESIGN });
                                setKnightTableSubs({ ...EMPTY_TABLE_DESIGN });
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
                        regularFrameOpts={regularFrameOpts}
                        regularFlowerOpts={regularFlowerOpts}
                        regularCandleOpts={regularCandleOpts}
                        knightFrameOpts={knightFrameOpts}
                        knightFlowerOpts={knightFlowerOpts}
                        knightCandleOpts={knightCandleOpts}
                        knightTableCount={knightTableCount}
                        setKnightTableCount={setKnightTableCount}
                        regularSubs={regularTableSubs}
                        setRegularSubs={setRegularTableSubs}
                        knightSubs={knightTableSubs}
                        setKnightSubs={setKnightTableSubs}
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
                        venueImageUrl={selectedVenueImageUrl}
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
                    />
                );
        }
    }

    return (
        <main className="page builder-page">
            {renderStep()}

            {stepError && <div className="builder-step-error">{stepError}</div>}
            {submitError && step === 6 && (
                <div className="builder-step-error" style={{ maxWidth: "920px", margin: "8px auto 0" }}>
                    {submitError}
                </div>
            )}

            <div className="builder-navigation">
                {step > 0 ? (
                    <button
                        className="builder-nav-button secondary"
                        onClick={goBack}
                        disabled={submitting}
                    >
                        חזרה
                    </button>
                ) : (
                    <div />
                )}

                {step < 6 ? (
                    <button className="builder-nav-button primary" onClick={goNext}>
                        {step === 5 ? "לסיכום ושליחה" : "המשך"}
                    </button>
                ) : (
                    <button
                        className="builder-nav-button primary"
                        onClick={handleSubmit}
                        disabled={submitting}
                    >
                        {submitting ? "שולח..." : "שליחה לאישור מנהל האולם"}
                    </button>
                )}
            </div>
        </main>
    );
}
