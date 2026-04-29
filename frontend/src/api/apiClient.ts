import axios from "axios";

/**
 * Central Axios instance.
 *
 * Base URL is /api so all calls use the Vite dev-proxy.
 * Token and 401 handling are registered by AuthContext at mount time
 * to avoid circular module dependencies.
 */
export const apiClient = axios.create({
    baseURL: import.meta.env.VITE_API_URL ?? "/api",
});

let _token: string | null = null;
let _onUnauthorized: (() => void) | null = null;

export function setApiToken(token: string | null): void {
    _token = token;
}

export function setUnauthorizedHandler(handler: () => void): void {
    _onUnauthorized = handler;
}

apiClient.interceptors.request.use((config) => {
    if (_token) {
        config.headers.Authorization = `Bearer ${_token}`;
    }
    return config;
});

apiClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401 && _onUnauthorized) {
            _onUnauthorized();
        }
        return Promise.reject(error);
    }
);
