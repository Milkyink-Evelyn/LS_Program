import { MainPageData } from "~/infrastructure/models/base";
import { ID_BASE_SERVICE } from "~/types";
import { IBaseService } from "~/infrastructure/boundaries/base-service";
import { inject, injectable } from "inversify";
@injectable()
export class MainPagePresenter {
    constructor(
        @inject(ID_BASE_SERVICE) private readonly service: IBaseService
    ) { }

    public async mainPageInit(): Promise<MainPageData> {
        let data = {} as MainPageData;
        await this.getMainPageData(data);
        return data;
    }
    public async getMainPageData(data: MainPageData): Promise<void> {
        const res = await this.service.getMainPageData();
        data.onlineUser = res.data.onlineUser;
        data.userTypeList = res.data.userTypeList;
        data.requestData = res.data.requestData;
        data.operateData = res.data.operateData;

    }

}