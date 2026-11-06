# 校医院预约挂号系统 — 前端开发交接与改造方案

> 版本：v1.4（已对齐《Web技术实验要求2026》，并按 `web-db.sql` 核对数据库口径）
> 日期：2026-10-29
> 适用工程：`web-vue3-ts-element-plus-css`
> 后端：Spring Boot 3.4 + Spring Data JPA + MySQL，端口 `22222`
> 本文档是前后端唯一接口契约。后端按本文实现；前端不得自行修改接口路径、字段名、枚举值。确需变更时，先改本文档并同步后端。
>
> 本版重点补充了实验要求中的：默认管理员登录、右上角账号切换、用户注册、忘记密码提示、验证码重置密码、个人信息维护、二级菜单、系统 Logo、个人头像、版权页脚、项目/团队介绍页，以及课程要求的打包部署方式。

> **已确认约定（2026-10-29，项目负责人确认）**
> 1. 演示账号固定为：管理员 `admin / 123456`、医生 `2022030001 / 123456`、患者 `2022030002 / 123456`。
> 2. 注册与重置密码使用新增接口 `POST /auth/register`、`POST /auth/resetPassword`；后端模板旧的 `/auth/registerUser` 不复用。
> 3. 验证码统一使用 `POST /auth/getValidateCode`，注册和重置密码服务端校验，登录页不启用验证码。
>
> **数据库口径（2026-10-29 核对 `web-db.sql`）**
> 认证沿用课程模板的 `person`、`user`、`user_type` 三张表，**不新建 `t_user`**；业务新增 5 张表：`t_department`、`t_doctor`、`t_schedule`、`t_appointment`、`t_medical_record`。
> 原因：课程搭建文档要求直接导入 `web-db.sql`，模板登录、JWT、演示账号都已基于 `user/person/user_type`；替换成 `t_user` 会牵动 27 个 Java 文件，风险大于收益。

---

## 0. 给前端 AI 的开工指令

请按以下顺序执行，不要跳步：

1. 先读第 2 章，完成旧模板清理与工程配置调整。
2. 再读第 4、5 章，建立类型、请求层、登录态、路由守卫。
3. 按第 6 章实现全部 API 函数，函数签名和字段必须与文档一致。
4. 按第 7 章逐页实现，页面不得自行发明接口或字段。
5. 按第 9 章分 Wave 推进，每完成一个 Wave 必须运行 `npm run typecheck`。
6. 全部完成后按第 10.4 节做验收，`npm run typecheck` 与 `npm run build` 必须同时通过。

硬性要求：

- 按《Web技术实验要求2026》第 018 条：登录页默认填充管理员账号 `admin / 123456`，点击登录直接进入系统；系统右上角必须提供账号切换功能（管理员 / 医生 / 患者），方便检查。**不得移除这两项演示功能。**
- 必须实现用户注册、忘记密码（界面提示）、重置密码（带验证码）、个人信息维护（真实姓名、邮箱、修改密码）。
- 必须实现二级菜单导航、系统 Logo、个人头像、版权说明页脚。
- 必须实现项目展示与团队/成员介绍页面，可使用现有图片和视频素材。
- 不引入新的 UI 组件库、状态管理库、请求库、图表库。现有 `element-plus`、`pinia`、`axios`、`echarts` 足够。
- 不使用 SSR、微前端、GraphQL、WebSocket、国际化框架。
- 不修改后端代码，不修改数据库。
- 页面必须处理加载中、空数据、接口报错三种状态。
- 所有金额、日期、状态显示必须使用本文定义的格式，不得自创。
- 所有删除、取消、接诊提交操作必须有二次确认。

---

## 1. 项目与角色

### 1.1 业务目标

实现一个轻量级校医院预约挂号系统，覆盖三条主链路：

1. 管理员：维护科室、医生、用户，按日期和时段排班，查看全部挂号与统计数据。
2. 患者：选择科室和医生，查看可挂号源，完成挂号，查看/取消自己的挂号，查看病历。
3. 医生：查看自己的排班和就诊列表，对患者完成接诊并填写病历。

### 1.2 角色与权限

| 角色值 | 中文 | 登录后首页 | 权限范围 |
|---|---|---|---|
| `ADMIN` | 管理员 | `/admin/dashboard` | 全部管理功能、统计 |
| `DOCTOR` | 医生 | `/doctor/dashboard` | 自己的排班、自己的就诊、接诊 |
| `PATIENT` | 患者 | `/patient/home` | 预约挂号、自己的挂号与病历、个人中心 |

角色值由后端登录接口返回，前端不得根据用户名猜角色。

演示账号（密码均为 `123456`，登录页默认填管理员）：

| 角色 | 用户名 | 用途 |
|---|---|---|
| `ADMIN` | `admin` | 默认登录、完整管理权限 |
| `DOCTOR` | `2022030001` | 医生端演示 |
| `PATIENT` | `2022030002` | 患者端演示 |

现有 `web-db.sql` 中 `2022030001`、`2022030002` 的角色都是 `ROLE_STUDENT`，需要按附录 A 改成 `ROLE_DOCTOR`、`ROLE_PATIENT`。`person` 表中两人姓名为杨平、张菊，前端只显示后端返回的 `realName`，不在前端写死。

右上角“切换账号”使用这三个演示账号重新调用登录接口，拿到新 token 后跳到对应角色首页。该功能仅用于课程检查和答辩演示，代码中必须集中放在 `src/config/demoAccounts.ts`，不得散落在页面里。

### 1.3 核心业务流程

挂号：

```
患者登录 -> 选科室 -> 选医生 -> 选日期/时段 -> 确认挂号
-> 后端校验 remain_quota > 0 -> remain_quota - 1 -> 生成 status=0 的挂号单
```

接诊：

```
医生登录 -> 查看 status=0 的挂号 -> 点击接诊 -> 填写症状/诊断/处方
-> 后端保存病历 -> 挂号状态改为 status=1
```

取消：

```
患者登录 -> 我的挂号 -> 取消 status=0 的挂号
-> 后端状态改为 status=2 -> remain_quota + 1
```

### 1.4 页面地图

```
/login                         登录（默认 admin/123456）
/register                      患者注册（带验证码）
/forgot-password               忘记密码（界面提示）
/reset-password                重置密码（带验证码）
/about                         项目展示与团队介绍（公开页）

/admin/dashboard               管理员-数据看板（3 张 ECharts）
/admin/departments             管理员-科室管理
/admin/doctors                 管理员-医生管理
/admin/schedules               管理员-排班管理
/admin/appointments            管理员-挂号管理
/admin/users                   管理员-用户管理

/doctor/dashboard              医生-工作台
/doctor/schedules              医生-我的排班
/doctor/appointments           医生-就诊管理

/patient/home                  患者-预约挂号
/patient/appointments          患者-我的挂号
/patient/records               患者-我的病历

/profile                       个人中心（三种角色共用）
/:pathMatch(.*)*               404
```

---

## 2. 现有模板评估与改造总方针

### 2.1 模板已具备的能力

当前 `web-vue3-ts-element-plus-css` 已具备：

- Vue 3 + TypeScript + Vite
- Element Plus（自动按需引入）
- Pinia + `pinia-plugin-persistedstate`
- Vue Router（hash 模式）
- Axios
- ECharts 5
- 登录页、整体布局、侧边菜单、示例 CRUD 页面
- `inversify` 依赖注入 + `domain/infrastructure/presenters` 分层

### 2.2 保留、改造、删除

| 对象 | 处理方式 | 说明 |
|---|---|---|
| `src/main.ts` | 保留，微调 | 保留 Element Plus、Pinia、Router、图标注册 |
| `src/stores/index.ts` | 保留 | Pinia 持久化插件已配置 |
| `src/styles/**` | 保留 | 全局样式可继续使用 |
| `public/login_pic.png`、`public/conlinkinfo.png` | 保留 | 登录背景和 Logo 可复用 |
| `src/Login.vue` | 删除，新建 `src/views/Login.vue` | 保留“默认管理员账号密码、点击直接登录”的实验要求，重写旧登录流程 |
| `src/router/index.ts` | 重写 | 改为三角色路由 + 守卫 + 注册/重置密码/项目介绍公开路由 |
| `src/App.vue` | 重写 | 只保留 `<router-view />` |
| `src/stores/app.ts` | 重写 | 只保存 token、当前用户、角色 |
| 旧 `infrastructure/services/request-service-impl.ts` | 替换 | 改为 RESTful 请求层 |
| 旧 `infrastructure/services/login-service-impl.ts` | 删除 | 登录逻辑并入 `src/api/auth.ts` |
| 旧 `infrastructure/presenters/**` | 删除 | 新页面不再使用 Presenter 模式 |
| 旧 `domain/**` | 删除 | 旧的学生/教师/课程/成绩模型全部删除 |
| `src/inverfiy.config.ts` | 删除 | 新代码不再使用 inversify |
| `src/types.ts` | 删除 | 旧 DI 符号全部废弃 |
| 旧组件 `Student`、`Teacher`、`Course`、`Score`、`FamilyMember`、`DictionaryManage`、`MenuManage`、`StudentIntroduce`、`TeacherIntroduce`、`SystemIntroduce`、`Password` | 删除 | 与本项目无关 |
| `src/components/Navi` | 重写为 `src/layouts/BasicLayout.vue` | 保留并改造“账号切换”，增加二级菜单、Logo、头像、页脚 |
| 旧 `src/infrastructure/models/**` | 删除 | 由 `src/types/index.ts` 替代 |

### 2.3 模板中必须修掉的问题

1. `src/components/Navi/index.vue` 的 `onMounted` 里调用了 `logout()`，会导致刷新页面即退出登录。新布局必须删除这段逻辑。
2. `Navi` 顶部的“角色切换”下拉框是模板自带的演示功能，实验要求第 018 条明确要求保留账号切换。不能删除，必须改造为管理员（`admin`）/ 医生（`2022030001`）/ 患者（`2022030002`）三个演示账号，并把硬编码抽到 `src/config/demoAccounts.ts`。
3. `src/Login.vue` 默认填充 `admin/123456` 同样符合实验要求“缺省管理员账号和密码，点击登录直接进入系统”。新登录页必须保留默认值，不能清空。
4. `request-service-impl.ts` 只支持 POST，且请求体固定包一层 `{ data: ... }`。本项目是 RESTful，必须替换。
5. 登录后 `AppPresenter.enterApp` 会调用 `/api/base/getMenuList`。本项目菜单改为前端静态配置，不再依赖菜单表。
6. `vite.config.ts` 的 `build.outDir` 必须按课程要求指向后端 `java-server/src/main/resources/public`，不能只输出到 `dist`。详见 3.3 节。
7. `main.ts` 里把 `echarts` 挂到 `globalProperties`。可以保留，但图表组件统一直接 `import * as echarts from 'echarts'`，不要依赖全局变量。
8. 模板没有注册、忘记密码、重置密码、个人信息维护、二级菜单、系统 Logo、头像、版权页脚、项目/团队介绍页。这些是实验评分项，必须补齐，详见第 7 章。

### 2.4 目标目录结构

清理后按以下结构组织：

```
src/
├─ api/
│  ├─ auth.ts
│  ├─ user.ts
│  ├─ department.ts
│  ├─ doctor.ts
│  ├─ schedule.ts
│  ├─ appointment.ts
│  ├─ medicalRecord.ts
│  └─ statistics.ts
├─ components/
│  ├─ PageHeader.vue
│  ├─ StatusTag.vue
│  ├─ TimeSlotTag.vue
│  ├─ CaptchaField.vue
│  └─ charts/
│     ├─ LineChart.vue
│     ├─ PieChart.vue
│     └─ BarChart.vue
├─ config/
│  ├─ menu.ts
│  ├─ demoAccounts.ts
│  └─ team.ts
├─ layouts/
│  └─ BasicLayout.vue
├─ router/
│  └─ index.ts
├─ stores/
│  ├─ index.ts
│  └─ app.ts
├─ types/
│  └─ index.ts
├─ utils/
│  ├─ request.ts
│  └─ format.ts
├─ views/
│  ├─ Login.vue
│  ├─ Register.vue
│  ├─ ForgotPassword.vue
│  ├─ ResetPassword.vue
│  ├─ About.vue
│  ├─ Profile.vue
│  ├─ NotFound.vue
│  ├─ admin/
│  │  ├─ Dashboard.vue
│  │  ├─ Departments.vue
│  │  ├─ Doctors.vue
│  │  ├─ Schedules.vue
│  │  ├─ Appointments.vue
│  │  └─ Users.vue
│  ├─ doctor/
│  │  ├─ Dashboard.vue
│  │  ├─ Schedules.vue
│  │  └─ Appointments.vue
│  └─ patient/
│     ├─ Home.vue
│     ├─ Appointments.vue
│     └─ Records.vue
├─ App.vue
├─ main.ts
├─ components.d.ts（自动生成，不手改）
└─ env.d.ts
```

