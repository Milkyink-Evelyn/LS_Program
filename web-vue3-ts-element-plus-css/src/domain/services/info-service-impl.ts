import { DataResponse } from "~/infrastructure/models/request";
import { IRequestService } from "~/infrastructure/boundaries/request-service";
import { IInfoService } from "~/domain/boundaries/info-service";
import { ID_REQUEST_SERVICE, } from "~/types";
import { inject, injectable } from "inversify";
@injectable()
export class InfoServiceImpl implements IInfoService {
    constructor(
        @inject(ID_REQUEST_SERVICE) private readonly requestService: IRequestService
    ) { }
    //修改口令后台数据请求方法
    public async updatePassword(data: Object): Promise<DataResponse> {
        const res = await this.requestService.generalRequest("/api/base/updatePassword", data);
        return res as DataResponse;
    }
    //获取学生个人简介信息后台数据请求方法
    public async getStudentIntroduceData(
        personId: number | null
    ): Promise<DataResponse> {
        const res = await this.requestService.generalRequest("/api/student/getStudentIntroduceData", {
            personId: personId,
        });
        return res as DataResponse;
    }
    //获取学生照片数据后台数据请求方法
    public async getPhotoImageStr(
        fileName: String
    ): Promise<DataResponse> {
        const res = await this.requestService.generalRequest("/api/base/getPhotoImageStr", {
            fileName: fileName,
        });
        return res as DataResponse;
    }
    //上传学生照片数据后台数据请求方法
    public async uploadPhoto(remoteFile: string, file: any): Promise<any> {
        const formData = new FormData();
        formData.append("file", file);
        const res = await this.requestService.uploadRequest(
            "/api/base/uploadPhotoWeb?remoteFile=" + remoteFile,
            formData
        );
        return res as DataResponse;
    }
}