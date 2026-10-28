import { OptionItem } from "~/infrastructure/models/base";
import { StudentItem } from "~/domain/models/person";
import { ScoreItem } from "~/domain/models/teaching";
export interface PasswordData {
    oldPassword: string;
    newPassword: string;
    checkPassword: string;
}
export interface StudentIntroduceData {
    authHeader: any;
    info: StudentItem;
    feeList: OptionItem[];
    markList: OptionItem[];
    scoreList: ScoreItem[];
    personId: number | null;
    imgStr: string;
}
export interface SystemIntroduceData {
}