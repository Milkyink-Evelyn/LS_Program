import axios, { type AxiosError, type AxiosResponse } from "axios";
import { ElMessage } from "element-plus";
import { useAppStore } from "~/stores/app";
import type { ApiResponse } from "~/types";

const service = axios.create({
  timeout: 15000,
});

/** 请求没有拿到响应：断网、超时、后端未启动等。 */
function networkErrorText(error: AxiosError) {
  if (error.code === "ECONNABORTED" || error.code === "ETIMEDOUT") {
    return "请求超时，请稍后重试";
  }
  return "网络异常，请确认后端服务已启动";
}

function httpErrorText(status?: number) {
  if (status === 404) return "接口不存在";
  if (status && status >= 500) return "服务端异常，请稍后重试";
  return "操作失败，请稍后重试";
}

service.interceptors.request.use((config) => {
  const store = useAppStore();
  if (store.token) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${store.token}`;
  }
  return config;
});

service.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown> | unknown;

    // 登录接口是裸对象；新接口是 { code, msg, data }
    if (body && typeof body === "object" && "code" in body) {
      const res = body as ApiResponse<unknown>;
      if (res.code !== 0) {
        ElMessage.error(res.msg || "操作失败");
        return Promise.reject(new Error(res.msg || "操作失败"));
      }
      return res.data as unknown as AxiosResponse;
    }
    return body as unknown as AxiosResponse;
  },
  (error: AxiosError<{ msg?: string }>) => {
    const status = error.response?.status;
    const url: string = error.config?.url || "";
    const serverMsg = error.response?.data?.msg;

    // 登录失败：只有 401 才是账号密码错误，网络或服务端问题单独提示
    if (url.includes("/auth/login")) {
      if (status === 401) {
        ElMessage.error("用户名或密码错误");
      } else if (!error.response) {
        ElMessage.error(networkErrorText(error));
      } else if (status && status >= 500) {
        // 开发代理在后端未启动时也会返回 500
        ElMessage.error("登录服务暂不可用，请确认后端服务已启动或稍后重试");
      } else {
        ElMessage.error(serverMsg || "登录失败，请稍后重试");
      }
      return Promise.reject(error);
    }

    if (url.includes("/auth/")) {
      ElMessage.error(serverMsg || "操作失败，请稍后重试");
      return Promise.reject(error);
    }

    // 403 是已登录但无权限，不能清除会话
    if (status === 401) {
      useAppStore().logout();
      ElMessage.error(serverMsg || "登录状态已失效，请重新登录");
      window.location.hash = "#/login";
      return Promise.reject(error);
    }

    if (status === 403) {
      ElMessage.error(serverMsg || "无权限执行该操作");
      return Promise.reject(error);
    }

    if (!error.response) {
      ElMessage.error(networkErrorText(error));
      return Promise.reject(error);
    }

    ElMessage.error(serverMsg || httpErrorText(status));
    return Promise.reject(error);
  }
);

const http = {
  get<T>(url: string, params?: Record<string, unknown>) {
    return service.get(url, { params }) as unknown as Promise<T>;
  },
  post<T>(url: string, data?: unknown) {
    return service.post(url, data) as unknown as Promise<T>;
  },
  put<T>(url: string, data?: unknown) {
    return service.put(url, data) as unknown as Promise<T>;
  },
  delete<T>(url: string) {
    return service.delete(url) as unknown as Promise<T>;
  },
};

export default http;

