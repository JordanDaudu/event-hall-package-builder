import { apiClient } from "./apiClient";
import type {
    ChuppahCompatibilityMap,
    ChuppahCompatibilityResponse,
    PackageOptionResponse,
} from "../types/api";

/** Customer: fetch the full compatibility map (chuppahId → allowed upgradeIds). */
export async function getChuppahCompatibilityMap(): Promise<ChuppahCompatibilityMap> {
    const res = await apiClient.get<ChuppahCompatibilityMap>("/chuppah-compatibility");
    return res.data;
}

/** Admin: list all CHUPPAH options (to pick from). */
export async function listChuppahs(): Promise<PackageOptionResponse[]> {
    const res = await apiClient.get<PackageOptionResponse[]>("/admin/chuppah-compatibility");
    return res.data;
}

/** Admin: get compatibility state for one chuppah. */
export async function getChuppahCompatibility(chuppahId: number): Promise<ChuppahCompatibilityResponse> {
    const res = await apiClient.get<ChuppahCompatibilityResponse>(`/admin/chuppah-compatibility/${chuppahId}`);
    return res.data;
}

/** Admin: set allowed upgrades for one chuppah. */
export async function setChuppahCompatibility(
    chuppahId: number,
    allowedUpgradeIds: number[]
): Promise<ChuppahCompatibilityResponse> {
    const res = await apiClient.put<ChuppahCompatibilityResponse>(
        `/admin/chuppah-compatibility/${chuppahId}`,
        { allowedUpgradeIds }
    );
    return res.data;
}
