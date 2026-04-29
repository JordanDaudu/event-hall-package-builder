import { apiClient } from "./apiClient";
import type { VenueResponse, CreateVenueRequest, UpdateVenueRequest } from "../types/api";

const BASE = "/admin/venues";

export async function listAllVenues(): Promise<VenueResponse[]> {
    const r = await apiClient.get<VenueResponse[]>(BASE);
    return r.data;
}

export async function createVenue(req: CreateVenueRequest): Promise<VenueResponse> {
    const r = await apiClient.post<VenueResponse>(BASE, req);
    return r.data;
}

export async function getVenueById(id: number): Promise<VenueResponse> {
    const r = await apiClient.get<VenueResponse>(`${BASE}/${id}`);
    return r.data;
}

export async function updateVenue(id: number, req: UpdateVenueRequest): Promise<VenueResponse> {
    const r = await apiClient.put<VenueResponse>(`${BASE}/${id}`, req);
    return r.data;
}

export async function setVenueActive(id: number, active: boolean): Promise<VenueResponse> {
    const r = await apiClient.put<VenueResponse>(`${BASE}/${id}/active`, { active });
    return r.data;
}

export async function deleteVenue(id: number): Promise<void> {
    await apiClient.delete(`${BASE}/${id}`);
}
