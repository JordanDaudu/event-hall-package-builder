import { apiClient } from "./apiClient";
import type {
    PackageRequestDetailResponse,
    PackageRequestSummaryResponse,
    RequestStatus,
} from "../types/api";

export async function listAdminRequests(
    status?: RequestStatus
): Promise<PackageRequestSummaryResponse[]> {
    const params = status ? { status } : {};
    const response = await apiClient.get<PackageRequestSummaryResponse[]>(
        "/admin/requests",
        { params }
    );
    return response.data;
}

export async function getAdminRequestById(
    id: number
): Promise<PackageRequestDetailResponse> {
    const response = await apiClient.get<PackageRequestDetailResponse>(
        `/admin/requests/${id}`
    );
    return response.data;
}

export async function updateAdminRequestStatus(
    id: number,
    status: Exclude<RequestStatus, "PENDING">,
    summaryNotes?: string
): Promise<PackageRequestDetailResponse> {
    const response = await apiClient.patch<PackageRequestDetailResponse>(
        `/admin/requests/${id}/status`,
        { status, summaryNotes: summaryNotes ?? null }
    );
    return response.data;
}
