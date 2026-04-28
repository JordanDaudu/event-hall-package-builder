import { apiClient } from "./apiClient";
import type { AdminDashboardResponse } from "../types/api";

export async function getAdminDashboard(
    year: number
): Promise<AdminDashboardResponse> {
    const response = await apiClient.get<AdminDashboardResponse>(
        `/admin/dashboard?year=${year}`
    );

    return response.data;
}