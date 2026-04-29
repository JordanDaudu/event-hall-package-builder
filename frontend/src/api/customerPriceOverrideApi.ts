import { apiClient } from "./apiClient";
import type { PriceOverrideResponse } from "../types/api";

export async function listMyPriceOverrides(): Promise<PriceOverrideResponse[]> {
    const r = await apiClient.get<PriceOverrideResponse[]>("/customer/my-overrides");
    return r.data;
}
