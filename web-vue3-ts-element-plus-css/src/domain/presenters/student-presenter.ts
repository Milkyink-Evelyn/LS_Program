import { StudentData, StudentItem } from "~/domain/models/person";
import { ID_PERSON_SERVICE, ID_REQUEST_SERVICE, ID_MESSAGE_SERVICE, ID_BASE_SERVICE, ID_COMM_SERVICE, ID_INFO_SERVICE } from "~/types";
import { IRequestService } from "~/infrastructure/boundaries/request-service";
import { ICommService } from "~/infrastructure/boundaries/comm-service";
import { IMessageService } from "~/infrastructure/boundaries/message-service";
import { IBaseService } from "~/infrastructure/boundaries/base-service";
import { IInfoService } from "~/domain/boundaries/info-service";
import { IPersonService } from "~/domain/boundaries/person-service";
import { inject, injectable } from "inversify";
@injectable()
export class StudentPresenter {
    constructor(
        @inject(ID_REQUEST_SERVICE) private readonly requestService: IRequestService,
        @inject(ID_COMM_SERVICE) private readonly commService: ICommService,
        @inject(ID_MESSAGE_SERVICE) private readonly messageService: IMessageService,
        @inject(ID_BASE_SERVICE) private readonly baseService: IBaseService,
        @inject(ID_INFO_SERVICE) private readonly infoService: IInfoService,
        @inject(ID_PERSON_SERVICE) private readonly service: IPersonService
    ) { }

    public async studentInit(): Promise<StudentData> {
        let data = {} as StudentData;
        data.pagination = {
            currentPage: 1,
            pageSize: 10,
            total: 0
        };
        data.authHeader = this.requestService.getAuthHeader();
        data.numName = "";
        data.genderList = await this.baseService.getDictionaryOptionItemList("XBM");
        data.genderList.unshift({ id: 0, value: "", title: "请选择..." });
        await this.getStudentDataPage(data);
        return data;
    }
    public async getStudentDataPage(data: StudentData): Promise<void> {
        const res = await this.service.getStudentPageData(data.numName, data.pagination.currentPage);
        data.pagination.total = res.data.dataTotal;
        data.pagination.pageSize = res.data.pageSize;
        data.dataList = res.data.dataList;

    }
    public async doExport(data: StudentData): Promise<void> {
        const res = await this.requestService.downloadPost(
            "/api/student/getStudentListExcl",
            "学生.xlsx",
            {
                numName: data.numName,
            }
        );
        if (res != 200) {
            this.messageService.error("导出失败！");
        }
    };
    public addItem(data: StudentData): StudentItem {
        let item = {} as StudentItem;
        data.currentIndex = -1;
        return item;
    }
    public async editItem(data: StudentData, index: number): Promise<StudentItem> {
        let item = { ...data.dataList[index] } as StudentItem;
        data.currentIndex = index;
        data.birthday = this.commService.getDateFromStr(item.birthday);
        const res = await this.infoService.getPhotoImageStr("photo/" + item.personId + ".jpg");
        data.imgStr = res.data;
        return item;
    }
    public async itemSubmit(item: StudentItem, data: StudentData): Promise<void> {
        item.birthday = this.commService.formatDate(data.birthday);
        const res = await this.service.studentEditSave(item.personId, item);
        if (res.code == 0) {
            let personId = res.data;
            item = await this.service.getStudentInfo(personId);
            if (data.currentIndex == -1) {
                data.dataList.push(item);
                data.pagination.total++;
                this.messageService.success("添加成功");
            } else {
                data.dataList[data.currentIndex] = item;
                this.messageService.success("修改成功");
            }
        } else {
            this.messageService.error(res.msg);
        }
    }
    async deleteItem(data: StudentData, index: number) {
        const result = await this.messageService.confirm("确认删除学生吗?");
        if (!result) {
            return;
        }
        const res = await this.service.studentDelete(data.dataList[index].personId);
        if (res.code == 0) {
            this.messageService.success("删除成功");
            data.dataList.splice(index, 1);
            data.pagination.total--;
        } else {
            this.messageService.error(res.msg);
        }
    };
    public async onSuccessPhoto(data: StudentData, res: any): Promise<void> {
        if (res.code == 0) {
            this.messageService.success("上传成功！");
            const res = await this.infoService.getPhotoImageStr("photo/" + data.dataList[data.currentIndex].personId + ".jpg");
            data.imgStr = res.data;
        } else {
            this.messageService.error(res.msg);
        }
    }
}