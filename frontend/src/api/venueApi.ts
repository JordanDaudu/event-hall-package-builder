import { apiClient } from "./apiClient";
import type { VenueResponse } from "../types/api";

export async function listActiveVenues(): Promise<VenueResponse[]> {
    const response = await apiClient.get<VenueResponse[]>("/venues");
    return response.data;
}
