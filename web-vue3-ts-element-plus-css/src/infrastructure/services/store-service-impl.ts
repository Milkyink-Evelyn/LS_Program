import { IStoreService } from "../boundaries/store-service";
import { JwtResponse } from "../models/login";
import { MenuInfo } from "../models/store";
import { DataResponse } from "../models/request";
import { useAppStore } from "~/stores/app";
import router from "~/router";
export class StoreServiceImpl implements IStoreService {
    public async login(res: JwtResponse): Promise<void> {
        const store = useAppStore();
        await store.login(res, true);
    }
    public async enterApp(menuList: MenuInfo[], dataBaseUserName: string): Promise<void> {
        const store = useAppStore();
        await store.setNavi(menuList, dataBaseUserName);
        router.push({ path: "/mainPage" });
    }
    public async logout(): Promise<void> {
        const store = useAppStore();
        store.logout();
    }
    public getToken(): string {
        return useAppStore().userInfo.token;
    }
}