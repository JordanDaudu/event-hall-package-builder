// ─── Legacy types (existing builder — kept for compatibility) ───────────────

export type EventType = {
    id: number;
    name: string;
    basePrice: number;
};

export type Upgrade = {
    id: number;
    name: string;
    description: string;
    category: string;
    price: number;
    active: boolean;
};

export type CreateQuoteRequest = {
    eventTypeId: number;
    guestCount: number;
    upgradeIds: number[];
    customerName: string;
    customerEmail: string;
    customerPhoneNumber: string;
};

export type QuoteResponse = {
    id: number;
    customerName: string;
    customerEmail: string;
    customerPhoneNumber: string;
    eventTypeName: string;
    guestCount: number;
    upgrades: string[];
    totalPrice: number;
    status: "NEW" | "CONTACTED" | "APPROVED" | "REJECTED";
};

export type EventTypeRevenueDto = {
    eventTypeName: string;
    revenue: number;
};

export type UpgradeUsageDto = {
    upgradeName: string;
    count: number;
};

export type AdminDashboardResponse = {
    totalQuotes: number;
    approvedQuotes: number;
    totalRevenue: number;
    revenueByMonth: Record<string, number>;
    revenueByEventType: EventTypeRevenueDto[];
    topUpgrades: UpgradeUsageDto[];
};

export type PublicConfig = {
    name: string;
    contactEmail: string;
    contactPhone: string;
};

// ─── Auth ────────────────────────────────────────────────────────────────────

export type UserRole = "ADMIN" | "CUSTOMER";