### 2.5 删除旧文件的安全做法

先创建新文件，再删除旧文件，避免中途无法构建。每删一个旧模块，同步清理：

- `src/router/index.ts` 中对它的 import
- `src/types.ts`、`src/inverfiy.config.ts` 中的绑定
- `src/components` 中对它的引用

如果担心误删，先把 `src` 整体复制到项目外备份，再执行删除。最终交付时 `src` 中不得残留旧业务模块。

---

## 3. 工程配置

### 3.1 启动与构建

```bash
cd web-vue3-ts-element-plus-css
npm install
npm run dev        # http://localhost:3000
npm run typecheck  # 必须通过
npm run build      # 必须通过
```

Node 版本建议 18 及以上，当前机器为 24.x，可直接使用。

### 3.2 `vite.config.ts` 调整（本地开发）

本地开发保留现有 `/api` 和 `/auth` 代理，目标为 `http://localhost:22222`：

```ts
server: {
  port: 3000,
  proxy: {
    "/api": { target: "http://localhost:22222", changeOrigin: true },
    "/auth": { target: "http://localhost:22222", changeOrigin: true },
  },
},
```

`changeOrigin: true` 建议加上，避免跨域调试问题。

### 3.3 课程要求的打包部署配置

《Web技术实验要求2026》要求前端构建产物复制到后端工程 `java-server/src/main/resources/public`，然后打 jar 上传。**最终提交前必须按以下配置修改 `vite.config.ts` 的 `build` 段**：

```ts
build: {
  // 相对前端工程根目录。等价于课程文档里的
  // \teach\server\java-server\src\main\resources\public
  outDir: "../java-server/src/main/resources/public",
  emptyOutDir: true, // 每次构建先清空后端 public，避免旧 hash 文件堆积
  assetsDir: "assets",
  cssCodeSplit: true,
  sourcemap: false,
  minify: "terser",
  terserOptions: {
    compress: {
      drop_console: true,
      drop_debugger: true,
    },
  },
  rollupOptions: {
    output: {
      chunkFileNames: "js/[name]-[hash].js",
      entryFileNames: "js/[name]-[hash].js",
      assetFileNames: "[ext]/[name]-[hash].[ext]",
    },
  },
},
```

部署流程：

1. 本地开发：代理保持 `http://localhost:22222`，执行 `npm run dev`。
2. 本地联调打包：执行 `npm run build`，产物自动进入后端 `src/main/resources/public`；启动后端 jar，访问 `http://localhost:22222`。
3. 在线上传前：把代理目标改为 `http://202.194.7.29:22222`（两处 `/api`、`/auth` 都要改），重新执行 `npm run build`，再执行 Maven 打包生成 `java-server-1.0-SNAPSHOT.jar`，上传 jar。
4. 课程要求代码压缩包中不要包含 `node_modules`、`dist`、`target`、jar 包；数据库文件与前、后端源码一起打成 zip。

注意：`emptyOutDir: true` 会让每次 `npm run build` 先清空后端 `public` 目录。后端同学需知晓，不要把手写文件放在该目录；所有静态资源统一放前端 `public/` 下由构建复制。

### 3.4 依赖

现有依赖已包含：

- `vue`、`vue-router`、`pinia`、`pinia-plugin-persistedstate`
- `axios`
- `element-plus`、`@element-plus/icons-vue`
- `echarts`
- `sass`、`unocss`、`typescript`、`vue-tsc`

不需要新增运行时依赖。`inversify` 可保留在 `package.json` 中但不再使用，也可以移除；不影响构建。

构建工具例外：课程要求 `minify: "terser"`，如果构建时报 `terser not found`，执行：

```bash
npm i -D terser
```

`terser` 只是构建期压缩工具，不属于新增 UI/状态/请求库。

---

## 4. 类型定义

新建 `src/types/index.ts`，内容如下。所有页面和 API 必须复用这些类型，不得重复定义。

```ts
// ---------- 通用 ----------
export interface ApiResponse<T = unknown> {
  code: number;
  msg: string;
  data: T;
}

export type Role = "ADMIN" | "DOCTOR" | "PATIENT";

/** 0 上午，1 下午 */
export type TimeSlot = 0 | 1;

/** 0 待诊，1 已诊，2 取消 */
export type AppointmentStatus = 0 | 1 | 2;

// ---------- 登录与用户 ----------
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  /** 后端字段名是 perName，含义是真实姓名 */
  perName: string;
  role: Role;
  serverName?: string;
}

export interface CurrentUser {
  /** 后端为 person_id */
  id: number;
  username: string;
  realName: string;
  email: string | null;
  role: Role;
}

export interface UserItem {
  /** 后端为 person_id */
  id: number;
  username: string;
  realName: string;
  email: string | null;
  role: Role;
}

export interface CreateUserRequest {
  username: string;
  password: string;
  realName: string;
  email?: string;
  role: "ADMIN" | "PATIENT";
}

export interface UpdateUserRequest {
  realName: string;
  email?: string;
  /** 不修改密码时传空字符串或不传 */
  newPassword?: string;
}

export interface UpdateProfileRequest {
  realName: string;
  email?: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

// ---------- 验证码 / 注册 / 重置密码 ----------
export interface CaptchaResponse {
  /** 后端生成的验证码 ID */
  validateCodeId: number;
  /** data:image/png;base64,... */
  img: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  realName: string;
  email?: string;
  validateCodeId: number;
  validateCode: string;
}

export interface ResetPasswordRequest {
  username: string;
  realName: string;
  newPassword: string;
  validateCodeId: number;
  validateCode: string;
}

// ---------- 科室 ----------
export interface Department {
  id: number;
  deptName: string;
  /** 该科室医生数，后端返回，用于删除前提示 */
  doctorCount?: number;
}

export interface DepartmentRequest {
  deptName: string;
}

// ---------- 医生 ----------
export interface Doctor {
  id: number;
  userId: number;
  username: string;
  realName: string;
  deptId: number;
  deptName: string;
  title: string;
  introduction: string;
}

export interface CreateDoctorRequest {
  username: string;
  password: string;
  realName: string;
  deptId: number;
  title: string;
  introduction: string;
}

export interface UpdateDoctorRequest {
  realName: string;
  deptId: number;
  title: string;
  introduction: string;
  /** 不修改密码时传空字符串或不传 */
  newPassword?: string;
}

// ---------- 排班 ----------
export interface Schedule {
  id: number;
  doctorId: number;
  doctorName: string;
  deptId: number;
  deptName: string;
  /** 医生职称 */
  title: string;
  /** yyyy-MM-dd */
  workDate: string;
  timeSlot: TimeSlot;
  totalQuota: number;
  remainQuota: number;
}

export interface CreateScheduleRequest {
  doctorId: number;
  workDate: string;
  timeSlot: TimeSlot;
  totalQuota: number;
}

export interface UpdateScheduleRequest {
  totalQuota: number;
}

export interface BatchScheduleRequest {
  doctorIds: number[];
  startDate: string;
  endDate: string;
  timeSlot: TimeSlot;
  totalQuota: number;
}

export interface BatchScheduleResult {
  createdCount: number;
  skipped: Array<{ doctorId: number; workDate: string; reason: string }>;
}

// ---------- 挂号 ----------
export interface Appointment {
  id: number;
  scheduleId: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  deptId: number;
  deptName: string;
  workDate: string;
  timeSlot: TimeSlot;
  status: AppointmentStatus;
  /** yyyy-MM-dd HH:mm:ss */
  createTime: string;
  hasRecord: boolean;
}

export interface AppointmentDetail extends Appointment {
  medicalRecord: MedicalRecord | null;
}

export interface CreateAppointmentRequest {
  scheduleId: number;
}

export interface CreateAppointmentResult {
  id: number;
  status: AppointmentStatus;
  remainQuota: number;
}

export interface CompleteAppointmentRequest {
  symptom: string;
  diagnosis: string;
  prescription: string;
}

export interface CompleteAppointmentResult {
  recordId: number;
}

// ---------- 病历 ----------
export interface MedicalRecord {
  id: number;
  appointmentId: number;
  symptom: string;
  diagnosis: string;
  prescription: string;
  createTime: string;
  doctorName: string;
  deptName: string;
  workDate: string;
  timeSlot: TimeSlot;
}

// ---------- 统计 ----------
export interface ChartPoint {
  date: string;
  count: number;
}

export interface DeptStat {
  deptName: string;
  count: number;
}

export interface DoctorStat {
  doctorId: number;
  doctorName: string;
  deptName: string;
  count: number;
}
```

---

## 5. 请求层与鉴权

### 5.1 统一响应约定

除登录接口外，所有新接口统一返回：

```json
{ "code": 0, "msg": "成功", "data": {} }
```

- `code = 0`：成功，前端直接使用 `data`。
- `code != 0`：业务失败，前端弹出 `msg`，不更新页面数据。
- HTTP 401/403：登录失效或无权限，清空登录态并跳转 `/login`。
- 登录接口 `POST /auth/login` 是例外，直接返回 `LoginResponse`，不包 `DataResponse`。

### 5.2 新建 `src/utils/request.ts`

替换旧 `request-service-impl.ts`。代码如下：

```ts
import axios from "axios";
import { ElMessage } from "element-plus";
import { useAppStore } from "~/stores/app";
import type { ApiResponse } from "~/types";

const service = axios.create({
  timeout: 15000,
});

service.interceptors.request.use((config) => {
  const store = useAppStore();
  if (store.token) {
    config.headers.Authorization = `Bearer ${store.token}`;
  }
  return config;
});

service.interceptors.response.use(
  (response) => {
    const body = response.data as ApiResponse<unknown> | unknown;

    // 登录接口是裸对象；新接口是 { code, msg, data }
    if (body && typeof body === "object" && "code" in body) {
      const res = body as ApiResponse<unknown>;
      if (res.code !== 0) {
        ElMessage.error(res.msg || "操作失败");
        return Promise.reject(new Error(res.msg || "操作失败"));
      }
      return res.data;
    }
    return body;
  },
  (error) => {
    const status = error.response?.status;
    const url: string = error.config?.url || "";
    const isAuthRequest = url.includes("/auth/");

    // 登录/注册/重置密码等公开接口的 401 不是登录态失效
    if (url.includes("/auth/login")) {
      ElMessage.error("用户名或密码错误");
      return Promise.reject(error);
    }

    if (isAuthRequest) {
      const msg = error.response?.data?.msg || "操作失败，请稍后重试";
      ElMessage.error(msg);
      return Promise.reject(error);
    }

    if (status === 401 || status === 403) {
      useAppStore().logout();
      ElMessage.error("登录已失效，请重新登录");
      window.location.hash = "#/login";
    } else {
      const msg =
        error.response?.data?.msg || error.message || "网络异常，请稍后重试";
      ElMessage.error(msg);
    }
    return Promise.reject(error);
  }
);

const http = {
  get<T>(url: string, params?: Record<string, unknown>) {
    return service.get(url, { params }) as unknown as Promise<T>;
  },
  post<T>(url: string, data?: unknown) {
    return service.post(url, data) as unknown as Promise<T>;
  },
  put<T>(url: string, data?: unknown) {
    return service.put(url, data) as unknown as Promise<T>;
  },
  delete<T>(url: string) {
    return service.delete(url) as unknown as Promise<T>;
  },
};

export default http;
```

说明：

- 不要在拦截器里直接 `import router`，避免与路由守卫形成循环依赖。用 `window.location.hash` 跳转即可。
- 业务错误统一弹 `ElMessage`，页面只需 `try/catch` 控制 loading 和关闭弹窗。
- 不要在每个页面重复写 axios。

### 5.3 重写 `src/stores/app.ts`

```ts
import { defineStore } from "pinia";
import type { CurrentUser, LoginResponse, Role } from "~/types";

export const useAppStore = defineStore("app", {
  state: () => ({
    token: "",
    user: null as CurrentUser | null,
  }),

  getters: {
    role: (state): Role | "" => state.user?.role ?? "",
    isLoggedIn: (state) => Boolean(state.token && state.user),
  },

  actions: {
    setLogin(res: LoginResponse) {
      this.token = res.token;
      // 模板后端可能返回 ROLE_ADMIN，这里做一次防御性归一化
      const role = String(res.role).replace(/^ROLE_/, "") as Role;
      this.user = {
        id: res.id,
        username: res.username,
        realName: res.perName || res.username,
        // 登录响应不包含 email，个人中心会调用 /api/users/me 补齐
        email: null,
        role,
      };
    },

    logout() {
      this.token = "";
      this.user = null;
    },
  },

  persist: {
    storage: localStorage,
    key: "hospital-app",
  },
});
```

