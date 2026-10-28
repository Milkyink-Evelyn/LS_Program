import { DataResponse } from "~/infrastructure/models/request";
import { OptionItem } from "~/infrastructure/models/base";
import { TreeNode } from "~/domain/models/system";
export interface ISystemService {
    getRoleOptionItemList(): Promise<OptionItem[]>;
    getDictionaryOptionItemList(code: string): Promise<OptionItem[]>;
    getMenuTreeNodeList(): Promise<TreeNode[]>;
    menuDelete(id: number): Promise<DataResponse>;
    menuSave(editType: number, node: TreeNode): Promise<DataResponse>;
    getDictionaryTreeNodeList(): Promise<TreeNode[]>;
    dictionaryDelete(id: number): Promise<DataResponse>;
    dictionarySave(node: TreeNode): Promise<DataResponse>;
}