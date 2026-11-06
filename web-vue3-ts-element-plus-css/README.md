# 山东大学校医院 · 预约挂号系统（前端）

Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router + ECharts。

本项目只包含前端。后端为 Spring Boot 3.4 + Spring Data JPA + MySQL，端口 `22222`。所有科室、医生、排班、挂号、病历和统计都通过接口获取，前端不预置业务数据，也不直接访问数据库。

## 启动

```bash
npm install
npm run dev
```

打开 `http://localhost:3000`。开发环境 Vite 已把 `/auth` 和 `/api` 代理到 `http://localhost:22222`。

## 演示账号

| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | `admin` | `123456` |
| 医生 | `2022030001` | `123456` |
| 患者 | `2022030002` | `123456` |

登录页默认填充管理员账号，点击登录即可进入；右上角“切换账号”会真正重新调用登录接口，不是装饰性 UI。

## 页面

- 公开：登录、患者注册、忘记密码、重置密码、关于项目
- 管理员：数据看板、科室管理、医生管理、排班管理、挂号管理、用户管理
- 医生：工作台、我的排班、就诊管理
- 患者：预约挂号、我的挂号、我的病历
- 通用：个人中心、404

所有列表都有加载态和空态；删除、取消、接诊都有二次确认。

## 接口约定

接口路径、字段名、枚举值以 `docs/frontend-handoff.md` 为准：

- `code = 0` 表示成功，前端直接使用 `data`
- 登录接口 `POST /auth/login` 直接返回裸对象
- 其余接口统一返回 `{ code, msg, data }`
- 受保护接口 401/403 会清空登录态并回到登录页
- 角色值为 `ADMIN` / `DOCTOR` / `PATIENT`

前端请求统一在 `src/api/`，不要在页面中直接使用 axios。

## 构建

```bash
npm run typecheck
npm run build
```

`vite.config.ts` 按课程要求把产物输出到：

```text
../java-server/src/main/resources/public
```

本地只想检查构建、不写后端目录时：

```bash
npx vite build --outDir dist --emptyOutDir
```

构建后访问后端 `http://localhost:22222`。

## 设计

- 登录/注册：山东大学校园门户风格，校徽 + 校园背景 + 白色卡片 + 深红按钮
- 内部页面：深色侧栏 + 米色内容区，圆角卡片、大标题，参考课程提供的 Dashboard 视觉
- 登录页不包含额外助手脚本或弹窗提示
- 校徽来自山东大学官网，校园背景取自课程参考图，仅用于课程教学演示，不代表学校官方系统
- 团队成员信息是占位内容，请在 `src/config/team.ts` 替换为真实姓名、学号和分工

## 目录

```text
src/
├─ api/           接口函数
├─ components/    通用组件和图表
├─ config/        菜单、演示账号、团队
├─ layouts/       BasicLayout
├─ router/        三角色路由与守卫
├─ stores/        Pinia 登录态
├─ styles/        全局样式
├─ types/         接口类型
├─ utils/         请求层与格式化
└─ views/         admin / doctor / patient / 认证页面
```

## 说明

- 不修改后端代码，不修改数据库。
- 所有删除、取消、接诊操作必须经过二次确认。
- 业务数据为空时显示空态，不显示前端伪造数据。
- 当前工程原模板为课程示例；本目录已按交接文档重构为校医院预约挂号系统。