要点：

- 持久化后刷新页面仍保持登录。
- 不再保存 `menuList`、`dataBaseUserName`、`passwordSave` 等模板字段。
- 不保存密码。

### 5.4 重写 `src/router/index.ts`

公开路由（无需登录）：`/login`、`/register`、`/forgot-password`、`/reset-password`、`/about`。
其余全部挂在 `BasicLayout` 下，需登录且按 `meta.roles` 校验角色。

```ts
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
    // 已登录用户访问登录/注册/找回密码时，直接回各自首页
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
```

### 5.5 登录页 `src/views/Login.vue`

> 实验要求原文：“提交的演示系统登录界面建议缺省管理员账号和密码，点击登录直接进入系统，系统右上角位置可设计账号切换功能，方便检查。”
> 因此本页**必须默认填充管理员账号密码**，这不是模板 bug，不要“修掉”。

要求：

- 居中卡片，复用 `public/login_pic.png` 背景，配合淡入动画。
- 字段：用户名、密码；**初始值 `admin / 123456`**，打开页面点“登录”即可进入。
- 不显示角色选择；登录页不启用验证码（保证一键演示）。验证码只用于注册、重置密码。
- 登录按钮有 `loading`，回车可提交。
- 登录成功：`store.setLogin(res)`，`router.replace(roleHome[res.role])`。
- 登录失败：显示“用户名或密码错误”，不要泄露账号是否存在。
- 底部提供三个入口：`注册账号` → `/register`，`忘记密码` → `/forgot-password`，`关于项目` → `/about`。
- 在卡片下方放一行演示账号提示（答辩方便），只展示不自动切换：`管理员 admin/123456`、`医生 2022030001/123456`、`患者 2022030002/123456`（以种子数据为准）。

登录 API 调用：

```ts
const form = reactive({ username: "admin", password: "123456" });

const res = await login({ username: form.username, password: form.password });
```

### 5.6 错误与登录失效

- 业务错误：拦截器已弹 `msg`，页面不要再重复弹。
- 受保护接口 401/403：拦截器清空登录态并跳转登录页。
- `/auth/**` 公开接口（登录、注册、重置密码、验证码）出错不触发退出登录，只提示后端 `msg`。
- 网络错误：提示“网络异常，请稍后重试”。
- 路由角色不匹配：守卫自动跳回当前角色首页，不显示 403 页面。

---

## 6. API 契约

### 6.0 通用约定

- 基地址：开发环境由 Vite 代理，前端写相对路径。
- 认证：除 `POST /auth/login` 外，所有请求必须带 `Authorization: Bearer <token>`。
- 字段命名：JSON 全部小驼峰；数据库字段用下划线，前端不感知。
- 日期：`workDate` 为 `yyyy-MM-dd`；`createTime` 为 `yyyy-MM-dd HH:mm:ss`。
- 列表接口不分页，一次性返回；课程作业数据量小。
- 排序：未特别说明时，按 id 升序；挂号、病历按 `createTime` 降序。
- 空列表返回 `[]`，不返回 `null`。

### 6.1 登录与个人

#### 6.1.1 登录

```
POST /auth/login
权限：公开
```

请求：

```json
{ "username": "2022030002", "password": "123456" }
```

