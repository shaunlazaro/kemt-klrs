import axios from "axios";
// import { useSessionStore } from "./session";

// const apiBase = "http://140.238.151.117:8000/api";
const apiBase = "http://127.0.0.1:8000/api";

const axiosInstance = axios.create({
    baseURL: apiBase,
    headers: {
        "Content-Type": "application/json;charset=UTF-8",
    },
});

// TODO: Auth
// axiosInstance.interceptors.request.use(
//     (config) => {
//         const token = useSessionStore.getState().user?.accessToken;

//         if (token) {
//             config.headers["Authorization"] = `Bearer ${token}`;
//         }

//         return config;
//     },
//     (error) => Promise.reject(error),
// );

const request = {
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    send: <_TResponse, TBody = undefined>(
        method: "get" | "post" | "patch" | "put" | "delete",
        url: string,
        body?: TBody,
        customHeaders?: Record<string, string>,
    ) =>
        axiosInstance({
            method,
            url,
            data: body,
            headers: { ...axiosInstance.defaults.headers.common, ...customHeaders },
        }).then((response) => response.data),

    get: <TResponse>(url: string, customHeaders?: Record<string, string>) =>
        request.send<TResponse>("get", url, undefined, customHeaders),

    post: <TBody, TResponse>(
        url: string,
        body?: TBody,
        customHeaders?: Record<string, string>,
    ) => request.send<TResponse, TBody>("post", url, body, customHeaders),

    patch: <TBody, TResponse>(
        url: string,
        body?: TBody,
        customHeaders?: Record<string, string>,
    ) => request.send<TResponse, TBody>("patch", url, body, customHeaders),

    put: <TBody, TResponse>(
        url: string,
        body?: TBody,
        customHeaders?: Record<string, string>,
    ) => request.send<TResponse, TBody>("put", url, body, customHeaders),

    delete: <TResponse>(url: string, customHeaders?: Record<string, string>) =>
        request.send<TResponse>("delete", url, undefined, customHeaders),
};

export default request;
