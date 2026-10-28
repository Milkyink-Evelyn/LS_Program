import { FamilyMemberData } from "~/domain/models/person";
import { ID_PERSON_SERVICE, ID_MESSAGE_SERVICE, ID_BASE_SERVICE } from "~/types";
import { IMessageService } from "~/infrastructure/boundaries/message-service";
import { IBaseService } from "~/infrastructure/boundaries/base-service";
import { IPersonService } from "~/domain/boundaries/person-service";
import { inject, injectable } from "inversify";
@injectable()
export class FamilyMemberPresenter {
    constructor(
        @inject(ID_MESSAGE_SERVICE) private readonly messageService: IMessageService,
        @inject(ID_BASE_SERVICE) private readonly baseService: IBaseService,
        @inject(ID_PERSON_SERVICE) private readonly service: IPersonService
    ) { }

    public async familyMemberInit(data: FamilyMemberData): Promise<void> {
        data.genderList = await this.baseService.getDictionaryOptionItemList("XBM");
        data.genderList.unshift({ id: 0, value: "", title: "请选择..." });
        if (data.personId > 0) {
            data.dataList = await this.service.getFamilyMemberList(data.personId);
        } else {
            data.dataList = [];
        }
    };
    async saveItem(data: FamilyMemberData, index: number) {
        const item = data.dataList[index];
        const res = await this.service.familyMemberSave(item);
        if (res.code == 0) {
            this.messageService.success("保存成功");
        } else {
            this.messageService.error(res.msg);
        }
    };
    async deleteItem(data: FamilyMemberData, index: number) {
        const result = await this.messageService.confirm("确认删除当前成员吗?");
        if (!result) {
            return;
        }
        const res = await this.service.familyMemberDelete(data.dataList[index].memberId);
        if (res.code == 0) {
            this.messageService.success("删除成功");
            data.dataList.splice(index, 1);
        } else {
            this.messageService.error(res.msg);
        }
    };

}