成功响应（裸对象，不包 `code/msg/data`）：

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 3,
  "username": "2022030002",
  "perName": "张菊",
  "role": "PATIENT",
  "serverName": null
}
```

- `id` 是模板 `person.person_id`，不是 `user` 表的自增 ID。
- 后端必须把 Spring Security 的 `ROLE_ADMIN` / `ROLE_DOCTOR` / `ROLE_PATIENT` 归一化成 `ADMIN` / `DOCTOR` / `PATIENT` 再返回；前端 `Role` 类型不认 `ROLE_` 前缀。
- `perName` 来自 `person.name`，前端只负责展示。

失败：HTTP 401/403，或响应体含 `code != 0`。前端统一提示“用户名或密码错误”。

#### 6.1.2 获取验证码

```
POST /auth/getValidateCode
权限：公开
```

请求体：`{}`（模板接口需要 `{ "data": {} }` 也可，前端统一传空对象即可）。

返回 `DataResponse<CaptchaResponse>`：

```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "validateCodeId": 123456789,
    "img": "data:image/png;base64,/9j/4AAQSk..."
  }
}
```

- `img` 直接作为 `<img :src="captcha.img">` 使用。
- 验证码为 4 位数字，有效期由后端内存管理；前端点击图片重新获取。
- 注册、重置密码必须携带 `validateCodeId` 与用户输入的 `validateCode`。
- 后端校验成功后应使该验证码失效，防止重复使用（安全设计评分点）。

#### 6.1.3 用户注册

```
POST /auth/register
权限：公开
```

请求 `RegisterRequest`：

```json
{
  "username": "patient_li",
  "password": "123456",
  "realName": "李小红",
  "email": "li@example.com",
  "validateCodeId": 123456789,
  "validateCode": "1234"
}
```

- `email` 可选。
- 注册角色固定为 `PATIENT`，不允许前端指定角色。
- 用户名唯一，密码长度 ≥ 6。
- 后端实现：先 `INSERT person`（`num=username`、`name=realName`、`email`），再 `INSERT user`（`person_id`、`user_type_id=ROLE_PATIENT`、`user_name=username`、BCrypt 密码）；不创建 `student`/`teacher` 记录。
- 后端模板自带的 `/auth/registerUser` 会创建 `student` 记录，不适用于本项目；新增 `/auth/register` 按本节字段实现。

成功 `data`：`{ "id": 5 }`（新 `person_id`）。

失败：`用户名已存在`、`验证码错误或已失效`、`密码长度不能少于 6 位`。

#### 6.1.4 重置密码（忘记密码）

```
POST /auth/resetPassword
权限：公开
```

请求 `ResetPasswordRequest`：

```json
{
  "username": "patient_li",
  "realName": "李小红",
  "newPassword": "654321",
  "validateCodeId": 123456789,
  "validateCode": "1234"
}
```

- 用“用户名 + 真实姓名”做简易身份校验，避免任意改他人密码。
- 两项与库中记录不一致时统一返回 `用户信息不匹配`。
- 后端校验通过后更新 `user.password`（BCrypt），不修改 `person`。
- 重置成功后建议前端跳转登录页。

成功 `data` 为 `null`。

#### 6.1.5 当前用户信息

```
GET /api/users/me
权限：已登录
```

返回 `CurrentUser`：

```json
{ "id": 3, "username": "2022030002", "realName": "张菊", "email": null, "role": "PATIENT" }
```

- 后端查询 `user` + `person`，`id=person.person_id`，`realName=person.name`，`email=person.email`，`role` 由 `user_type.name` 去掉 `ROLE_` 前缀。

#### 6.1.6 修改个人资料

```
PUT /api/users/me
权限：已登录
```

请求 `UpdateProfileRequest`：

```json
{ "realName": "张菊", "email": "zhangju@example.com" }
```

- `realName` 必填；`email` 可空，空字符串按 `null` 处理。
- 只允许修改当前登录用户自己的资料，不接受 `role`、`username`。
- 后端更新 `person.name`、`person.email`，不修改 `user` 表中的账号和角色。

成功 `data` 为 `null`。前端成功后同步更新 Pinia 中的 `user.realName`、`user.email`。

#### 6.1.7 修改密码

```
PUT /api/users/me/password
权限：已登录
```

请求：

```json
{ "oldPassword": "123456", "newPassword": "654321" }
```

成功 `data` 为 `null`。失败示例：`原密码错误`、`新密码长度不能少于 6 位`。后端更新 `user.password`。

### 6.2 用户管理（ADMIN）

#### 6.2.1 用户列表

```
GET /api/admin/users?role=&keyword=
权限：ADMIN
```

- `role` 可选：`ADMIN`、`PATIENT`；为空返回管理员和患者账号。
- **本接口不返回医生账号**。医生账号统一在医生管理中维护，避免医生档案与账号信息不同步。
- `keyword` 可选：匹配用户名或真实姓名。

返回 `UserItem[]`：

```json
[
  { "id": 1, "username": "admin", "realName": "管理员", "email": null, "role": "ADMIN" },
  { "id": 3, "username": "2022030002", "realName": "张菊", "email": null, "role": "PATIENT" }
]
```

- 后端查询 `user` + `person` + `user_type`，`id` 返回 `person.person_id`。
- `role` 过滤按 `user_type.name`，返回前去掉 `ROLE_` 前缀。

#### 6.2.2 新增用户

```
POST /api/admin/users
权限：ADMIN
```

请求：

```json
{
  "username": "patient_li",
  "password": "123456",
  "realName": "李小红",
  "email": "li@example.com",
  "role": "PATIENT"
}
```

`role` 只允许 `ADMIN` 或 `PATIENT`；`email` 可选。医生账号请使用 6.4.3 的医生新增接口，一次创建账号和医生档案。

- 后端实现：`INSERT person` → `INSERT user`（`user_type_id` 对应 `ROLE_ADMIN` 或 `ROLE_PATIENT`）。
- 不需要创建 `student`/`teacher` 记录。

成功 `data`：`{ "id": 5 }`（新 `person_id`）。

失败：`用户名已存在`、`角色不合法`、`密码长度不能少于 6 位`。

#### 6.2.3 修改用户

```
PUT /api/admin/users/{id}
权限：ADMIN
```

请求：

```json
{ "realName": "李小红", "email": "li@example.com", "newPassword": "123456" }
```

- `newPassword` 为空或省略表示不修改密码。
- `email` 可选，空字符串按 `null` 处理。
- 不允许修改用户名和角色。
- 后端更新 `person.name`、`person.email`；`newPassword` 非空时更新 `user.password`。

成功 `data` 为 `null`。

#### 6.2.4 删除用户

```
DELETE /api/admin/users/{id}
权限：ADMIN
```

成功 `data` 为 `null`。

失败：

- `不能删除当前登录账号`
- `该用户存在关联数据，不能删除`

后端删除 `user` 记录；`person` 记录在没有其他业务引用时一并删除。

### 6.3 科室

#### 6.3.1 科室列表

```
GET /api/departments
权限：已登录
```

返回 `Department[]`，按 id 升序：

```json
[
  { "id": 1, "deptName": "内科", "doctorCount": 2 },
  { "id": 2, "deptName": "外科", "doctorCount": 1 }
]
```

#### 6.3.2 新增科室

```
POST /api/departments
权限：ADMIN
```

请求：`{ "deptName": "儿科" }`

成功 `data`：`{ "id": 3 }`

失败：`科室名称不能为空`、`科室名称已存在`。

#### 6.3.3 修改科室

```
PUT /api/departments/{id}
权限：ADMIN
```

请求：`{ "deptName": "儿科" }`

成功 `data` 为 `null`。

#### 6.3.4 删除科室

```
DELETE /api/departments/{id}
权限：ADMIN
```

成功 `data` 为 `null`。

失败：`该科室下还有医生，不能删除`。

### 6.4 医生

#### 6.4.1 医生列表

```
GET /api/doctors?deptId=&keyword=
权限：已登录
```

- `deptId` 可选，按科室过滤。
- `keyword` 可选，匹配医生姓名或登录名。

返回 `Doctor[]`：

```json
[
  {
    "id": 1,
    "userId": 2,
    "username": "2022030001",
    "realName": "杨平",
    "deptId": 1,
    "deptName": "内科",
    "title": "主任医师",
    "introduction": "擅长高血压、糖尿病等慢性病诊治。"
  }
]
```

#### 6.4.2 医生详情

```
GET /api/doctors/{id}
权限：已登录
```

返回单个 `Doctor`。不存在时 `code=1`，`msg="医生不存在"`。

#### 6.4.3 新增医生（同时创建登录账号）

```
POST /api/admin/doctors
权限：ADMIN
```

请求：

```json
{
  "username": "2022030001",
  "password": "123456",
  "realName": "杨平",
  "deptId": 1,
  "title": "主任医师",
  "introduction": "擅长高血压、糖尿病等慢性病诊治。"
}
```

- 后端实现：`INSERT person` → `INSERT user`（`user_type_id=ROLE_DOCTOR`）→ `INSERT t_doctor`（`user_id=person_id`）。
- `userId` 返回 `person_id`。

成功 `data`：

```json
{ "doctorId": 1, "userId": 2 }
```

失败：`用户名已存在`、`科室不存在`、`密码长度不能少于 6 位`。

#### 6.4.4 修改医生

```
PUT /api/admin/doctors/{id}
权限：ADMIN
```

请求：

```json
{
  "realName": "杨平",
  "deptId": 1,
  "title": "主任医师",
  "introduction": "擅长高血压、糖尿病等慢性病诊治。",
  "newPassword": ""
}
```

`newPassword` 为空或省略表示不修改登录密码；非空时更新 `user.password`。`realName` 更新 `person.name`。成功 `data` 为 `null`。

#### 6.4.5 删除医生

```
DELETE /api/admin/doctors/{id}
权限：ADMIN
```

成功 `data` 为 `null`。后端删除 `t_doctor` 记录和对应 `user` 记录；`person` 记录在没有其他引用时一并删除。

失败：

- `该医生还有排班或挂号记录，不能删除`
- `医生不存在`

### 6.5 排班

#### 6.5.1 排班列表

```
GET /api/schedules?date=&deptId=&doctorId=&timeSlot=
权限：已登录
```

参数：

- `date`：可选，`yyyy-MM-dd`。不传时返回今天起 7 天内。
- `deptId`：可选。
- `doctorId`：可选。
- `timeSlot`：可选，`0` 上午 / `1` 下午。

返回 `Schedule[]`，按 `workDate`、`timeSlot`、`doctorName` 升序：

```json
[
  {
    "id": 10,
    "doctorId": 1,
    "doctorName": "杨平",
    "deptId": 1,
    "deptName": "内科",
    "title": "主任医师",
    "workDate": "2026-10-30",
    "timeSlot": 0,
    "totalQuota": 20,
    "remainQuota": 15
  }
]
```

#### 6.5.2 排班详情

```
GET /api/schedules/{id}
权限：已登录
```

返回单个 `Schedule`。失败：`排班不存在`。

#### 6.5.3 医生查看自己的排班

```
GET /api/schedules/my?date=
权限：DOCTOR
```

- `date` 可选；不传返回今天起 7 天内。
- 返回 `Schedule[]`，只包含当前登录医生的排班。

#### 6.5.4 新增排班

```
POST /api/schedules
权限：ADMIN
```

请求：

```json
{
  "doctorId": 1,
  "workDate": "2026-10-30",
  "timeSlot": 0,
  "totalQuota": 20
}
```

成功 `data`：`{ "id": 10 }`。

失败：

- `医生不存在`
- `排班日期不能早于今天`
- `该医生当天该时段已排班`
- `总号数必须大于 0`

#### 6.5.5 修改总号数

```
PUT /api/schedules/{id}
权限：ADMIN
```

请求：`{ "totalQuota": 30 }`

成功 `data` 为 `null`。

失败：

- `排班不存在`
- `总号数不能小于已挂号数`
- `总号数必须大于 0`

后端会同步重算 `remainQuota = totalQuota - 已挂号数`（已挂号数统计 status 为 0 或 1 的记录）。

#### 6.5.6 删除排班

```
DELETE /api/schedules/{id}
权限：ADMIN
```

成功 `data` 为 `null`。

失败：`该排班已有挂号记录，不能删除`。

#### 6.5.7 批量排班（P1，可选）

```
POST /api/schedules/batch
权限：ADMIN
```

请求：

```json
{
  "doctorIds": [1, 2],
  "startDate": "2026-10-30",
  "endDate": "2026-11-05",
  "timeSlot": 0,
  "totalQuota": 20
}
```

返回：

```json
{
  "createdCount": 10,
  "skipped": [
    { "doctorId": 1, "workDate": "2026-10-30", "reason": "已存在排班" }
  ]
}
```

说明：如果后端未实现该接口，前端不得调用；管理员单条新增已能满足作业要求。默认按“未实现”处理，页面只提供单条新增。

### 6.6 挂号

#### 6.6.1 患者挂号

```
POST /api/appointments
权限：PATIENT
```

请求：`{ "scheduleId": 10 }`

成功 `data`：

```json
{ "id": 100, "status": 0, "remainQuota": 14 }
```

失败：

- `排班不存在`
- `该排班已过期`
- `号源已满`
- `您已挂过该号`

规则：

- 只允许挂今天及以后的排班。
- 同一患者对同一排班，已存在 status=0 或 1 的记录时，不能重复挂号。
- 已取消（status=2）后可以重新挂同一排班。

#### 6.6.2 患者查看自己的挂号

```
GET /api/appointments/my?status=
权限：PATIENT
```

- `status` 可选：`0` 待诊、`1` 已诊、`2` 取消；不传返回全部。
- 按 `createTime` 降序。

返回 `Appointment[]`：

```json
[
  {
    "id": 100,
    "scheduleId": 10,
    "patientId": 4,
    "patientName": "张菊",
    "doctorId": 1,
    "doctorName": "杨平",
    "deptId": 1,
    "deptName": "内科",
    "workDate": "2026-10-30",
    "timeSlot": 0,
    "status": 0,
    "createTime": "2026-10-29 21:30:00",
    "hasRecord": false
  }
]
```

#### 6.6.3 患者取消挂号

```
PUT /api/appointments/{id}/cancel
权限：PATIENT
```

请求体为空。成功 `data` 为 `null`。

失败：

- `挂号记录不存在`
- `只能取消待诊状态的挂号`
- `该挂号已就诊，不能取消`

规则：只有本人的、status=0 的挂号可以取消；取消后 `remainQuota + 1`，但不超过 `totalQuota`。

#### 6.6.4 医生查看自己的就诊列表

```
GET /api/appointments/doctor?status=&date=
权限：DOCTOR
```

- `status` 可选：`0` 待诊、`1` 已诊、`2` 取消；不传返回全部。
- `date` 可选，按排班日期过滤；不传返回全部。
- 按 `workDate` 升序、`timeSlot` 升序、`createTime` 升序。

返回 `Appointment[]`，包含患者姓名。

#### 6.6.5 医生接诊并提交病历

```
POST /api/appointments/{id}/complete
权限：DOCTOR
```

请求：

```json
{
  "symptom": "咳嗽、发热三天",
  "diagnosis": "上呼吸道感染",
  "prescription": "阿莫西林胶囊，一日三次，饭后服用"
}
```

成功 `data`：`{ "recordId": 50 }`

失败：

- `挂号记录不存在`
- `无权接诊该挂号`
- `该挂号已就诊，不能重复提交`
- `该挂号已取消，不能接诊`
- `症状、诊断、处方不能为空`

规则：只能接诊自己排班下的、status=0 的挂号；成功后写入 `t_medical_record` 并把挂号状态改为 1，必须在同一事务内完成。

#### 6.6.6 管理员查看全部挂号

```
GET /api/appointments?date=&deptId=&doctorId=&status=&keyword=
权限：ADMIN
```

- `date`：排班日期，`yyyy-MM-dd`。
- `deptId`、`doctorId`：可选。
- `status`：可选。
- `keyword`：可选，匹配患者姓名。

返回 `Appointment[]`，按 `createTime` 降序。

#### 6.6.7 挂号详情

```
GET /api/appointments/{id}
权限：ADMIN、该挂号所属医生、该挂号所属患者
```

返回 `AppointmentDetail`：

```json
{
  "id": 100,
  "scheduleId": 10,
  "patientId": 4,
  "patientName": "张菊",
  "doctorId": 1,
  "doctorName": "杨平",
  "deptId": 1,
  "deptName": "内科",
  "workDate": "2026-10-30",
  "timeSlot": 0,
  "status": 1,
  "createTime": "2026-10-29 21:30:00",
  "hasRecord": true,
  "medicalRecord": {
    "id": 50,
    "appointmentId": 100,
    "symptom": "咳嗽、发热三天",
    "diagnosis": "上呼吸道感染",
    "prescription": "阿莫西林胶囊，一日三次，饭后服用",
    "createTime": "2026-10-30 09:20:00",
    "doctorName": "杨平",
    "deptName": "内科",
    "workDate": "2026-10-30",
    "timeSlot": 0
  }
}
```

未就诊时 `medicalRecord` 为 `null`。

### 6.7 病历

#### 6.7.1 患者查看自己的病历列表

```
GET /api/medical-records/my
权限：PATIENT
```

返回 `MedicalRecord[]`，按 `createTime` 降序。

#### 6.7.2 按挂号单查看病历

```
GET /api/medical-records/appointment/{appointmentId}
权限：ADMIN、该挂号所属医生、该挂号所属患者
```

返回单个 `MedicalRecord`。失败：`病历不存在`、`无权查看该病历`。

### 6.8 统计（仅管理员）

#### 6.8.1 近 7 天挂号总量

```
GET /api/statistics/appointments/last-7-days
权限：ADMIN
```

返回 7 个点，日期升序，必须包含没有数据的日期（count=0）：

```json
[
  { "date": "2026-10-23", "count": 3 },
  { "date": "2026-10-24", "count": 5 },
  { "date": "2026-10-25", "count": 0 },
  { "date": "2026-10-26", "count": 8 },
  { "date": "2026-10-27", "count": 6 },
  { "date": "2026-10-28", "count": 4 },
  { "date": "2026-10-29", "count": 7 }
]
```

统计口径：按 `t_appointment.create_time` 的日期分组，排除 status=2（已取消）。

#### 6.8.2 各科室挂号人数占比

```
GET /api/statistics/departments
权限：ADMIN
```

返回：

```json
[
  { "deptName": "内科", "count": 18 },
  { "deptName": "外科", "count": 12 },
  { "deptName": "儿科", "count": 7 }
]
```

统计口径：挂号记录关联排班、医生、科室；排除 status=2；按 count 降序；只返回有挂号记录的科室。

#### 6.8.3 医生接诊量 Top5

```
GET /api/statistics/doctors/top5
权限：ADMIN
```

返回：

```json
[
  { "doctorId": 1, "doctorName": "杨平", "deptName": "内科", "count": 15 },
  { "doctorId": 2, "doctorName": "李娜", "deptName": "外科", "count": 11 }
]
```

统计口径：status=1（已诊）的挂号记录，按医生分组计数，降序，最多 5 条。

### 6.9 接口总表

| 序号 | 方法 | 路径 | 权限 | 用途 |
|---|---|---|---|---|
| 1 | POST | `/auth/login` | 公开 | 登录 |
| 2 | POST | `/auth/getValidateCode` | 公开 | 获取图形验证码 |
| 3 | POST | `/auth/register` | 公开 | 患者注册 |
| 4 | POST | `/auth/resetPassword` | 公开 | 重置密码 |
| 5 | GET | `/api/users/me` | 已登录 | 当前用户 |
| 6 | PUT | `/api/users/me` | 已登录 | 修改个人资料 |
| 7 | PUT | `/api/users/me/password` | 已登录 | 修改密码 |
| 8 | GET | `/api/admin/users` | ADMIN | 用户列表 |
| 9 | POST | `/api/admin/users` | ADMIN | 新增用户 |
| 10 | PUT | `/api/admin/users/{id}` | ADMIN | 修改用户 |
| 11 | DELETE | `/api/admin/users/{id}` | ADMIN | 删除用户 |
| 12 | GET | `/api/departments` | 已登录 | 科室列表 |
| 13 | POST | `/api/departments` | ADMIN | 新增科室 |
| 14 | PUT | `/api/departments/{id}` | ADMIN | 修改科室 |
| 15 | DELETE | `/api/departments/{id}` | ADMIN | 删除科室 |
| 16 | GET | `/api/doctors` | 已登录 | 医生列表 |
| 17 | GET | `/api/doctors/{id}` | 已登录 | 医生详情 |
| 18 | POST | `/api/admin/doctors` | ADMIN | 新增医生 |
| 19 | PUT | `/api/admin/doctors/{id}` | ADMIN | 修改医生 |
| 20 | DELETE | `/api/admin/doctors/{id}` | ADMIN | 删除医生 |
| 21 | GET | `/api/schedules` | 已登录 | 排班列表 |
| 22 | GET | `/api/schedules/{id}` | 已登录 | 排班详情 |
| 23 | GET | `/api/schedules/my` | DOCTOR | 我的排班 |
| 24 | POST | `/api/schedules` | ADMIN | 新增排班 |
| 25 | PUT | `/api/schedules/{id}` | ADMIN | 修改总号数 |
| 26 | DELETE | `/api/schedules/{id}` | ADMIN | 删除排班 |
| 27 | POST | `/api/schedules/batch` | ADMIN | 批量排班（P1） |
| 28 | POST | `/api/appointments` | PATIENT | 挂号 |
| 29 | GET | `/api/appointments/my` | PATIENT | 我的挂号 |
| 30 | PUT | `/api/appointments/{id}/cancel` | PATIENT | 取消挂号 |
| 31 | GET | `/api/appointments/doctor` | DOCTOR | 医生就诊列表 |
| 32 | POST | `/api/appointments/{id}/complete` | DOCTOR | 接诊并写病历 |
| 33 | GET | `/api/appointments` | ADMIN | 全部挂号 |
| 34 | GET | `/api/appointments/{id}` | 相关角色 | 挂号详情 |
| 35 | GET | `/api/medical-records/my` | PATIENT | 我的病历 |
| 36 | GET | `/api/medical-records/appointment/{appointmentId}` | 相关角色 | 按挂号查病历 |
| 37 | GET | `/api/statistics/appointments/last-7-days` | ADMIN | 近 7 天挂号量 |
| 38 | GET | `/api/statistics/departments` | ADMIN | 科室占比 |
| 39 | GET | `/api/statistics/doctors/top5` | ADMIN | 医生 Top5 |

### 6.10 API 函数清单（前端必须使用这些函数名）

`src/api/auth.ts`

```ts
login(data: LoginRequest): Promise<LoginResponse>
getValidateCode(): Promise<CaptchaResponse>
register(data: RegisterRequest): Promise<{ id: number }>
resetPassword(data: ResetPasswordRequest): Promise<null>
getMe(): Promise<CurrentUser>
updateProfile(data: UpdateProfileRequest): Promise<null>
changePassword(data: ChangePasswordRequest): Promise<null>
```

`src/api/user.ts`

```ts
listUsers(params?: { role?: "ADMIN" | "PATIENT"; keyword?: string }): Promise<UserItem[]>
createUser(data: CreateUserRequest): Promise<{ id: number }>
updateUser(id: number, data: UpdateUserRequest): Promise<null>
deleteUser(id: number): Promise<null>
```

`src/api/department.ts`

```ts
listDepartments(): Promise<Department[]>
createDepartment(data: DepartmentRequest): Promise<{ id: number }>
updateDepartment(id: number, data: DepartmentRequest): Promise<null>
deleteDepartment(id: number): Promise<null>
```

`src/api/doctor.ts`

```ts
listDoctors(params?: { deptId?: number; keyword?: string }): Promise<Doctor[]>
getDoctor(id: number): Promise<Doctor>
createDoctor(data: CreateDoctorRequest): Promise<{ doctorId: number; userId: number }>
updateDoctor(id: number, data: UpdateDoctorRequest): Promise<null>
deleteDoctor(id: number): Promise<null>
```

`src/api/schedule.ts`

```ts
listSchedules(params?: {
  date?: string;
  deptId?: number;
  doctorId?: number;
  timeSlot?: TimeSlot;
}): Promise<Schedule[]>
getSchedule(id: number): Promise<Schedule>
listMySchedules(params?: { date?: string }): Promise<Schedule[]>
createSchedule(data: CreateScheduleRequest): Promise<{ id: number }>
updateSchedule(id: number, data: UpdateScheduleRequest): Promise<null>
deleteSchedule(id: number): Promise<null>
// P1，后端未实现时不要调用
batchCreateSchedules(data: BatchScheduleRequest): Promise<BatchScheduleResult>
```

`src/api/appointment.ts`

```ts
createAppointment(data: CreateAppointmentRequest): Promise<CreateAppointmentResult>
listMyAppointments(params?: { status?: AppointmentStatus }): Promise<Appointment[]>
cancelAppointment(id: number): Promise<null>
listDoctorAppointments(params?: {
  status?: AppointmentStatus;
  date?: string;
}): Promise<Appointment[]>
completeAppointment(
  id: number,
  data: CompleteAppointmentRequest
): Promise<CompleteAppointmentResult>
listAppointments(params?: {
  date?: string;
  deptId?: number;
  doctorId?: number;
  status?: AppointmentStatus;
  keyword?: string;
}): Promise<Appointment[]>
getAppointment(id: number): Promise<AppointmentDetail>
```

`src/api/medicalRecord.ts`

```ts
listMyRecords(): Promise<MedicalRecord[]>
getRecordByAppointment(appointmentId: number): Promise<MedicalRecord>
```

`src/api/statistics.ts`

```ts
getLast7Days(): Promise<ChartPoint[]>
getDeptStats(): Promise<DeptStat[]>
getDoctorTop5(): Promise<DoctorStat[]>
```

---

## 7. 页面详细设计

### 7.0 整体布局

新建 `src/layouts/BasicLayout.vue`，采用经典三段式：顶部 Header + 左侧二级菜单 Aside + 右侧 `el-main`，底部版权页脚。

#### 7.0.1 Header（实验评分点：Logo、头像、账号切换）

- 左侧：系统 Logo `public/conlinkinfo.png`（`<img class="logo">`）+ 系统名称“校医院预约挂号系统”。
- 右侧依次为：
  1. **账号切换**下拉框（实验要求保留，放在右上角）。
  2. 当前用户头像 `public/user.png`（圆角）。
  3. 真实姓名 + 角色标签（管理员 / 医生 / 患者）。
  4. 下拉菜单：个人中心、关于项目、退出登录。
- **不得**在 `onMounted` 中调用 `logout()`（模板原有 bug，必须删除）。
- 不显示“数据库用户名”。

账号切换交互：

- 下拉项固定为三个演示账号：管理员 `admin`、医生 `2022030001`、患者 `2022030002`，统一密码 `123456`。
- 选中当前角色时不重复登录，只提示“当前已是该账号”。
- 选中其他角色时：`ElMessageBox.confirm("切换后当前登录状态将退出，是否继续？")` → 调 `login()` 获取新 token → `store.setLogin(res)` → `router.replace(roleHome[res.role])` → `ElMessage.success("已切换为XX账号")`。
- 切换失败则清空登录态回到 `/login`，不要保留旧 token。
- 演示账号集中放在 `src/config/demoAccounts.ts`，不要在模板里散落硬编码。

```ts
// src/config/demoAccounts.ts
import type { Role } from "~/types";

