import { apiClient } from "./apiClient";
import type {
    CustomerResponse,
    CreateCustomerRequest,
    UpdateCustomerRequest,
    ChangePasswordRequest,
} from "../types/api";

const BASE = "/admin/customers";

export async function listCustomers(): Promise<CustomerResponse[]> {
    const r = await apiClient.get<CustomerResponse[]>(BASE);
    return r.data;
}

export async function createCustomer(req: CreateCustomerRequest): Promise<CustomerResponse> {
    const r = await apiClient.post<CustomerResponse>(BASE, req);
    return r.data;
}

export async function getCustomer(id: number): Promise<CustomerResponse> {
    const r = await apiClient.get<CustomerResponse>(`${BASE}/${id}`);
    return r.data;
}

export async function updateCustomer(id: number, req: UpdateCustomerRequest): Promise<CustomerResponse> {
    const r = await apiClient.put<CustomerResponse>(`${BASE}/${id}`, req);
    return r.data;
}

export async function changeCustomerPassword(id: number, req: ChangePasswordRequest): Promise<void> {
    await apiClient.put(`${BASE}/${id}/password`, req);
}

export async function setCustomerActive(id: number, active: boolean): Promise<CustomerResponse> {
    const r = await apiClient.put<CustomerResponse>(`${BASE}/${id}/active`, { active });
    return r.data;
}
