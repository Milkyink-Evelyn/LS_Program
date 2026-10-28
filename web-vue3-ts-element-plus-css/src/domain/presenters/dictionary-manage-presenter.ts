import { DictionaryManageData, TreeNode } from "~/domain/models/system";
import { ID_SYSTEM_SERVICE, ID_MESSAGE_SERVICE } from "~/types";
import { IMessageService } from "~/infrastructure/boundaries/message-service";
import { ISystemService } from "~/domain/boundaries/system-service";
import { inject, injectable } from "inversify";
@injectable()
export class DictionaryManagePresenter {
    constructor(
        @inject(ID_MESSAGE_SERVICE) private readonly messageService: IMessageService,
        @inject(ID_SYSTEM_SERVICE) private readonly service: ISystemService
    ) { }
    public async dictionaryManageInit(): Promise<DictionaryManageData> {
        let data = {} as DictionaryManageData;
        data.nodes = await this.service.getDictionaryTreeNodeList();
        return data;
    }
    public async confirm(data: DictionaryManageData, editedNode: TreeNode): Promise<void> {
        const res = await this.service.dictionarySave(editedNode);
        if (res.code == 0) {
            this.messageService.success("保存成功");
            data.nodes = await this.service.getDictionaryTreeNodeList();
        } else {
            this.messageService.error(res.msg);
        }
    };
    public async deleteItem(data: DictionaryManageData): Promise<void> {
        const result = await this.messageService.confirm("确认删除吗?");
        if (!result) {
            return;
        }
        const res = await this.service.dictionaryDelete(data.currentNode.id);
        if (res.code == 0) {
            this.messageService.success("删除成功");
            data.nodes = await this.service.getDictionaryTreeNodeList();
        } else {
            this.messageService.error(res.msg);
        }
    };

}