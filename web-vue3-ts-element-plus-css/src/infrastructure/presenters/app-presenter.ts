import { LoginRequest } from "~/infrastructure/models/login";
import { IStoreService } from "~/infrastructure/boundaries/store-service";
import { ID_STORE_SERVICE, ID_BASE_SERVICE, ID_LOGIN_SERVICE } from "~/types";
import { IBaseService } from "~/infrastructure/boundaries/base-service";
import { ILoginService } from "~/infrastructure/boundaries/login-service";
import { inject, injectable } from "inversify";
@injectable()
export class AppPresenter {
    constructor(
        @inject(ID_STORE_SERVICE) private readonly storeService: IStoreService,
        @inject(ID_BASE_SERVICE) private readonly baseService: IBaseService,
        @inject(ID_LOGIN_SERVICE) private readonly loginService: ILoginService

    ) { }
    public async enterApp(loginReq: LoginRequest): Promise<void> {
        const res = await this.loginService.login(loginReq);
        if (res) {
            await this.storeService.login(res);
            const menuList = await this.baseService.getMenuList();
            const dataBaseUserName = await this.baseService.getDataBaseUserName();
            await this.storeService.enterApp(menuList, dataBaseUserName);
        }
    }
}