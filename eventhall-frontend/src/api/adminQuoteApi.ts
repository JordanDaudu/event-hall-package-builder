import { apiClient } from "./apiClient";
import type { QuoteResponse } from "../types/api";

export async function getAllQuotes(
    status?: QuoteResponse["status"]
): Promise<QuoteResponse[]> {
    const url = status ? `/admin/quotes?status=${status}` : "/admin/quotes";

    const response = await apiClient.get<QuoteResponse[]>(url);
    return response.data;
}

export async function updateQuoteStatus(
    id: number,
    status: QuoteResponse["status"]
): Promise<QuoteResponse> {
    const response = await apiClient.put<QuoteResponse>(
        `/admin/quotes/${id}/status`,
        { status }
    );

    return response.data;
}