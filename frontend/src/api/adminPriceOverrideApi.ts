import { apiClient } from "./apiClient";
import type { PriceOverrideResponse, PriceOverrideRequest } from "../types/api";

function base(customerId: number) {
    return `/admin/customers/${customerId}/price-overrides`;
}

export async function listPriceOverrides(customerId: number): Promise<PriceOverrideResponse[]> {
    const r = await apiClient.get<PriceOverrideResponse[]>(base(customerId));
    return r.data;
}

export async function setPriceOverride(
    customerId: number,
    req: PriceOverrideRequest
): Promise<PriceOverrideResponse> {
    const r = await apiClient.post<PriceOverrideResponse>(base(customerId), req);
    return r.data;
}

export async function deletePriceOverride(customerId: number, optionId: number): Promise<void> {
    await apiClient.delete(`${base(customerId)}/${optionId}`);
}
