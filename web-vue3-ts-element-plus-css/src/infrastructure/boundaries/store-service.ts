import { JwtResponse } from "../models/login";
import { MenuInfo } from "../models/store";
export interface IStoreService {
    login(res: JwtResponse): Promise<void>;
    enterApp(menuList: MenuInfo[], dataBaseUserName: string): Promise<void>;
    logout(): Promise<void>;
    getToken(): string;
}