export interface DemoAccount {
  role: Role;
  label: string;
  username: string;
  password: string;
}

export const demoAccounts: DemoAccount[] = [
  { role: "ADMIN", label: "管理员", username: "admin", password: "123456" },
  { role: "DOCTOR", label: "医生", username: "2022030001", password: "123456" },
  { role: "PATIENT", label: "患者", username: "2022030002", password: "123456" },
];
```

> 账号以种子数据为准。若组员改了种子账号，只改这个文件。

#### 7.0.2 左侧二级菜单（实验评分点）

使用 `el-menu` + `el-sub-menu` 实现二级菜单。菜单按角色静态配置，不从后端拉取。

`src/config/menu.ts`：

```ts
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
```

- “关于项目”指向公开页 `/about`，页面内提供“返回系统”按钮（已登录时显示），点击回到各自首页。
- 所有角色在 Header 下拉中保留“个人中心” `/profile`，不占用一级菜单。
- 图标使用 Element Plus 图标组件名，通过 `<component :is="item.icon" />` 渲染。
- 当前路由高亮：`el-menu` 的 `:default-active="route.path"`，`router` 模式开启。
- 菜单折叠按钮可选，不做硬性要求。

#### 7.0.3 Footer（实验评分点）

- 固定在内容区底部（不是视口 fixed），文案：
  `© 2026 山东大学校医院预约挂号系统 · Web技术课程设计`
- 可加一行小字：`前端 Vue 3 + Element Plus · 后端 Spring Boot + MySQL`。
- 颜色使用次要文字色，不抢主内容。

#### 7.0.4 其他

- 右侧 Main：`<router-view />`，配合 `<transition name="fade">` 做路由切换动画。
- 退出登录：`ElMessageBox.confirm` 后清空 store，跳转 `/login`。
- 切换账号与退出登录必须清理 Pinia 持久化数据，避免旧 token 残留。

### 7.1 认证相关页面

#### 7.1.1 登录页

路径：`/login`

- 背景：`public/login_pic.png`。
- 卡片标题：“校医院预约挂号系统”，副标题“山东大学”。
- 表单：用户名、密码，**默认值 `admin / 123456`**（实验要求，打开即可一键登录）。
- 校验：两项必填；密码至少 6 位。
- 登录按钮：loading 状态，回车提交。
- 成功后按角色跳转。
- 卡片下方三个文字链接：`注册账号`、`忘记密码`、`关于项目`。
- 页面底部展示演示账号（只展示，不自动切换）：
  - 管理员：`admin / 123456`
  - 医生：`2022030001 / 123456`
  - 患者：`2022030002 / 123456`
- 登录页不启用验证码，保证答辩一键进入；验证码用于注册与重置密码。

#### 7.1.2 注册页

路径：`/register`（公开）

- 布局与登录页同一套背景与卡片风格，卡片稍宽（480px）。
- 字段：
  - 用户名（必填，4–20 位字母/数字/下划线）
  - 密码（必填，≥6 位）
  - 确认密码（必填，与密码一致）
  - 真实姓名（必填）
  - 邮箱（可选，`type="email"` 校验）
  - 验证码（必填，4 位数字）+ 右侧验证码图片，点击图片刷新
- 验证码加载：页面 `onMounted` 调 `getValidateCode()`，把 `validateCodeId` 存入表单隐藏字段，`img` 绑定到 `<img>`。
- 提交调 `register()`，成功后提示“注册成功，请登录”，`router.replace("/login")`，并保留用户名到登录页（可通过 query 或 store 临时字段，二选一）。
- 失败：`用户名已存在`、`验证码错误或已失效` 等直接展示；验证码错误后自动刷新验证码。
- 底部链接：`已有账号？去登录`。
- 注册角色固定为患者，页面上不出现角色选择。

#### 7.1.3 忘记密码页（UI 提示，无接口）

路径：`/forgot-password`（公开）

实验要求是“忘记密码（界面提示）”，本页不调用后端：

- 卡片标题“忘记密码”。
- 文案：
  - 方式一：携带学生证/校园卡到校医院信息中心重置。
  - 方式二：联系系统管理员（附占位邮箱 `admin@example.com`）。
  - 提示：系统为课程演示系统，不提供短信/邮件找回。
- 提供按钮：`去重置密码` → `/reset-password`；`返回登录` → `/login`。

#### 7.1.4 重置密码页

路径：`/reset-password`（公开）

- 字段：
  - 用户名（必填）
  - 真实姓名（必填，用于简易身份校验）
  - 新密码（必填，≥6 位）
  - 确认新密码（必填，一致）
  - 验证码（必填，4 位数字）+ 图片，点击刷新
- 提交调 `resetPassword()`，成功提示“密码重置成功，请使用新密码登录”，跳 `/login`。
- 失败：`用户信息不匹配`、`验证码错误或已失效`，并刷新验证码。
- 底部链接：`返回登录`。

### 7.2 管理员-数据看板

路径：`/admin/dashboard`

页面结构：

1. 顶部 `PageHeader`：标题“数据看板”，右侧“刷新”按钮。
2. 第一行：近 7 天挂号总量折线图，占满宽度，高度 320px。
3. 第二行：左侧科室占比饼图，右侧医生接诊量 Top5 横向柱状图，各占一半，高度 320px。

数据加载：

```ts
const [lineData, pieData, barData] = await Promise.all([
  getLast7Days(),
  getDeptStats(),
  getDoctorTop5(),
]);
```

图表映射：

- 折线图：xAxis 为 `date`，series 为 `count`，`smooth: true`，面积渐变可选。
- 饼图：`data = pieData.map(i => ({ name: i.deptName, value: i.count }))`，显示百分比。
- 柱状图：yAxis 为 `doctorName`，series 为 `count`；建议横向柱状图，tooltip 显示科室和接诊量。

状态：

- 加载中显示 `v-loading`。
- 某个接口返回空数组时，对应图表区域显示 `el-empty`，不要画空图。
- 三个接口互不影响，用 `Promise.allSettled` 也可以；至少保证一个失败不阻塞其他图表。

### 7.3 管理员-科室管理

路径：`/admin/departments`

- 顶部：标题 + “新增科室”按钮。
- 表格列：序号、科室名称、医生数、操作（编辑、删除）。
- 新增/编辑弹窗：科室名称，必填，最大 20 字。
- 删除：二次确认；后端返回“该科室下还有医生，不能删除”时直接展示。
- 加载中、空数据（`el-empty`）状态。
- 操作成功后刷新列表。

### 7.4 管理员-医生管理

路径：`/admin/doctors`

筛选栏：

- 科室下拉（全部科室）。
- 关键字输入（医生姓名/登录名）。
- 查询、重置按钮。

表格列：

- 序号、姓名、登录名、科室、职称、简介（`show-overflow-tooltip`）、操作（编辑、删除）。

新增医生弹窗字段：

- 登录名（必填，4-20 位，字母/数字/下划线）
- 密码（必填，至少 6 位）
- 真实姓名（必填）
- 科室（必填，下拉）
- 职称（必填，下拉可创建：主任医师、副主任医师、主治医师、住院医师）
- 简介（多行文本，最大 500 字，可空）

编辑医生弹窗：

- 真实姓名、科室、职称、简介。
- “重置密码”输入框可选，留空不修改。
- 登录名只读展示，不允许修改。

删除：

- 二次确认，提示“删除后该医生的登录账号也会被删除”。
- 后端拒绝时展示原因。

### 7.5 管理员-排班管理

路径：`/admin/schedules`

筛选栏：

- 日期（`el-date-picker`，`value-format="YYYY-MM-DD"`，默认今天）
- 科室下拉
- 医生下拉（随科室联动；未选科室时显示全部医生）
- 时段下拉（全部/上午/下午）
- 查询、重置

表格列：

- 日期、时段、科室、医生、职称、总号、剩余号、操作（编辑号数、删除）
- 剩余号显示：
  - `0`：`el-tag type="danger"` 已满
  - `1-3`：`el-tag type="warning"` 紧张
  - `>3`：`el-tag type="success"` 充足

新增排班弹窗：

- 医生（必填，下拉，显示“姓名 - 科室 - 职称”）
- 日期（必填，禁止选择今天之前的日期）
- 时段（必填，上午/下午）
- 总号数（必填，正整数，默认 20）

编辑号数弹窗：

- 展示已挂号数 = `totalQuota - remainQuota`。
- 输入新的总号数，必须大于等于已挂号数。
- 提交后刷新列表。

删除：

- 二次确认；后端返回“该排班已有挂号记录，不能删除”时展示。

批量排班：

- P1 可选，默认不实现。页面只提供单条新增。

### 7.6 管理员-挂号管理

路径：`/admin/appointments`

筛选栏：

- 日期（按排班日期）
- 科室
- 医生
- 状态（全部/待诊/已诊/取消）
- 患者姓名关键字
- 查询、重置

表格列：

- 挂号单号、患者、科室、医生、就诊日期、时段、状态、挂号时间、操作（详情）

状态标签：

- 0 待诊：`warning`
- 1 已诊：`success`
- 2 取消：`info`

详情弹窗：

- 展示挂号全部字段。
- `hasRecord=true` 时，调用 `GET /api/medical-records/appointment/{appointmentId}` 展示病历；否则显示“尚未就诊”。
- 管理员页面只读，不提供取消/接诊按钮。

### 7.7 管理员-用户管理

路径：`/admin/users`

筛选栏：

- 角色：全部 / 管理员 / 患者
- 关键字（用户名/真实姓名）
- 查询、重置

表格列：

- ID、用户名、真实姓名、邮箱、角色、操作（编辑、重置密码、删除）

新增用户弹窗：

- 用户名、密码、真实姓名、邮箱（可选）、角色（管理员/患者）
- 页面提示：“医生账号请在医生管理中新增，会同时创建登录账号和医生档案。”

编辑用户弹窗：

- 真实姓名
- 邮箱（可选，留空保存为 null）
- 新密码（可选，留空不修改）

删除：

- 二次确认。
- 后端拒绝时展示原因。
- 当前登录账号的删除按钮禁用，或点击后提示“不能删除当前登录账号”。

### 7.8 医生-工作台

路径：`/doctor/dashboard`

页面结构：

1. 三张统计卡片：
   - 今日待诊：`GET /api/appointments/doctor?date=今天&status=0` 的长度
   - 今日已诊：`GET /api/appointments/doctor?date=今天&status=1` 的长度
   - 今日排班：`GET /api/schedules/my?date=今天` 的长度
2. 今日待诊列表（最多 5 条），列：患者、时段、挂号时间、操作（接诊）。
3. 点击“接诊”复用 7.10 的接诊弹窗。

空状态：今日无排班/无待诊时显示 `el-empty`。

### 7.9 医生-我的排班

路径：`/doctor/schedules`

- 日期选择器，默认今天。
- 快捷按钮“今天”“明天”。
- 表格列：日期、时段、科室、总号、已挂号、剩余号。
- 已挂号 = `totalQuota - remainQuota`。
- 只读，不提供编辑/删除。

### 7.10 医生-就诊管理

路径：`/doctor/appointments`

顶部：

- 状态 Tabs：待诊（0）、已诊（1）、全部。
- 日期选择器（可选，清空表示全部日期）。
- 刷新按钮。

表格列：

- 患者、就诊日期、时段、状态、挂号时间、操作
- 待诊：操作“接诊”
- 已诊：操作“查看病历”

接诊弹窗：

- 只读信息：患者姓名、就诊日期、时段。
- 表单：
  - 症状（必填，多行，最大 500 字）
  - 诊断（必填，多行，最大 500 字）
  - 处方（必填，多行，最大 500 字）
- 提交按钮 loading，防止重复点击。
- 成功后：`ElMessage.success("接诊成功")`，关闭弹窗，刷新当前列表。
- 失败：展示后端 `msg`，弹窗保持打开，保留已填内容。

查看病历弹窗：

- 调用 `GET /api/medical-records/appointment/{appointmentId}`。
- 展示症状、诊断、处方、接诊时间。

### 7.11 患者-预约挂号

路径：`/patient/home`

推荐用三步式布局（也可用 `el-steps`）：

第一步：选择科室

- 调用 `GET /api/departments`。
- 卡片或下拉展示科室名称、医生数。
- 选中后加载医生。

第二步：选择医生

- 调用 `GET /api/doctors?deptId=当前科室`。
- 卡片展示：姓名、职称、简介。
- 提供“全部医生”选项（不按医生过滤）。
- 选中后加载排班。

第三步：选择日期与号源

- 日期快捷条：今天起 7 天，显示“今天/明天/周几 + MM-DD”。
- 也可用日期选择器，禁止选择今天之前的日期。
- 调用 `GET /api/schedules?deptId=&doctorId=&date=`。
- 表格列：医生、职称、时段、剩余号、操作（挂号）。
- 剩余号为 0：按钮禁用，显示“已满”。
- 排班已过期：不显示或按钮禁用。
- 点击“挂号”：弹出确认框，展示科室、医生、日期、时段、剩余号。
- 确认后调用 `POST /api/appointments`。
- 成功：提示“挂号成功”，跳转 `/patient/appointments`。
- 失败：展示后端 `msg`（号源已满、已挂过该号等），并刷新当前排班列表。

边界：

- 未选科室：不展示医生和排班，提示“请先选择科室”。
- 无医生/无排班：`el-empty`。
- 不显示 past 排班。

### 7.12 患者-我的挂号

路径：`/patient/appointments`

- Tabs：全部、待诊、已诊、已取消。
- 调用 `GET /api/appointments/my?status=`。
- 表格列：科室、医生、职称、就诊日期、时段、状态、挂号时间、操作。
- 状态标签同 7.6。
- 操作：
  - status=0：取消挂号
  - status=1：查看病历
  - status=2：无操作
- 取消：
  - 二次确认：“确认取消该挂号吗？取消后号源会释放。”
  - 调用 `PUT /api/appointments/{id}/cancel`。
  - 成功后刷新列表。
- 查看病历：
  - 调用 `GET /api/medical-records/appointment/{appointmentId}`，弹窗展示。
- 空状态：`el-empty`。

### 7.13 患者-我的病历

路径：`/patient/records`

- 调用 `GET /api/medical-records/my`。
- 表格列：就诊日期、时段、科室、医生、诊断、开单时间、操作（详情）。
- 详情弹窗：症状、诊断、处方、医生、科室、就诊日期、时段、记录时间。
- 处方使用多行文本展示，保留换行。
- 空状态：`el-empty`。

### 7.14 个人中心

路径：`/profile`

使用 `el-tabs` 或上下两张卡片，包含“基本资料”和“修改密码”两部分。

页面 `onMounted` 先调用 `getMe()` 获取最新资料（登录响应不含 `email`），并用返回值填充表单、同步 `store.user`。

基本资料：

- 用户名：只读。
- 角色：只读标签。
- 真实姓名：可编辑，必填。
- 邮箱：可编辑，可空，`type="email"` 校验。
- 保存调用 `PUT /api/users/me`（`UpdateProfileRequest`）。
- 成功后提示“资料已更新”，并同步 `store.user.realName`、`store.user.email`，Header 显示立即刷新。

修改密码：

- 原密码（必填）
- 新密码（必填，至少 6 位）
- 确认新密码（必填，必须与新密码一致）
- 提交调用 `PUT /api/users/me/password`。
- 成功后提示“密码修改成功，请重新登录”，清空 store，跳转 `/login`。
- 失败展示后端 `msg`。

### 7.15 关于项目 / 团队介绍页（实验评分点）

路径：`/about`（公开，未登录也可访问）

这是“Web 设计”评分项，必须有项目展示、团队与成员介绍、动效。

页面结构：

1. 顶部品牌区：系统 Logo `public/conlinkinfo.png`、项目名“校医院预约挂号系统”、一句简介、`进入系统` / `返回系统` 按钮（已登录按角色回首页，未登录去 `/login`）。
2. 项目简介：解决什么问题、三个角色能做什么、技术栈标签（Vue 3 / TypeScript / Element Plus / Spring Boot / MySQL / ECharts）。
3. 功能亮点：三张卡片（预约挂号、医生诊疗、数据统计），配 Element Plus 图标与 hover 上浮动画。
4. 系统截图轮播：使用 `public/app/img1.jpg` ~ `img4.jpg`，`el-carousel` 自动播放。
5. 团队介绍：成员卡片（头像用 `public/user.png` 占位），字段：姓名、学号、分工。人数按实际填写，至少 3 人。
6. 演示视频：`<video controls src="/app/video.mp4">`，宽度自适应，最大 720px。
7. 页脚：版权信息 + 返回顶部按钮。

动效要求（至少实现 3 种）：

- 首屏标题淡入上移（CSS `@keyframes`）。
- 卡片 hover 上浮 + 阴影加深。
- 滚动到可视区渐显（`IntersectionObserver` 加 `.visible` 类，或使用 `el-scrollbar` 不做也行）。
- 轮播自动切换。

注意：

- 图片、视频均放在 `public/app/` 下，引用路径写 `/app/img1.jpg`，不要写 `public/` 前缀。
- 页面不依赖登录态；未登录时隐藏“返回系统”，只显示“进入系统”。
- 团队成员信息集中放在 `src/config/team.ts`，方便组员改：

```ts
export interface TeamMember {
  name: string;
  studentId: string;
  duty: string;
  avatar?: string;
}

