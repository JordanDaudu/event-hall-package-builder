import { apiClient } from "./apiClient";
import type { Upgrade } from "../types/api";

export type CreateUpgradeRequest = {
    name: string;
    description: string;
    category: string;
    price: number;
};

export type UpdateUpgradeRequest = {
    name: string;
    description: string;
    category: string;
    price: number;
    active: boolean;
};

export async function getAllAdminUpgrades(): Promise<Upgrade[]> {
    const response = await apiClient.get<Upgrade[]>("/admin/upgrades");
    return response.data;
}

export async function createUpgrade(
    request: CreateUpgradeRequest
): Promise<Upgrade> {
    const response = await apiClient.post<Upgrade>("/admin/upgrades", request);
    return response.data;
}

export async function updateUpgrade(
    id: number,
    request: UpdateUpgradeRequest
): Promise<Upgrade> {
    const response = await apiClient.put<Upgrade>(
        `/admin/upgrades/${id}`,
        request
    );

    return response.data;
}