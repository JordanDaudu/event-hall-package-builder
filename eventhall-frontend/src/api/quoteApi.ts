import { apiClient } from "./apiClient";
import type { CreateQuoteRequest, QuoteResponse } from "../types/api";

export async function createQuote(
    request: CreateQuoteRequest
): Promise<QuoteResponse> {
    const response = await apiClient.post<QuoteResponse>("/quotes", request);
    return response.data;
}

export async function getQuoteById(id: number): Promise<QuoteResponse> {
    const response = await apiClient.get<QuoteResponse>(`/quotes/${id}`);
    return response.data;
}