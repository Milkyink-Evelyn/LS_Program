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
