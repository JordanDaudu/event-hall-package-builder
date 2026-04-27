import { apiClient } from "./apiClient";
import type { Upgrade } from "../types/api";

export async function getUpgrades(): Promise<Upgrade[]> {
    const response = await apiClient.get<Upgrade[]>("/upgrades");
    return response.data;
}