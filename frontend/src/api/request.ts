import axios from "axios";
// import { useSessionStore } from "./session";

export const apiBase = "https://5010-140-238-151-117.ngrok-free.app/api";
// export const apiBase = "http://localhost:8000/api";

const axiosInstance = axios.create({
    baseURL: apiBase,
    headers: {
        "Content-Type": "application/json;charset=UTF-8",
    },
});

// Add the Authorization header if token is in localStorage
axiosInstance.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("authToken");
        if (token) {
            config.headers["Authorization"] = `Token ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

const request = {
    send: <_TResponse, TBody = undefined>(
        method: "get" | "post" | "patch" | "put" | "delete",
        url: string,
        body?: TBody,
        customHeaders?: Record<string, string>
    ) =>
        axiosInstance({
            method,
            url,
            data: body,
            headers: {
                ...axiosInstance.defaults.headers.common,
                "ngrok-skip-browser-warning": "true",
                ...customHeaders,
            },
        }).then((response) => response.data),

    get: <TResponse>(url: string, customHeaders?: Record<string, string>) =>
        request.send<TResponse>("get", url, undefined, customHeaders),

    post: <TBody, TResponse>(
        url: string,
        body?: TBody,
        customHeaders?: Record<string, string>
    ) => request.send<TResponse, TBody>("post", url, body, customHeaders),

    patch: <TBody, TResponse>(
        url: string,
        body?: TBody,
        customHeaders?: Record<string, string>
    ) => request.send<TResponse, TBody>("patch", url, body, customHeaders),

    put: <TBody, TResponse>(
        url: string,
        body?: TBody,
        customHeaders?: Record<string, string>
    ) => request.send<TResponse, TBody>("put", url, body, customHeaders),

    delete: <TResponse>(url: string, customHeaders?: Record<string, string>) =>
        request.send<TResponse>("delete", url, undefined, customHeaders),
};

export default request;