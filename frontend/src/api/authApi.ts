import { apiClient } from "./apiClient";
import type { LoginApiResponse, LoginRequest, MeResponse } from "../types/api";

export async function loginApi(req: LoginRequest): Promise<LoginApiResponse> {
    const response = await apiClient.post<LoginApiResponse>("/auth/login", req);
    return response.data;
}

export async function getMe(): Promise<MeResponse> {
    const response = await apiClient.get<MeResponse>("/auth/me");
    return response.data;
}
