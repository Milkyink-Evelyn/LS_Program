import { ScoreData, ScoreItem } from "~/domain/models/teaching";
import { ID_TEACHING_SERVICE, ID_MESSAGE_SERVICE } from "~/types";
import { IMessageService } from "~/infrastructure/boundaries/message-service";
import { ITeachingService } from "~/domain/boundaries/teaching-service";
import { inject, injectable } from "inversify";
@injectable()
export class ScorePresenter {
    constructor(
        @inject(ID_MESSAGE_SERVICE) private readonly messageService: IMessageService,
        @inject(ID_TEACHING_SERVICE) private readonly service: ITeachingService
    ) { }
    public async scoreInit(): Promise<ScoreData> {
        let data = {} as ScoreData;
        data.personId = 0;
        data.courseId = 0;
        data.studentList = await this.service.getStudentItemOptionList();
        data.courseList = await this.service.getCourseItemOptionList();
        data.studentList.unshift({ id: 0, value: "", title: "请选择" });
        data.courseList.unshift({ id: 0, value: "", title: "请选择" });
        await this.getScoreList(data);
        return data;
    }
    public async getScoreList(data: ScoreData): Promise<void> {
        data.dataList = await this.service.getScoreList(data.personId, data.courseId);
    }
    public addItem(data: ScoreData): ScoreItem {
        let item = { personId: 0, courseId: 0 } as ScoreItem;
        return item;
    }
    public editItem(data: ScoreData, index: number): ScoreItem {
        let item = { ...data.dataList[index] } as ScoreItem;
        return item;
    }
    public async itemSubmit(item: ScoreItem, data: ScoreData): Promise<void> {
        const res = await this.service.scoreSave(item.scoreId, item.personId, item.courseId, item.mark);
        if (res.code == 0) {
            this.messageService.success("添加修改成功");
            this.getScoreList(data);
        } else {
            this.messageService.error(res.msg);
        }
    }
    async deleteItem(data: ScoreData, index: number) {
        const result = await this.messageService.confirm("确认删除成绩吗?");
        if (!result) {
            return;
        }
        const res = await this.service.scoreDelete(data.dataList[index].scoreId);
        if (res.code == 0) {
            this.messageService.success("删除成功");
            data.dataList.splice(index, 1);
        } else {
            this.messageService.error(res.msg);
        }
    };

}