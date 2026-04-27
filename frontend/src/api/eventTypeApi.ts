import { apiClient } from "./apiClient";
import type { EventType } from "../types/api";

export async function getEventTypes(): Promise<EventType[]> {
    const response = await apiClient.get<EventType[]>("/event-types");
    return response.data;
}