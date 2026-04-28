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