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
    | "EXTRAS";

export interface PackageOptionResponse {
    id: number;
    nameHe: string;
    nameEn?: string | null;
    category: PackageOptionCategory;
    globalPrice: number;
    active: boolean;
    sortOrder: number;
}

// ─── Package Requests ────────────────────────────────────────────────────────

export type RequestStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface SubmitRequestRequest {
    venueId: number;
    optionIds: number[];
    eventCustomerIdentityNumber: string;
    eventContactName: string;
    eventContactPhoneNumber: string;
    eventDate: string;
    knightTableCount?: number | null;
}

export interface PackageRequestItemResponse {
    id: number;
    packageOptionId: number;
    optionNameSnapshot: string;
    globalPriceSnapshot: number;
    customerOverridePriceSnapshot?: number | null;
    finalPrice: number;
    hasCustomerOverride: boolean;
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
}

export interface UpdatePackageOptionRequest {
    nameHe?: string;
    nameEn?: string;
    category?: PackageOptionCategory;
    globalPrice?: number;
    sortOrder?: number;
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
