import { StudentIntroduceData } from "~/domain/models/info";
import { StudentItem } from "~/domain/models/person";
import { ID_INFO_SERVICE, ID_MESSAGE_SERVICE } from "~/types";
import { IMessageService } from "~/infrastructure/boundaries/message-service";
import { IInfoService } from "~/domain/boundaries/info-service";
import { inject, injectable } from "inversify";
@injectable()
export class StudentIntroducePresenter {
    constructor(
        @inject(ID_MESSAGE_SERVICE) private readonly messageService: IMessageService,
        @inject(ID_INFO_SERVICE) private readonly service: IInfoService
    ) { }
    public async studentIntroduceInit(data: StudentIntroduceData): Promise<void> {
        let res = await this.service.getStudentIntroduceData(data.personId);
        if (res.code == 0) {
            data.info = res.data.info;
            data.personId = data.info.personId;
            data.feeList = res.data.feeList;
            data.markList = res.data.markList;
            data.scoreList = res.data.scoreList;
            res = await this.service.getPhotoImageStr("photo/" + data.info.personId + ".jpg");
            data.imgStr = res.data;
        }
    }
}