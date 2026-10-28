import { DataResponse } from "~/infrastructure/models/request";
import { StudentItem, FamilyMemberItem, TeacherItem } from "~/domain/models/person";
export interface IPersonService {
    getStudentPageData(numName: String | null, currentPage: number): Promise<DataResponse>;
    studentDelete(personId: number): Promise<DataResponse>;
    getStudentInfo(personId: number): Promise<StudentItem>;
    studentEditSave(personId: number | null, form: StudentItem): Promise<DataResponse>;
    getFamilyMemberList(personId: number): Promise<FamilyMemberItem[]>;
    familyMemberSave(form: FamilyMemberItem): Promise<DataResponse>;
    familyMemberDelete(memberId: number): Promise<DataResponse>;
    getTeacherList(numName: String | null): Promise<[]>;
    getTeacherInfo(personId: number | null): Promise<TeacherItem>;
    teacherEditSave(personId: number, form: TeacherItem): Promise<DataResponse>;
    teacherDelete(personId: number): Promise<DataResponse>;
}
