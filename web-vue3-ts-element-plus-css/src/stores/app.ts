import { defineStore } from "pinia";
import {
  type MenuInfo, type UserInfo, SystemConfig
} from "~/infrastructure/models/store";
import { type JwtResponse, } from "~/infrastructure/models/login";

export const useAppStore = defineStore("app", {
  state: () => ({
    isAuthenticated: false,
    usernameSave: "",
    passwordSave: "",
    remember: false,
    userInfo: {
      loggedIn: false,
      username: "",
      perName: "",
      token: "",
      id: 0,
      role: '',
    } as UserInfo,

    systemConfig: {
      menuList: [] as MenuInfo[],
      dataBaseUserName: '',
    } as SystemConfig,
  }),
  actions: {
    async login(res: JwtResponse, isAuthenticated: boolean): Promise<void> {
      this.isAuthenticated = isAuthenticated;
      this.userInfo = {
        loggedIn: true,
        username: res.username,
        perName: res.perName,
        token: res.token,
        id: res.id,
        role: res.role,
      };
    },
    logout() {
      this.userInfo = {
        loggedIn: false,
        username: "",
        perName: "",
        token: "",
        role: '',
        id: 0,
      };
      this.isAuthenticated = false;
    },
    async setNavi(menuList: MenuInfo[], dataBaseUserName: string): Promise<void> {
      this.systemConfig.menuList = menuList;
      this.systemConfig.dataBaseUserName = dataBaseUserName;
    },
  },
  persist: {
    storage: localStorage,
    // debug: true,
    // afterRestore: (context: PiniaPluginContext) => {
    //   console.log(context);
    // },
  },
});