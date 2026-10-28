import { CourseData, CourseItem } from "~/domain/models/teaching";
import { ID_TEACHING_SERVICE, ID_MESSAGE_SERVICE } from "~/types";
import { IMessageService } from "~/infrastructure/boundaries/message-service";
import { ITeachingService } from "~/domain/boundaries/teaching-service";
import { inject, injectable } from "inversify";
@injectable()
export class CoursePresenter {
    constructor(
        @inject(ID_MESSAGE_SERVICE) private readonly messageService: IMessageService,
        @inject(ID_TEACHING_SERVICE) private readonly service: ITeachingService
    ) { }
    public makeSelectCourseList(data: CourseData): void {
        data.courseSelectList = []
        for (let i = 0; i < data.dataList.length; i++) {
            const item = data.dataList[i]
            data.courseSelectList.push({
                id: item.courseId,
                value: item.num,
                title: item.num + '-' + item.name
            })
        }
    };
    public async courseInit(): Promise<CourseData> {
        let data = {} as CourseData;
        data.numName = "";
        await this.getCourseList(data);
        return data;
    }
    public async getCourseList(data: CourseData): Promise<void> {
        data.dataList = await this.service.getCourseList(data.numName);
        this.makeSelectCourseList(data);
    }
    public addItem(data: CourseData): CourseItem {
        let item = {} as CourseItem;
        data.currentIndex = -1;
        return item;
    }
    public editItem(data: CourseData, index: number): CourseItem {
        let item = { ...data.dataList[index] } as CourseItem;
        return item;
    }
    public async itemSubmit(item: CourseItem, data: CourseData): Promise<void> {
        const res = await this.service.courseSave(item);
        if (res.code == 0) {
            this.messageService.success("添加修改成功");
            this.getCourseList(data);
        } else {
            this.messageService.error(res.msg);
        }
    }
    async deleteItem(data: CourseData, index: number) {
        const result = await this.messageService.confirm("确认删除课程吗?");
        if (!result) {
            return;
        }
        const res = await this.service.courseDelete(data.dataList[index].courseId);
        if (res.code == 0) {
            this.messageService.success("删除成功");
            data.dataList.splice(index, 1);
        } else {
            this.messageService.error(res.msg);
        }
    };

}