import { apiClient } from "./apiClient";
import type {
    PackageOptionResponse,
    CreatePackageOptionRequest,
    UpdatePackageOptionRequest,
} from "../types/api";

const BASE = "/admin/package-options";

export async function listAllPackageOptions(): Promise<PackageOptionResponse[]> {
    const r = await apiClient.get<PackageOptionResponse[]>(BASE);
    return r.data;
}

export async function createPackageOption(req: CreatePackageOptionRequest): Promise<PackageOptionResponse> {
    const r = await apiClient.post<PackageOptionResponse>(BASE, req);
    return r.data;
}

export async function updatePackageOption(id: number, req: UpdatePackageOptionRequest): Promise<PackageOptionResponse> {
    const r = await apiClient.put<PackageOptionResponse>(`${BASE}/${id}`, req);
    return r.data;
}

export async function setPackageOptionActive(id: number, active: boolean): Promise<PackageOptionResponse> {
    const r = await apiClient.patch<PackageOptionResponse>(`${BASE}/${id}/active`, { active });
    return r.data;
}

export async function deletePackageOption(id: number): Promise<void> {
    await apiClient.delete(`${BASE}/${id}`);
}