export const teamMembers: TeamMember[] = [
  { name: "成员A", studentId: "2022000001", duty: "后端接口与数据库" },
  { name: "成员B", studentId: "2022000002", duty: "前端页面与交互" },
  { name: "成员C", studentId: "2022000003", duty: "测试、部署与文档" },
];
```

### 7.16 404

路径：`/:pathMatch(.*)*`

- 显示“页面不存在”，提供“返回首页”按钮。
- 返回首页按当前角色跳转；未登录跳 `/login`。

---

## 8. 组件规范

### 8.1 `PageHeader.vue`

Props：

```ts
defineProps<{
  title: string;
  description?: string;
}>();
```

右侧操作按钮使用默认插槽。页面标题统一使用该组件，不要每页重复写样式。

### 8.2 `StatusTag.vue`

Props：`status: AppointmentStatus`

映射：

| status | 文案 | Element Plus type |
|---|---|---|
| 0 | 待诊 | warning |
| 1 | 已诊 | success |
| 2 | 已取消 | info |

### 8.3 `TimeSlotTag.vue`

Props：`timeSlot: TimeSlot`

映射：

| timeSlot | 文案 |
|---|---|
| 0 | 上午 |
| 1 | 下午 |

### 8.4 图表组件

新建 `src/components/charts/LineChart.vue`、`PieChart.vue`、`BarChart.vue`。要求：

- 每个组件接收 `data`，在 `onMounted` 初始化，`watch` 数据变化时 `setOption`。
- 监听 `window.resize`，调用 `chart.resize()`。
- `onBeforeUnmount` 移除监听并 `chart.dispose()`。
- 容器必须有明确高度（320px），否则图表不显示。
- 数据为空时由父组件显示 `el-empty`，图表组件不处理空态。

折线图示例：

```vue
<template>
  <div ref="el" style="width: 100%; height: 320px" />
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from "vue";
import * as echarts from "echarts";
import type { ChartPoint } from "~/types";

