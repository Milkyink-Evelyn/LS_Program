import { DataResponse } from "~/infrastructure/models/request";
import { MenuInfo } from "~/infrastructure/models/store";
import { OptionItem } from "~/infrastructure/models/base";
export interface IBaseService {
    getMenuList(): Promise<MenuInfo[]>;
    getDataBaseUserName(): Promise<string>;
    getMainPageData(): Promise<DataResponse>;
    getDictionaryOptionItemList(code: string): Promise<OptionItem[]>;
}