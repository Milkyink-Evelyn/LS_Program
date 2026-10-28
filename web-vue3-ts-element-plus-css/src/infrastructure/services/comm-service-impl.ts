import { Base64 } from "js-base64";
import { OptionItem } from '~/infrastructure/models/base';
import { ICommService } from '~/infrastructure/boundaries/comm-service';
export class CommServiceImpl implements ICommService {
    public getEditorApiKey(): string {
        return "q8yfwq3nbanuhmvj5a147eg6mhktttnr0zzma8yw1f3zn237";
    }
    public getOptionItemTitleByValue(
        itemList: OptionItem[],
        value: string
    ): string {
        let index = -1;
        for (let i = 0; i < itemList.length; i++) {
            if (itemList[i].value === value) {
                index = i;
                return itemList[i].title;
            }
        }
        return "";
    }
    public getOptionItemTitleById(
        itemList: OptionItem[],
        id: number
    ): string {
        let index = -1;
        for (let i = 0; i < itemList.length; i++) {
            if (itemList[i].id === id) {
                index = i;
                return itemList[i].title;
            }
        }
        return "";
    }

    public getOptionItemIndex(
        itemList: OptionItem[],
        value: string
    ): number {
        let index = -1;
        for (let i = 0; i < itemList.length; i++) {
            if (itemList[i].value === value) {
                index = i;
                break;
            }
        }
        return index;
    }
    public getPleaseSelect(): string {
        return "请选择...";
    }
    //搜索框选择项列表，获取当前值的选择项对象
    public getOptionItem(
        itemList: OptionItem[],
        value: string
    ): OptionItem {
        const index = this.getOptionItemIndex(itemList, value);
        if (index < 0) {
            return {} as OptionItem;
        }
        return itemList[index];
    }
    //日期转换成 yyyy-MM-dd 格式的字符串
    public formatDate(d: Date): string {
        if (!(d instanceof Date && !isNaN(d.getTime()))) return "";
        const year = d.getFullYear();
        let month = "" + (d.getMonth() + 1);
        let day = "" + d.getDate();

        if (month.length < 2) month = "0" + month;
        if (day.length < 2) day = "0" + day;

        return [year, month, day].join("-");
    }
    public getDate(type: string): string {
        const date = new Date();
        let year = date.getFullYear();
        let month = "" + (date.getMonth() + 1);
        let day = "" + date.getDate();

        if (type === "start") {
            year = year - 100;
        } else if (type === "end") {
            year = year + 2;
        }
        if (month.length < 2) month = "0" + month;
        if (day.length < 2) day = "0" + day;
        return [year, month, day].join("-");
    }

    public getDateFromStr(str: string): any {
        if (str == null || str == undefined || str.length === 0) return null;
        return new Date(str);
    }
    //日期转换成 yyyy-MM-dd HH:mm:ss格式的字符串
    public formatDateTime(d: Date) {
        if (!(d instanceof Date && !isNaN(d.getTime()))) return "";
        const year = d.getFullYear();
        let month = "" + (d.getMonth() + 1);
        let day = "" + d.getDate();
        let hour = "" + d.getHours();
        let minute = "" + d.getMinutes();
        let second = "" + d.getSeconds();

        if (month.length < 2) month = "0" + month;
        if (day.length < 2) day = "0" + day;
        if (hour.length < 2) hour = "0" + hour;
        if (minute.length < 2) minute = "0" + minute;
        if (second.length < 2) second = "0" + second;
        const str1 = [year, month, day].join("-");
        const str2 = [hour, minute, second].join(":");
        return str1 + " " + str2;
    }
    //日期转换成 HH:mm:ss格式的字符串
    public formatTime(d: Date) {
        if (!(d instanceof Date && !isNaN(d.getTime()))) return "";
        let hour = "" + d.getHours();
        let minute = "" + d.getMinutes();
        let second = "" + d.getSeconds();
        if (hour.length < 2) hour = "0" + hour;
        if (minute.length < 2) minute = "0" + minute;
        if (second.length < 2) second = "0" + second;
        return [hour, minute, second].join(":");
    }
    //根据ID获取Html元素
    public getDialog(id: string): HTMLDialogElement {
        return document.getElementById(id) as HTMLDialogElement;
    }
    //字符串转换成字符数组
    public getStringFromArrayJoin(array: any[]): string {
        return array.join(",");
    }
    //数组转换成字符串
    public getStringFromArray(array: any[]): string {
        let str = "";
        if (array == null || array.length === 0) {
            return "";
        }
        str = array[0] + "";
        for (let i = 1; i < array.length; i++) {
            str += "," + array[i];
        }
        return str;
    }
    //字符串转换成整数数组
    public getIntegerArrayFromStr(str: string): number[] {
        if (str == null || str.length === 0) {
            return [] as number[];
        }
        const array = str.split(",") as string[];
        const ret = [] as number[];
        for (let i = 0; i < array.length; i++) {
            ret.push(parseInt(array[i]));
        }
        return ret;
    }

    public getHomeWorkList(homework: string): OptionItem[] {
        const a = homework.split(",") as string[];
        let i: number;
        const noList = [] as OptionItem[];
        let b = [] as string[];
        for (i = 0; i < a.length; i++) {
            b = a[i].split(":");
            noList.push({
                id: i + 1,
                title: b[0],
            } as OptionItem);
        }
        return noList;
    }
    public getGradeList(): string[] {
        const date = new Date();
        let year = date.getFullYear();
        let gradeList = [] as string[];
        let i;
        for (i = 0; i < 15; i++) {
            gradeList.push("" + (year - i));
        }
        return gradeList;
    }
    public getYearList(): string[] {
        const date = new Date();
        let year = date.getFullYear();
        let yearList = [] as string[];
        let i;
        for (i = 0; i < 20; i++) {
            yearList.push("" + (year - i + 1));
        }
        return yearList;
    }

    public addSelectOptionItem(
        list: OptionItem[],
        id: number,
        value: string,
        title: string
    ): void {
        const item = { id: id, value: value, title: title };
        list.unshift(item);
    }
    public removeChar(str: string, c: string): string {
        const reg = new RegExp(c, "g");
        return str.replace(reg, "");
    }
    public removeErrorChar(str: string): string {
        return this.removeChar(str, "'");
    }
    public containsNumber(str: string): boolean {
        var reg = /\d/;
        return reg.test(str);
    }

    public getEncodeString(str: string): string {
        if (str == undefined || str == null || str.length === 0) return "";
        return Base64.encode(str);
    }
    public getDecodeString(str: string): string {
        if (str == undefined || str == null || str.length === 0) return "";
        return Base64.decode(str);
    }
    public spliceParagraph(str: string): string[] {
        if (str == undefined || str == null || str.length == 0) return [] as string[];
        return str.split("\n");
    }
    public getPageSizeSelectList(): OptionItem[] {
        return [
            { id: 20, value: "20", title: "每页20条" },
            { id: 50, value: "50", title: "每页50条" },
            { id: 100, value: "100", title: "每页100条" },
            { id: -1, value: "-1", title: "全部" },
        ];
    }

}