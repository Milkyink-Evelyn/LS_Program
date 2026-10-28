import { MenuManageData, TreeNode } from "~/domain/models/system";
import { ID_SYSTEM_SERVICE, ID_MESSAGE_SERVICE } from "~/types";
import { IMessageService } from "~/infrastructure/boundaries/message-service";
import { ISystemService } from "~/domain/boundaries/system-service";
import { inject, injectable } from "inversify";
@injectable()
export class MenuManagePresenter {
    constructor(
        @inject(ID_MESSAGE_SERVICE) private readonly messageService: IMessageService,
        @inject(ID_SYSTEM_SERVICE) private readonly service: ISystemService
    ) { }
    public async menuManageInit(): Promise<MenuManageData> {
        let data = {} as MenuManageData;
        data.roleList = await this.service.getRoleOptionItemList()
        data.nodes = await this.service.getMenuTreeNodeList()
        return data;
    }
    public async confirm(data: MenuManageData, editedNode: TreeNode): Promise<void> {
        let s = ''
        if (data.roleAdmin) {
            if (s == '') s = '1'
            else s = s + ',1'
        }
        if (data.roleStudent) {
            if (s == '') s = '2'
            else s = s + ',2'
        }
        if (data.roleAdmin) {
            if (s == '') s = '3'
            else s = s + ',3'
        }
        const res = await this.service.menuSave(data.editType, editedNode)
        if (res.code == 0) {
            this.messageService.success('保存成功')
            data.nodes = await this.service.getMenuTreeNodeList()
        } else {
            this.messageService.error(res.msg)
        }
    };
    public async deleteItem(data: MenuManageData, node: TreeNode): Promise<void> {
        data.currentNode = node
        const result = await this.messageService.confirm('确认删除吗?')
        if (!result) {
            return
        }
        const res = await this.service.menuDelete(data.currentNode.id)
        if (res.code == 0) {
            this.messageService.success('删除成功')
            data.nodes = await this.service.getMenuTreeNodeList()
        } else {
            this.messageService.error(res.msg)
        }
    };

}