export interface AuthUser {
    id: number;
    email: string;
    fullName: string;
    role: UserRole;
    customerIdentityNumber?: string | null;
    phoneNumber?: string | null;
    basePackagePrice?: number | null;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginApiResponse {
    token: string;
    expiresInMinutes: number;
    userId: number;
    fullName: string;
    email: string;
    role: UserRole;
}

export interface MeResponse {
    id: number;
    fullName: string;
    email: string;
    customerIdentityNumber?: string | null;
    phoneNumber?: string | null;
    role: UserRole;
    active: boolean;
    basePackagePrice?: number | null;
}

// ─── Venues ──────────────────────────────────────────────────────────────────

export interface VenueResponse {
    id: number;
    nameHe: string;
    nameEn?: string | null;
    descriptionHe?: string | null;
    imageUrl?: string | null;
    active: boolean;
    sortOrder: number;
    priceModifier?: number | null;
}

// ─── Package Options ─────────────────────────────────────────────────────────

export type PackageOptionCategory =
    | "CATERING"
    | "DECORATION"
    | "MUSIC"
    | "PHOTOGRAPHY"
    | "EXTRAS"
    | "CHUPPAH"
    | "CHUPPAH_UPGRADE"
    | "AISLE"
    | "TABLE_FRAME"
    | "TABLE_FLOWER"
    | "TABLE_CANDLE"
    | "NAPKIN"
    | "TABLECLOTH"
    | "BRIDE_CHAIR";

export type VisualBehavior = "REPLACE_IMAGE" | "OVERLAY_IMAGE" | "NO_VISUAL";

export type TableContextValue = "REGULAR" | "KNIGHT" | "BOTH";
export type FlowerSizeValue = "LARGE" | "SMALL";
export type CandleSelectionMode = "RANDOM" | "SELECTED";

export interface PackageOptionResponse {
    id: number;
    nameHe: string;
    nameEn?: string | null;
    category: PackageOptionCategory;
    globalPrice: number;
    active: boolean;
    sortOrder: number;
    imageUrl?: string | null;
    visualBehavior?: VisualBehavior | null;
    overlayTop?: string | null;
    overlayLeft?: string | null;
    overlayWidth?: string | null;
    overlayZIndex?: number | null;
    /** REGULAR | KNIGHT | BOTH (null = BOTH). Only relevant for table categories. */
    tableContext?: TableContextValue | null;
    /** LARGE | SMALL (null = treated as LARGE). Only relevant for TABLE_FLOWER. */
    flowerSize?: FlowerSizeValue | null;
}

export type ChuppahCompatibilityMap = Record<number, number[]>;

// ─── Package Requests ────────────────────────────────────────────────────────

export type RequestStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface TableDesignRequest {
    frameOptionId: number;
    primaryFlowerSize: FlowerSizeValue | null;
    primaryFlowerOptionId: number;
    secondarySmallFlowerOptionId?: number | null;
    candleSelectionMode: CandleSelectionMode;
    candleHolderOptionIds: number[];
}

export interface SubmitRequestRequest {
    venueId: number;
    chuppahOptionId: number;
    chuppahUpgradeIds: number[];
    aisleOptionId?: number | null;
    regularTableDesign: TableDesignRequest;
    knightTableCount?: number | null;
    knightTableDesign?: TableDesignRequest | null;
    napkinOptionId?: number | null;
    tableclothOptionId?: number | null;
    brideChairOptionId?: number | null;
    eventCustomerIdentityNumber: string;
    eventContactName: string;
    eventContactPhoneNumber: string;
    eventDate: string;
}

export interface PackageRequestItemResponse {
    id: number;
    packageOptionId: number;
    optionNameSnapshot: string;
    globalPriceSnapshot: number;
    customerOverridePriceSnapshot?: number | null;
    finalPrice: number;
    hasCustomerOverride: boolean;
    category: PackageOptionCategory;
    /** REGULAR | KNIGHT for table items; null for non-table items. */
    tableContext?: string | null;
}

export interface PackageRequestSummaryResponse {
    id: number;
    status: RequestStatus;
    totalPrice: number;
    eventDate: string;
    venueNameSnapshot: string;
    eventContactName: string;
    submittedAt: string;
    createdAt: string;
}

// ─── Admin — Customer Management ─────────────────────────────────────────────

export interface CustomerResponse {
    id: number;
    fullName: string;
    email: string;
    customerIdentityNumber?: string | null;
    phoneNumber?: string | null;
    active: boolean;
    basePackagePrice: number;
    createdAt: string;
    updatedAt: string;
}

export interface CreateCustomerRequest {
    fullName: string;
    email: string;
    customerIdentityNumber?: string;
    phoneNumber?: string;
    password: string;
    basePackagePrice: number;
}

export interface UpdateCustomerRequest {
    fullName: string;
    customerIdentityNumber?: string;
    phoneNumber?: string;
    basePackagePrice: number;
}

export interface ChangePasswordRequest {
    newPassword: string;
}

// ─── Admin — Price Overrides ──────────────────────────────────────────────────

export interface PriceOverrideResponse {
    id: number;
    customerId: number;
    optionId: number;
    customPrice: number;
    createdAt: string;
    updatedAt: string;
}

export interface PriceOverrideRequest {
    optionId: number;
    customPrice: number;
}

// ─── Admin — Venue Management ─────────────────────────────────────────────────

export interface CreateVenueRequest {
    nameHe: string;
    nameEn?: string;
    descriptionHe?: string;
    imageUrl?: string;
    sortOrder: number;
}

export type UpdateVenueRequest = CreateVenueRequest;

// ─── Admin — Package Option Management ───────────────────────────────────────

export interface CreatePackageOptionRequest {
    nameHe: string;
    nameEn?: string;
    category: PackageOptionCategory;
    globalPrice: number;
    sortOrder?: number;
    imageUrl?: string;
    visualBehavior?: VisualBehavior;
    overlayTop?: string;
    overlayLeft?: string;
    overlayWidth?: string;
    overlayZIndex?: number;
    tableContext?: TableContextValue;
    flowerSize?: FlowerSizeValue;
}

export interface UpdatePackageOptionRequest {
    nameHe?: string;
    nameEn?: string;
    category?: PackageOptionCategory;
    globalPrice?: number;
    sortOrder?: number;
    imageUrl?: string;
    visualBehavior?: VisualBehavior;
    overlayTop?: string;
    overlayLeft?: string;
    overlayWidth?: string;
    overlayZIndex?: number;
    tableContext?: TableContextValue;
    flowerSize?: FlowerSizeValue;
}

// ─── Chuppah Compatibility ────────────────────────────────────────────────────

export interface ChuppahCompatibilityResponse {
    chuppah: PackageOptionResponse;
    availableUpgrades: PackageOptionResponse[];
    allowedUpgradeIds: number[];
}

// ─── Package Requests (admin) ─────────────────────────────────────────────────

export interface PackageRequestDetailResponse {
    id: number;
    customerId: number;
    customerEmail: string;
    customerFullName: string;
    status: RequestStatus;
    eventCustomerIdentityNumber: string;
    eventContactName: string;
    eventContactPhoneNumber: string;
    eventDate: string;
    venueNameSnapshot: string;
    basePackagePriceSnapshot: number;
    totalPrice: number;
    summaryNotes?: string | null;
    knightTableCount?: number | null;
    submittedAt: string;
    approvedAt?: string | null;
    rejectedAt?: string | null;
    createdAt: string;
    items: PackageRequestItemResponse[];
}
