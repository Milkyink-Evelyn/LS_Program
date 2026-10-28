import { DataResponse } from "~/infrastructure/models/request";
import { MenuInfo } from "~/infrastructure/models/store";
import { OptionItem } from "~/infrastructure/models/base";
import { IRequestService } from "~/infrastructure/boundaries/request-service";
import { IBaseService } from "~/infrastructure/boundaries/base-service";
import { ID_REQUEST_SERVICE, } from "~/types";
import { inject, injectable } from "inversify";
@injectable()
export class BaseServiceImpl implements IBaseService {
  constructor(
    @inject(ID_REQUEST_SERVICE) private readonly requestService: IRequestService
  ) { }
  public async getMenuList(): Promise<MenuInfo[]> {
    const res = await this.requestService.generalRequest("/api/base/getMenuList", null);
    return res.data as MenuInfo[];
  }
  public async getDataBaseUserName(): Promise<string> {
    const res = await this.requestService.generalRequest("/api/base/getDataBaseUserName", null);
    return res.data as string;
  }
  public async getMainPageData(): Promise<DataResponse> {
    const res = await this.requestService.generalRequest("/api/statistics/getMainPageData", null);
    return res as DataResponse;
  }
  public async getDictionaryOptionItemList(code: string): Promise<OptionItem[]> {
    const res = await this.requestService.generalRequest("/api/base/getDictionaryOptionItemList", { code: code, });
    return res.itemList as OptionItem[];
  }

}
