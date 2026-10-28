import { Option } from "element-plus/es/components/select-v2/src/select.types";
import { OptionItem } from "~/infrastructure/models/base";

//用于课程数据信息的定义
export interface CourseItem {
    courseId: number;
    num: string;
    name: string;
    credit: number;
    coursePath: string;
    preCourse: string;
    preCourseId: number;
}

export interface CourseData {
    numName: string;
    dataList: CourseItem[];
    courseSelectList: OptionItem[];
    currentIndex: number;
}
export interface ScoreItem {
    scoreId: number;
    personId: number;
    courseId: number;
    studentNum: string;
    studentName: string;
    courseNum: string;
    courseName: string;
    credit: number;
    mark: number;
    ranking: number;
}
export interface ScoreData {
    personId: number;
    courseId: number;
    dataList: ScoreItem[];
    studentList: OptionItem[];
    courseList: OptionItem[];
}