const props = defineProps<{ data: ChartPoint[] }>();
const el = ref<HTMLDivElement>();
let chart: echarts.ECharts | null = null;

const render = () => {
  if (!chart || !props.data.length) return;
  chart.setOption({
    tooltip: { trigger: "axis" },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: "category", data: props.data.map((i) => i.date) },
    yAxis: { type: "value", minInterval: 1 },
    series: [
      {
        type: "line",
        smooth: true,
        data: props.data.map((i) => i.count),
        areaStyle: { opacity: 0.15 },
      },
    ],
  });
};

const resize = () => chart?.resize();

onMounted(() => {
  if (el.value) {
    chart = echarts.init(el.value);
    render();
    window.addEventListener("resize", resize);
  }
});

watch(() => props.data, render, { deep: true });

onBeforeUnmount(() => {
  window.removeEventListener("resize", resize);
  chart?.dispose();
  chart = null;
});
</script>
```

饼图和柱状图按同样结构实现，不要引入 `vue-echarts`。

### 8.5 表单校验

- 必填项使用 `rules`，提交前 `await formRef.value?.validate()`。
- 密码至少 6 位。
- 总号数、科室名称等使用 `type: "number"` / `min`。
- 弹窗关闭时重置表单和校验状态。
- 提交按钮在请求期间设置 `loading`，防止重复提交。

### 8.6 空态、加载、确认

- 表格/列表加载：`v-loading="loading"`。
- 空数据：`<el-empty description="暂无数据" />`。
- 删除/取消/接诊提交：`ElMessageBox.confirm`。
- 成功提示：`ElMessage.success`。
- 业务失败：拦截器已提示，页面不要重复弹。
- 页面级请求失败时，保留上一次数据或显示空态，不要让页面白屏。

### 8.7 `CaptchaField.vue`（注册/重置密码共用）

Props / v-model：

```ts
const props = defineProps<{
  validateCodeId: number;
}>();
const emit = defineEmits<{
  (e: "update:validateCodeId", id: number): void;
}>();
```

行为：

- 暴露 `refresh()` 方法，页面在验证码错误后可主动刷新。
- 内部调 `getValidateCode()`，成功后 `emit("update:validateCodeId", res.validateCodeId)`。
- 左侧 4 位输入框（`maxlength=4`，只允许数字），右侧 120×40 的验证码图片，点击图片刷新。
- 加载时图片位置显示 `el-skeleton` 或“加载中”。
- 组件销毁无需清理。

注册页、重置密码页各放一个，不要复制粘贴两套逻辑。

---

## 9. 实施步骤（按 Wave 推进）

### Wave 0：清理与骨架

文件：

- `vite.config.ts`
- `src/App.vue`
- `src/main.ts`
- `src/router/index.ts`
- `src/stores/app.ts`
- `src/utils/request.ts`
- `src/types/index.ts`
- `src/views/Login.vue`
- `src/views/Register.vue`
- `src/views/ForgotPassword.vue`
- `src/views/ResetPassword.vue`
- `src/layouts/BasicLayout.vue`
- `src/config/menu.ts`
- `src/config/demoAccounts.ts`

任务：

1. 修改 `vite.config.ts` 的 `build.outDir` 和 proxy。
2. 新建类型、请求层、store、路由、布局、登录页、注册页、忘记密码页、重置密码页。
3. 删除或停用旧登录流程、旧菜单 API 调用；**改造而不是删除**右上角账号切换，改为三个演示账号。
4. 清理 `Navi` 的 `onMounted logout`。
5. 登录页默认填充 `admin / 123456`。

验收：

- 打开登录页直接点登录即可进入管理员首页。
- 三种角色都能登录并进入各自首页。
- 右上角账号切换可在三个演示账号间切换。
- 注册页验证码可刷新，注册成功后能登录。
- 重置密码页验证码可刷新，重置后新密码可登录。
- 刷新页面保持登录。
- 退出登录回到 `/login`。
- 直接访问无权限路由会跳回本角色首页。
- `npm run typecheck` 通过。

### Wave 1：API 层

文件：

- `src/api/auth.ts`
- `src/api/user.ts`
- `src/api/department.ts`
- `src/api/doctor.ts`
- `src/api/schedule.ts`
- `src/api/appointment.ts`
- `src/api/medicalRecord.ts`
- `src/api/statistics.ts`

任务：

- 按第 6 章实现所有函数，函数名和参数与本文一致。
- 所有函数返回 `Promise<T>`，T 是 `data` 的类型。
- 登录函数返回 `Promise<LoginResponse>`。

验收：

- `npm run typecheck` 通过。
- 每个函数都能在页面中直接调用，不需要额外转换。

### Wave 2：共享组件 + 管理员基础数据

文件：

- `src/components/PageHeader.vue`
- `src/components/StatusTag.vue`
- `src/components/TimeSlotTag.vue`
- `src/components/CaptchaField.vue`
- `src/views/admin/Departments.vue`
- `src/views/admin/Doctors.vue`

验收：

- 科室增删改查可用。
- 医生增删改查可用，新增医生后能用该账号登录。
- 删除有医生的科室被后端拒绝并正确提示。
- 表单校验、加载、空态齐全。

### Wave 3：管理员排班与用户

文件：

- `src/views/admin/Schedules.vue`
- `src/views/admin/Users.vue`

验收：

- 新增排班、修改总号数、删除排班可用。
- 剩余号显示正确。
- 用户新增、改名、重置密码、删除可用。
- 医生账号只能从医生管理创建。

### Wave 4：患者链路

文件：

- `src/views/patient/Home.vue`
- `src/views/patient/Appointments.vue`
- `src/views/patient/Records.vue`

验收：

- 科室 -> 医生 -> 日期/时段 -> 挂号完整走通。
- 号源为 0 时按钮禁用。
- 重复挂号、号源已满提示正确。
- 取消挂号后剩余号增加。
- 已诊挂号能查看病历。

### Wave 5：医生链路

文件：

- `src/views/doctor/Dashboard.vue`
- `src/views/doctor/Schedules.vue`
- `src/views/doctor/Appointments.vue`

验收：

- 工作台统计正确。
- 我的排班只显示当前医生。
- 待诊列表能接诊并提交病历。
- 提交后状态变为已诊，患者端能看到病历。
- 重复提交被后端拒绝并提示。

### Wave 6：管理员数据看板

文件：

- `src/views/admin/Dashboard.vue`
- `src/components/charts/LineChart.vue`
- `src/components/charts/PieChart.vue`
- `src/components/charts/BarChart.vue`

验收：

- 三个统计接口正常渲染。
- 空数据时显示空态而不是报错。
- 窗口缩放图表自适应。
- 切换路由后图表正确销毁，无内存泄漏。

### Wave 7：展示页、个人中心与收尾

任务：

- `src/views/About.vue` + `src/config/team.ts`：团队介绍、截图轮播、演示视频、CSS 动效。
- `src/views/Profile.vue`：基本资料（真实姓名、邮箱）+ 修改密码。
- `src/views/NotFound.vue`
- 全站样式统一、响应式检查。
- 检查 Logo、头像、版权页脚、二级菜单是否齐全。
- 删除所有旧业务代码和未使用 import。
- 按 3.3 节把 `build.outDir` 指到后端 `src/main/resources/public`，运行 `npm run typecheck`、`npm run build`。
- 若要求提交 jar：执行 Maven 打包并确认前端资源被打进 `static`/`public`。

验收：

- 构建通过，产物在后端 `src/main/resources/public`。
- 无 TypeScript 报错。
- 无控制台报错。
- 关于页图片、视频、动效正常。
- 个人资料修改后 Header 立即更新。
- 三个角色完整回归通过。

---

## 10. 联调与验收

### 10.1 测试账号

后端初始化数据应提供以下账号（密码均为 `123456`，BCrypt 存储）：

| 角色 | 用户名 | 真实姓名 | 说明 |
|---|---|---|---|
| ADMIN | `admin` | 管理员 | 全部管理权限 |
| DOCTOR | `2022030001` | 杨平 | 内科，主任医师 |
| DOCTOR | `2022030003` | 李娜 | 外科，副主任医师 |
| PATIENT | `2022030002` | 张菊 | 患者 |
| PATIENT | `2022030004` | 刘芳 | 患者 |

- 现有 `web-db.sql` 已包含 `admin`、`2022030001`、`2022030002` 三个 `user` 记录，密码哈希对应 `123456`；`2022030003`、`2022030004` 为新增种子。
- 2022030001/2022030002 当前是 `ROLE_STUDENT`，必须按附录 A.1 改为 `ROLE_DOCTOR`/`ROLE_PATIENT`。
- 登录页默认填充与右上角账号切换只使用前三个演示账号（`admin`、`2022030001`、`2022030002`），配置在 `src/config/demoAccounts.ts`。
- 其余账号以后端 seed 为准，前端不得在业务代码里硬编码。
- 如果后端最终 seed 的用户名不同，只改 `demoAccounts.ts` 与登录页提示，不要改接口。

### 10.2 建议的种子数据

- 认证数据：沿用 `web-db.sql` 的 `person` + `user`；先执行附录 A.1 的角色更新，新增账号同样按 `person` → `user` 两步插入。
- 用户：至少 1 名管理员、2 名医生、2 名患者；演示账号为 `admin`、`2022030001`、`2022030002`，密码统一 `123456`。
- 科室：内科、外科、儿科、口腔科、皮肤科。
- 医生：每个科室至少 1 名，共 5 名；医生登录名使用学号形式（如 `2022030001`）。
- 排班：未来 7 天，每位医生每天上午/下午各一条，总号 20。
- 挂号：至少 15 条，覆盖 status=0/1/2，分布在不同日期和科室，保证统计图有数据。
- 病历：至少 8 条，关联 status=1 的挂号。

### 10.3 关键测试用例

| 编号 | 场景 | 预期 |
|---|---|---|
| T01 | 管理员登录 | 进入 `/admin/dashboard` |
| T02 | 医生登录 | 进入 `/doctor/dashboard` |
| T03 | 患者登录 | 进入 `/patient/home` |
| T04 | 患者挂号 | 剩余号 -1，生成待诊记录 |
| T05 | 重复挂同一号 | 提示“您已挂过该号” |
| T06 | 挂满号排班 | 提示“号源已满” |
| T07 | 患者取消待诊 | 状态变取消，剩余号 +1 |
| T08 | 医生接诊 | 生成病历，状态变已诊 |
| T09 | 重复接诊 | 提示“该挂号已就诊，不能重复提交” |
| T10 | 患者查看病历 | 能看到症状/诊断/处方 |
| T11 | 删除有医生的科室 | 提示不能删除 |
| T12 | 删除有排班的医生 | 提示不能删除 |
| T13 | 删除有挂号的排班 | 提示不能删除 |
| T14 | 统计折线图 | 7 个点，日期连续 |
| T15 | 统计饼图 | 各科室数量与挂号数据一致 |
| T16 | Top5 柱状图 | 只统计已诊，最多 5 条 |
| T17 | 无 token 访问 `/admin/dashboard` | 跳转 `/login` |
| T18 | 患者访问管理员路由 | 跳回患者首页 |
| T19 | 刷新页面 | 登录态保持 |
| T20 | token 失效 | 401/403 后回登录页 |
| T21 | 打开登录页直接点登录 | 默认 `admin/123456`，进入管理员首页 |
| T22 | 右上角切换到医生账号 | 重新登录成功，进入 `/doctor/dashboard` |
| T23 | 注册新患者（验证码正确） | 注册成功，跳登录页，新账号可登录 |
| T24 | 注册时验证码错误 | 提示验证码错误，验证码自动刷新 |
| T25 | 忘记密码页 | 显示线下重置说明与入口，不报错 |
| T26 | 重置密码（用户名+姓名+验证码正确） | 重置成功，新密码可登录 |
| T27 | 重置密码信息不匹配 | 提示“用户信息不匹配”，不泄露账号是否存在 |
| T28 | 个人中心修改真实姓名/邮箱 | 保存成功，Header 立即更新，刷新后仍生效 |
| T29 | 关于项目页 | Logo、团队、截图轮播、视频、动效正常 |
| T30 | 布局检查 | 二级菜单可展开、Logo/头像/版权页脚齐全 |
| T31 | 执行 `npm run build` | 产物输出到 `java-server/src/main/resources/public` |

### 10.4 交付前自查清单

- [ ] `npm run typecheck` 通过。
- [ ] `npm run build` 通过。
- [ ] 三个角色都能登录、退出、刷新保持登录。
- [ ] 所有页面无控制台报错。
- [ ] 所有列表有加载态和空态。
- [ ] 所有删除/取消/接诊有二次确认。
- [ ] 所有表单有校验和提交 loading。
- [ ] 所有接口路径、字段名与本文一致。
- [ ] 旧模板业务代码已清理，无死代码引用。
- [ ] 登录页默认填充 `admin / 123456`，打开即可一键登录。
- [ ] 右上角账号切换可切换管理员/医生/患者三个演示账号。
- [ ] 布局有二级菜单、系统 Logo、个人头像、版权页脚；不显示数据库用户名。
- [ ] 注册、忘记密码、重置密码页面可用，验证码可点击刷新。
- [ ] 个人中心可修改真实姓名、邮箱、密码。
- [ ] 关于项目页包含项目介绍、团队成员、截图/视频、CSS 动效。
- [ ] `npm run build` 产物输出到 `java-server/src/main/resources/public`。
- [ ] 图表缩放正常，路由切换无报错。
- [ ] 401/403 能正确清空登录态。

---

## 11. 禁止事项与交付标准

禁止：

- 修改本文档定义的接口路径、字段名、枚举值。
- 在前端硬编码业务数据（科室、医生、排班）代替接口。
- 使用 `any` 绕过类型检查；确需 `any` 时必须在注释中说明原因。
- 直接操作 DOM 修改 Element Plus 内部结构。
- 在组件中直接 `axios.get/post`，必须走 `src/api`。
- 在页面中保存密码、token 到非 Pinia 持久化之外的地方。
- 引入新的依赖或升级 Element Plus / Vue 大版本。
- 删除后端代码或修改数据库。
- **删除或禁用登录页默认管理员账号密码**（实验要求）。
- **删除或隐藏右上角账号切换**（实验要求）。
- 把账号切换写成装饰性 UI 却不真正重新登录。

交付标准：

- 代码可运行、可构建、类型检查通过。
- 页面与第 7 章一致，接口与第 6 章一致。
- 三端主流程完整可用。
- 登录页一键进入、右上角三角色切换可用。
- 二级菜单、Logo、头像、版权页脚、关于项目页齐全。
- 代码结构清晰，无旧模板残留。
- 附一份简短的 README 更新，说明启动方式、测试账号、已完成页面、构建产物位置。

---

## 附录 A：数据库结构与迁移（以 `web-db.sql` 为准）

> 课程搭建文档要求把 `web-db.sql` 导入团队数据库（本项目为 `java_1_03`）。认证直接复用模板的 `person`、`user`、`user_type`，**不新建 `t_user`**。前端只关心接口字段，不感知表名。

### A.1 认证表（沿用模板）

`web-db.sql` 已包含：

- `person`：`person_id`、`type`、`num`、`name`、`email` 等；
- `user`：`person_id`、`user_type_id`、`user_name`、`password`、`create_time` 等；
- `user_type`：`id`、`name`，当前为 `ROLE_ADMIN`、`ROLE_STUDENT`、`ROLE_TEACHER`。

登录继续走 Spring Security + JWT，`UserDetailsServiceImpl` 仍然查 `user`。需要执行的数据调整：

```sql
INSERT INTO user_type (id, name) VALUES (4, 'ROLE_DOCTOR'), (5, 'ROLE_PATIENT');

