import { apiClient } from "./apiClient";
import type { PackageOptionResponse } from "../types/api";

export async function listActivePackageOptions(): Promise<PackageOptionResponse[]> {
    const response = await apiClient.get<PackageOptionResponse[]>("/package-options");
    return response.data;
}
