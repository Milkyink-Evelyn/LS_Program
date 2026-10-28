export interface UserInfo {
  loggedIn: boolean;
  username: string;
  perName: string;
  token: string;
  id: number;
  role: string;
}
export interface MenuInfo {
  id: number;
  name: string;
  path: string;
  title: string;
  sList: MenuInfo[];
}
export interface SystemConfig {
  menuList: MenuInfo[];
  dataBaseUserName: string;
}