UPDATE `user` SET user_type_id = 4 WHERE user_name = '2022030001';
UPDATE `user` SET user_type_id = 5 WHERE user_name = '2022030002';
-- admin 保持 user_type_id = 1
```

- `person.person_id` 就是接口里的 `userId` / `patientId`。
- `person.name` 对应 `realName`，`person.email` 对应 `email`。
- `user.password` 是 BCrypt；现有演示账号的哈希对应 `123456`，不要重复加密。
- 新增用户/医生：先 `INSERT person`，再 `INSERT user`（`person_id` 相同）；不创建 `student`/`teacher` 记录。
- 注册固定 `ROLE_PATIENT`。
- 删除用户/医生：删除 `user`；`person` 在没有其他业务引用时一并删除。
- 登录返回角色前，把 `ROLE_` 前缀去掉（`ROLE_DOCTOR` → `DOCTOR`）。

### A.2 业务表（新增 5 张）

```sql
CREATE TABLE t_department (
  id INT PRIMARY KEY AUTO_INCREMENT,
  dept_name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_doctor (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL UNIQUE,
  dept_id INT NOT NULL,
  title VARCHAR(50) NOT NULL,
  introduction VARCHAR(500),
  CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES `user`(person_id),
  CONSTRAINT fk_doctor_dept FOREIGN KEY (dept_id) REFERENCES t_department(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_schedule (
  id INT PRIMARY KEY AUTO_INCREMENT,
  doctor_id INT NOT NULL,
  work_date DATE NOT NULL,
  time_slot TINYINT NOT NULL COMMENT '0上午 1下午',
  total_quota INT NOT NULL,
  remain_quota INT NOT NULL,
  UNIQUE KEY uk_doctor_date_slot (doctor_id, work_date, time_slot),
  CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES t_doctor(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_appointment (
  id INT PRIMARY KEY AUTO_INCREMENT,
  patient_id INT NOT NULL,
  schedule_id INT NOT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0待诊 1已诊 2取消',
  create_time DATETIME NOT NULL,
  KEY idx_appointment_patient (patient_id),
  KEY idx_appointment_schedule (schedule_id),
  CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES `user`(person_id),
  CONSTRAINT fk_appointment_schedule FOREIGN KEY (schedule_id) REFERENCES t_schedule(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE t_medical_record (
  id INT PRIMARY KEY AUTO_INCREMENT,
  appointment_id INT NOT NULL UNIQUE,
  symptom VARCHAR(500) NOT NULL,
  diagnosis VARCHAR(500) NOT NULL,
  prescription VARCHAR(500) NOT NULL,
  create_time DATETIME NOT NULL,
  CONSTRAINT fk_record_appointment FOREIGN KEY (appointment_id) REFERENCES t_appointment(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### A.3 演示数据建议

现有 `person` 中：`1=管理员`、`2=2022030001 杨平`、`3=2022030002 张菊`。新增科室和医生档案：

```sql
INSERT INTO t_department (dept_name) VALUES ('内科'), ('外科'), ('儿科'), ('口腔科'), ('皮肤科');

INSERT INTO t_doctor (user_id, dept_id, title, introduction)
VALUES (2, 1, '主任医师', '擅长高血压、糖尿病等慢性病诊治。');
```

排班、挂号、病历按 10.2 节生成即可。`t_doctor.id` 是医生档案主键，排班外键用它，不是 `user_id`。

---

## 附录 B：状态与格式化工具

新建 `src/utils/format.ts`：

```ts
import type { AppointmentStatus, TimeSlot } from "~/types";

export const timeSlotText = (slot: TimeSlot) => (slot === 0 ? "上午" : "下午");

export const statusText = (status: AppointmentStatus) => {
  if (status === 0) return "待诊";
  if (status === 1) return "已诊";
  return "已取消";
};

export const statusTagType = (status: AppointmentStatus) => {
  if (status === 0) return "warning";
  if (status === 1) return "success";
  return "info";
};

export const isFull = (remainQuota: number) => remainQuota <= 0;
```

所有页面统一使用这些工具，不要重复写三目表达式。

---

## 附录 C：旧模板文件处理清单

必须删除或替换：

```
src/Login.vue                         -> 删除，新建 src/views/Login.vue
src/App.vue                           -> 重写为 <router-view />
src/router/index.ts                   -> 重写
src/stores/app.ts                     -> 重写
src/types.ts                          -> 删除
src/inverfiy.config.ts                -> 删除
src/domain/**                         -> 全部删除
src/infrastructure/**                 -> 全部删除
src/components/Navi/**                -> 替换为 src/layouts/BasicLayout.vue
src/components/Student/**             -> 删除
src/components/Teacher/**             -> 删除
src/components/Course/**              -> 删除
src/components/Score/**               -> 删除
src/components/FamilyMember/**        -> 删除
src/components/DictionaryManage/**    -> 删除
src/components/MenuManage/**          -> 删除
src/components/StudentIntroduce/**    -> 删除
src/components/TeacherIntroduce/**    -> 删除
src/components/SystemIntroduce/**     -> 删除
src/components/Password/**            -> 替换为 src/views/Profile.vue
```

必须新建：

```
src/views/Register.vue                -> 患者注册（含验证码）
src/views/ForgotPassword.vue          -> 忘记密码提示页
src/views/ResetPassword.vue           -> 验证码重置密码
src/views/About.vue                   -> 项目/团队介绍页
src/config/demoAccounts.ts            -> 三个演示账号
src/config/menu.ts                    -> 三角色二级菜单
src/config/team.ts                    -> 团队成员信息
```

可以保留：

```
src/components/MainPage/**            -> 可删除，新的管理员看板替代
src/composables/**                    -> 未使用可删除
src/styles/**                         -> 保留
public/login_pic.png                  -> 保留（登录/注册背景）
public/conlinkinfo.png                -> 保留（Header Logo）
public/user.png                       -> 保留（头像占位）
public/app/img1.jpg ~ img4.jpg        -> 保留（关于页轮播）
public/app/video.mp4                  -> 保留（关于页演示视频）
```

清理完成后，`src` 中不应再出现 `inversify`、`Presenter`、`StudentService`、`CourseService`、`MenuInfo` 等旧业务符号。

---

## 附录 D：后端返回示例汇总

成功：

```json
{ "code": 0, "msg": "成功", "data": [] }
```

业务失败：

```json
{ "code": 1, "msg": "号源已满", "data": null }
```

登录失败：

```json
{ "code": 1, "msg": "用户名或密码错误", "data": null }
```

未登录/无权限：HTTP 401 或 403，响应体可能不是统一格式；前端按 HTTP 状态处理。

---

## 附录 E：实验要求 → 实现位置对照

| 实验要求 | 实现位置 | 验收点 |
|---|---|---|
| 登录界面缺省管理员账号密码，点击直接进入 | `views/Login.vue`，默认 `admin/123456` | 打开登录页直接点登录进管理员首页 |
| 右上角账号切换 | `layouts/BasicLayout.vue` + `config/demoAccounts.ts` | 三角色切换会真正重新登录并跳对应首页 |
| 用户注册 | `views/Register.vue` + `POST /auth/register` | 注册后可用新账号登录 |
| 忘记密码（界面提示） | `views/ForgotPassword.vue` | 展示线下/管理员重置说明 |
| 重置密码（验证码） | `views/ResetPassword.vue` + `POST /auth/resetPassword` | 验证码正确且信息匹配时重置成功 |
| 用户信息维护（改密码、个人信息、邮箱） | `views/Profile.vue` + `PUT /api/users/me`、`PUT /api/users/me/password` | 姓名/邮箱/密码可修改并生效 |
| 角色与权限设置 | `router` 的 `meta.roles` + Header 角色标签 + 管理员用户管理 | 越权路由自动跳回本角色首页 |
| 二级菜单导航 | `layouts/BasicLayout.vue` + `config/menu.ts` | 一级分组可展开，子菜单可跳转 |
| 系统 Logo | Header `public/conlinkinfo.png` | 每个登录后页面可见 |
| 个人头像 | Header `public/user.png` | 每个登录后页面可见 |
| 版权页脚 | `BasicLayout` Footer | 每个登录后页面可见 |
| 项目展示、团队与成员介绍 | `views/About.vue` + `config/team.ts` | 项目简介、成员卡片、截图、视频 |
| CSS/多媒体动画 | `About.vue`、登录页、路由过渡、卡片 hover | 至少 3 处动效 |
| 数据统计与可视化 | `views/admin/Dashboard.vue` + ECharts | 折线/饼图/柱状图正常 |
| 至少 3 个业务模块 CRUD | 科室、医生、排班、挂号、用户 | 增删改查 + 业务流完整 |
| 输入校验 | 所有表单 `rules` + 后端校验 | 必填、长度、邮箱、验证码 |
| 刷新后会话保持 | Pinia 持久化 + 路由守卫 | F5 后仍登录 |
| 验证码 | 注册、重置密码 | 图片可刷新，错误有提示 |
| 后端端口 22222 / 前端代理 | `vite.config.ts` | 开发环境 `/api`、`/auth` 代理到 22222 |
| 前端构建进后端 public | `vite.config.ts` 的 `build.outDir` | 产物出现在 `java-server/src/main/resources/public` |
| 线上代理 `202.194.7.29:22222` | 部署前修改 proxy | 构建前确认 |
| 代码压缩与去 console | `build.minify: "terser"` + `drop_console` | 构建配置生效 |

---

**文档结束。前端如有疑问，先对照第 6 章接口契约；仍无法确定时，停止编码并向后端确认，不要自行发明接口。**
