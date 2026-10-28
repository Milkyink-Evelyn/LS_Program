import { DataResponse } from "~/infrastructure/models/request";
import { OptionItem } from "~/infrastructure/models/base";
import { TreeNode } from "~/domain/models/system";
import { IRequestService } from "~/infrastructure/boundaries/request-service";
import { ISystemService } from "~/domain/boundaries/system-service";
import { ID_REQUEST_SERVICE, } from "~/types";
import { inject, injectable } from "inversify";
@injectable()
export class SystemServiceImpl implements ISystemService {
    constructor(
        @inject(ID_REQUEST_SERVICE) private readonly requestService: IRequestService
    ) { }
    public async getRoleOptionItemList(): Promise<OptionItem[]> {
        const res = await this.requestService.generalRequest("/api/base/getRoleOptionItemList", {});
        return res.itemList as OptionItem[];
    }
    //获取字典选择项列表后台数据请求方法
    public async getDictionaryOptionItemList(
        code: string
    ): Promise<OptionItem[]> {
        const res = await this.requestService.generalRequest("/api/base/getDictionaryOptionItemList", {
            code: code,
        });
        return res.itemList as OptionItem[];
    }
    //获取菜单列表数据后台数据请求方法
    public async getMenuTreeNodeList(): Promise<TreeNode[]> {
        const res = await this.requestService.generalRequest("/api/base/getMenuTreeNodeList", {});
        return res as TreeNode[];
    }
    //菜单删除后台数据请求方法
    public async menuDelete(id: number): Promise<DataResponse> {
        const res = await this.requestService.generalRequest("/api/base/menuDelete", {
            id: id,
        });
        return res as DataResponse;
    }
    //菜单保存后台数据请求方法
    public async menuSave(
        editType: number,
        node: TreeNode
    ): Promise<DataResponse> {
        const res = await this.requestService.generalRequest("/api/base/menuSave", {
            editType: editType,
            node: node,
        });
        return res as DataResponse;
    }
    //获取字典树列表数据后台数据请求方法
    public async getDictionaryTreeNodeList(): Promise<TreeNode[]> {
        const res = await this.requestService.generalRequest("/api/base/getDictionaryTreeNodeList", {});
        return res as TreeNode[];
    }
    //字典删除后台数据请求方法
    public async dictionaryDelete(id: number): Promise<DataResponse> {
        const res = await this.requestService.generalRequest("/api/base/dictionaryDelete", {
            id: id,
        });
        return res as DataResponse;
    }
    //字典保存后台数据请求方法
    public async dictionarySave(node: TreeNode): Promise<DataResponse> {
        const res = await this.requestService.generalRequest("/api/base/dictionarySave", node);
        return res as DataResponse;
    }

}