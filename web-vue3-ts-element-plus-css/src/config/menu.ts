import type { Role } from "~/types";

export interface MenuChild {
  path: string;
  title: string;
}

export interface MenuGroup {
  title: string;
  icon: string;
  children: MenuChild[];
}

export interface MenuItem {
  path: string;
  title: string;
  icon: string;
}

export type MenuConfig = Array<MenuGroup | MenuItem>;

export function isMenuGroup(item: MenuGroup | MenuItem): item is MenuGroup {
  return "children" in item;
}

export const menus: Record<Role, MenuConfig> = {
  ADMIN: [
    { path: "/admin/dashboard", title: "数据看板", icon: "DataAnalysis" },
    {
      title: "基础管理",
      icon: "Setting",
      children: [
        { path: "/admin/departments", title: "科室管理" },
        { path: "/admin/doctors", title: "医生管理" },
        { path: "/admin/users", title: "用户管理" },
      ],
    },
    {
      title: "排班挂号",
      icon: "Calendar",
      children: [
        { path: "/admin/schedules", title: "排班管理" },
        { path: "/admin/appointments", title: "挂号管理" },
      ],
    },
    { path: "/about", title: "关于项目", icon: "InfoFilled" },
  ],
  DOCTOR: [
    { path: "/doctor/dashboard", title: "工作台", icon: "HomeFilled" },
    {
      title: "诊疗管理",
      icon: "FirstAidKit",
      children: [
        { path: "/doctor/schedules", title: "我的排班" },
        { path: "/doctor/appointments", title: "就诊管理" },
      ],
    },
    { path: "/about", title: "关于项目", icon: "InfoFilled" },
  ],
  PATIENT: [
    { path: "/patient/home", title: "预约挂号", icon: "Plus" },
    {
      title: "我的就医",
      icon: "Document",
      children: [
        { path: "/patient/appointments", title: "我的挂号" },
        { path: "/patient/records", title: "我的病历" },
      ],
    },
    { path: "/about", title: "关于项目", icon: "InfoFilled" },
  ],
};
