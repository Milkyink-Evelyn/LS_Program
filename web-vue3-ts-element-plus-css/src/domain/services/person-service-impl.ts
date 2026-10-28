import { DataResponse } from "~/infrastructure/models/request";
import { StudentItem, FamilyMemberItem, TeacherItem } from "~/domain/models/person";
import { IRequestService } from "~/infrastructure/boundaries/request-service";
import { IPersonService } from "~/domain/boundaries/person-service";
import { ID_REQUEST_SERVICE, } from "~/types";
import { inject, injectable } from "inversify";
@injectable()
export class PersonServiceImpl implements IPersonService {
    constructor(
        @inject(ID_REQUEST_SERVICE) private readonly requestService: IRequestService
    ) { }
    //获取学生列表分页数据后台数据请求方法
    public async getStudentPageData(
        numName: String | null,
        currentPage: number
    ): Promise<DataResponse> {
        const res = await this.requestService.generalRequest("/api/student/getStudentPageData", {
            numName: numName,
            currentPage: currentPage,
        });
        return res as DataResponse;
    }
    //删除学生后台数据请求方法
    public async studentDelete(personId: number): Promise<DataResponse> {
        const res = await this.requestService.generalRequest("/api/student/studentDelete", {
            personId: personId,
        });
        return res as DataResponse;
    }
    //获取学生基本信息后台数据请求方法
    public async getStudentInfo(personId: number): Promise<StudentItem> {
        const res = await this.requestService.generalRequest("/api/student/getStudentInfo", {
            personId: personId,
        });
        return res.data as StudentItem;
    }
    //保存学生基本信息后台数据请求方法
    public async studentEditSave(
        personId: number | null,
        form: StudentItem
    ): Promise<DataResponse> {
        const res = await this.requestService.generalRequest("/api/student/studentEditSave", {
            personId: personId,
            form: form,
        });
        return res as DataResponse;
    }
    //获取学生家庭成员列表数据后台数据请求方法
    public async getFamilyMemberList(
        personId: number
    ): Promise<FamilyMemberItem[]> {
        const res = await this.requestService.generalRequest("/api/student/getFamilyMemberList", {
            personId: personId,
        });
        return res.data as FamilyMemberItem[];
    }
    //保存学生家庭成员信息后台数据请求方法
    public async familyMemberSave(
        form: FamilyMemberItem
    ): Promise<DataResponse> {
        const res = await this.requestService.generalRequest("/api/student/familyMemberSave", {
            form: form,
        });
        return res as DataResponse;
    }
    //删除学生家庭成员信息后台数据请求方法
    public async familyMemberDelete(
        memberId: number
    ): Promise<DataResponse> {
        const res = await this.requestService.generalRequest("/api/student/familyMemberDelete", {
            memberId: memberId,
        });
        return res as DataResponse;
    }
    //获取教师列表数据后台数据请求方法
    public async getTeacherList(numName: String | null): Promise<[]> {
        const res = await this.requestService.generalRequest("/api/teacher/getTeacherList", {
            numName: numName,
        });
        return res as [];
    }
    //获取教师基本信息后台数据请求方法
    public async getTeacherInfo(
        personId: number | null
    ): Promise<TeacherItem> {
        const res = await this.requestService.generalRequest("/api/teacher/getTeacherInfo", {
            personId: personId,
        });
        return res as TeacherItem;
    }
    //保存教师基本信息后台数据请求方法
    public async teacherEditSave(
        teracherId: number,
        form: TeacherItem
    ): Promise<DataResponse> {
        const res = await this.requestService.generalRequest("/api/teacher/teacherEditSave", {
            personId: personId,
            form: form,
        });
        return res as DataResponse;
    }
    //删除教师后台数据请求方法
    public async teacherDelete(personId: number): Promise<DataResponse> {
        const res = await this.requestService.generalRequest("/api/teacher/teacherDelete", {
            personId: personId,
        });
        return res as DataResponse;
    }

}