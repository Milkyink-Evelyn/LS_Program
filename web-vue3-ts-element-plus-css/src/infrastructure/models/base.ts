export interface MenuInfo {
    id: number;
    name: string;
    path: string;
    title: string;
    sList: MenuInfo[];
}
export interface OptionItem {
    id: number;
    value: string;
    title: string;
}

export interface UserOnlineItem {
    total: number;
    monthCount: number;
    dayCount: number;
}
export interface ChartItem {
    value: string[];
    label1: string[];
    label2: string[];
}
export interface MainPageData {
    onlineUser: UserOnlineItem,
    userTypeList: [],
    requestData: ChartItem,
    operateData: ChartItem,
}

export interface Pagination {
    total: number;
    pageSize: number;
    currentPage: number;
}
