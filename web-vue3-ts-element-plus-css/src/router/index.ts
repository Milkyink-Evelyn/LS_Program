import { createRouter, createWebHashHistory } from "vue-router";
import type { Role } from "~/types";
import { useAppStore } from "~/stores/app";
import BasicLayout from "~/layouts/BasicLayout.vue";

export const roleHome: Record<Role, string> = {
  ADMIN: "/admin/dashboard",
  DOCTOR: "/doctor/dashboard",
  PATIENT: "/patient/home",
};

/** 已登录后不应再停留的页面 */
const guestOnlyPaths = ["/login", "/register", "/forgot-password", "/reset-password"];
/** 完全公开的页面 */
const publicPaths = [...guestOnlyPaths, "/about"];

const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("~/views/Login.vue"),
    meta: { title: "登录" },
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("~/views/Register.vue"),
    meta: { title: "用户注册" },
  },
  {
    path: "/forgot-password",
    name: "ForgotPassword",
    component: () => import("~/views/ForgotPassword.vue"),
    meta: { title: "忘记密码" },
  },
  {
    path: "/reset-password",
    name: "ResetPassword",
    component: () => import("~/views/ResetPassword.vue"),
    meta: { title: "重置密码" },
  },
  {
    path: "/about",
    name: "About",
    component: () => import("~/views/About.vue"),
    meta: { title: "关于项目" },
  },
  {
    path: "/",
    component: BasicLayout,
    redirect: () => {
      const store = useAppStore();
      return store.user ? roleHome[store.user.role] : "/login";
    },
    children: [
      {
        path: "admin/dashboard",
        component: () => import("~/views/admin/Dashboard.vue"),
        meta: { title: "数据看板", roles: ["ADMIN"] },
      },
      {
        path: "admin/departments",
        component: () => import("~/views/admin/Departments.vue"),
        meta: { title: "科室管理", roles: ["ADMIN"] },
      },
      {
        path: "admin/doctors",
        component: () => import("~/views/admin/Doctors.vue"),
        meta: { title: "医生管理", roles: ["ADMIN"] },
      },
      {
        path: "admin/schedules",
        component: () => import("~/views/admin/Schedules.vue"),
        meta: { title: "排班管理", roles: ["ADMIN"] },
      },
      {
        path: "admin/appointments",
        component: () => import("~/views/admin/Appointments.vue"),
        meta: { title: "挂号管理", roles: ["ADMIN"] },
      },
      {
        path: "admin/users",
        component: () => import("~/views/admin/Users.vue"),
        meta: { title: "用户管理", roles: ["ADMIN"] },
      },
      {
        path: "doctor/dashboard",
        component: () => import("~/views/doctor/Dashboard.vue"),
        meta: { title: "工作台", roles: ["DOCTOR"] },
      },
      {
        path: "doctor/schedules",
        component: () => import("~/views/doctor/Schedules.vue"),
        meta: { title: "我的排班", roles: ["DOCTOR"] },
      },
      {
        path: "doctor/appointments",
        component: () => import("~/views/doctor/Appointments.vue"),
        meta: { title: "就诊管理", roles: ["DOCTOR"] },
      },
      {
        path: "patient/home",
        component: () => import("~/views/patient/Home.vue"),
        meta: { title: "预约挂号", roles: ["PATIENT"] },
      },
      {
        path: "patient/appointments",
        component: () => import("~/views/patient/Appointments.vue"),
        meta: { title: "我的挂号", roles: ["PATIENT"] },
      },
      {
        path: "patient/records",
        component: () => import("~/views/patient/Records.vue"),
        meta: { title: "我的病历", roles: ["PATIENT"] },
      },
      {
        path: "profile",
        component: () => import("~/views/Profile.vue"),
        meta: { title: "个人中心" },
      },
    ],
  },
  {
    path: "/:pathMatch(.*)*",
    component: () => import("~/views/NotFound.vue"),
    meta: { title: "页面不存在" },
  },
];

const router = createRouter({
  history: createWebHashHistory(),
  routes,
});

router.beforeEach((to) => {
  const store = useAppStore();
  const isLoggedIn = Boolean(store.token && store.user);

  if (publicPaths.includes(to.path)) {
    if (isLoggedIn && guestOnlyPaths.includes(to.path)) {
      return roleHome[store.user!.role];
    }
    return true;
  }

  if (!isLoggedIn) {
    return "/login";
  }

  const roles = to.meta.roles as Role[] | undefined;
  if (roles && !roles.includes(store.user!.role)) {
    return roleHome[store.user!.role];
  }

  return true;
});

router.afterEach((to) => {
  document.title = to.meta.title
    ? `${to.meta.title} - 校医院预约挂号系统`
    : "校医院预约挂号系统";
});

export default router;
