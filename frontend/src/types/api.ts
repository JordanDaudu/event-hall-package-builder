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