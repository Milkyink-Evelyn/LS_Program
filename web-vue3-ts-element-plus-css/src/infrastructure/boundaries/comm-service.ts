import { OptionItem } from '~/infrastructure/models/base';
export interface ICommService {
    getEditorApiKey(): string;
    getPleaseSelect(): string;
    getOptionItemIndex(itemList: OptionItem[], value: string): number;
    getOptionItem(itemList: OptionItem[], value: string): OptionItem;
    formatDate(d: Date): string;
    getDate(type: string): string;
    getDateFromStr(str: string): any;
    formatDateTime(d: Date): string;
    formatTime(d: Date): string;
    getDialog(id: string): HTMLDialogElement;
    getStringFromArrayJoin(array: any[]): string;
    getStringFromArray(array: any[]): string;
    getIntegerArrayFromStr(str: string): number[];
    getHomeWorkList(homework: string): OptionItem[];
    getGradeList(): string[];
    getYearList(): string[];
    addSelectOptionItem(list: OptionItem[], id: number, value: string, title: string): void;
    removeChar(str: string, c: string): string;
    removeErrorChar(str: string): string;
    containsNumber(str: string): boolean;
    getEncodeString(str: string): string;
    getDecodeString(str: string): string;
    spliceParagraph(str: string): string[];
    getPageSizeSelectList(): OptionItem[];
}
