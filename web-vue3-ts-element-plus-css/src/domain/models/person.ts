import { Option } from "element-plus/es/components/select-v2/src/select.types";
import { OptionItem, Pagination } from "~/infrastructure/models/base";
export interface PersonItem {
    personId: number;
    num: string;
    name: string;
    dept: string;
    card: string;
    gender: string;
    genderName: string;
    birthday: string;
    email: string;
    phone: string;
    address: string;
}


//用于学生数据信息的定义
export interface StudentItem {
    personId: number;
    num: string;
    name: string;
    dept: string;
    major: string;
    className: string;
    card: string;
    gender: string;
    genderName: string;
    birthday: string;
    email: string;
    phone: string;
    address: string;
}
//用于家庭成员数据信息的定义
export interface FamilyMemberItem {
    memberId: number;
    personId: number;
    relation: string;
    name: string;
    gender: string;
    age: string;
    unit: string;
}
//用于教师数据信息的定义
export interface TeacherItem {
    personId: number;
    person: PersonItem;
    title: string;
    degree: string;
}

export interface StudentData {
    authHeader: any;
    numName: string;
    dataList: StudentItem[];
    pagination: Pagination;
    currentIndex: number;
    genderList: OptionItem[];
    birthday: Date;
    imgStr: string;
}

export interface FamilyMemberData {
    personId: number;
    dataList: FamilyMemberItem[];
    genderList: OptionItem[];
}
