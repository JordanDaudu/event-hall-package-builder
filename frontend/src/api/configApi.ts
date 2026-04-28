import { apiClient } from "./apiClient";
import type { PublicConfig } from "../types/api";

export async function getPublicConfig(): Promise<PublicConfig> {
    const response = await apiClient.get<PublicConfig>("/config");
    return response.data;
}