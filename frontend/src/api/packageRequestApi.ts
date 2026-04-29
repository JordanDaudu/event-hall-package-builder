import { apiClient } from "./apiClient";
import type {
    PackageRequestDetailResponse,
    PackageRequestSummaryResponse,
    SubmitRequestRequest,
} from "../types/api";

export async function submitPackageRequest(
    req: SubmitRequestRequest
): Promise<PackageRequestDetailResponse> {
    const response = await apiClient.post<PackageRequestDetailResponse>(
        "/customer/requests",
        req
    );
    return response.data;
}

export async function listOwnRequests(): Promise<PackageRequestSummaryResponse[]> {
    const response = await apiClient.get<PackageRequestSummaryResponse[]>(
        "/customer/requests"
    );
    return response.data;
}

export async function getOwnRequestById(id: number): Promise<PackageRequestDetailResponse> {
    const response = await apiClient.get<PackageRequestDetailResponse>(
        `/customer/requests/${id}`
    );
    return response.data;
}
