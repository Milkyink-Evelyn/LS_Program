import { defineStore } from "pinia";
import type { CurrentUser, LoginResponse, Role } from "~/types";

export const useAppStore = defineStore("app", {
  state: () => ({
    token: "",
    user: null as CurrentUser | null,
  }),

  getters: {
    role: (state): Role | "" => state.user?.role ?? "",
    isLoggedIn: (state) => Boolean(state.token && state.user),
  },

  actions: {
    setLogin(res: LoginResponse) {
      this.token = res.token;
      // 模板后端可能返回 ROLE_ADMIN，这里做一次防御性归一化
      const role = String(res.role).replace(/^ROLE_/, "") as Role;
      this.user = {
        id: res.id,
        username: res.username,
        realName: res.perName || res.username,
        // 登录响应不包含 email，个人中心会调用 /api/users/me 补齐
        email: null,
        role,
      };
    },

    updateUser(patch: Partial<Pick<CurrentUser, "realName" | "email">>) {
      if (this.user) {
        this.user = { ...this.user, ...patch };
      }
    },

    logout() {
      this.token = "";
      this.user = null;
    },
  },

  persist: {
    storage: localStorage,
    key: "hospital-app",
  },
});
