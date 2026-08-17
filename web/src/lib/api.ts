import axios from "axios";
import { getToken, clearToken } from "./auth";
import type { ApiResult } from "./types";

// 通过 next.config 的 rewrites，/api 在开发期代理到后端 8080
export const api = axios.create({ baseURL: "/api" });

api.interceptors.request.use((config) => {
  const t = getToken();
  if (t) config.headers.Authorization = `Bearer ${t}`;
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      clearToken();
      if (typeof window !== "undefined") window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);

// 统一解包 ApiResult：直接返回 data 字段；失败抛错（携带后端 message）
async function unwrap<T>(promise: Promise<{ data: ApiResult<T> }>): Promise<T> {
  const { data } = await promise;
  if (data.code !== 0) {
    throw new Error(data.message || "请求失败");
  }
  return data.data;
}

export const http = {
  get: <T>(url: string, params?: unknown) =>
    unwrap<T>(api.get(url, { params })),
  post: <T>(url: string, body?: unknown) => unwrap<T>(api.post(url, body)),
  put: <T>(url: string, body?: unknown) => unwrap<T>(api.put(url, body)),
  del: <T>(url: string) => unwrap<T>(api.delete(url)),
};
