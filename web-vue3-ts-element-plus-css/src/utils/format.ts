import type { AppointmentStatus, TimeSlot } from "~/types";

export const timeSlotText = (slot: TimeSlot) =>
  slot === 0 ? "上午" : "下午";

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

export const roleText = (role: string) => {
  if (role === "ADMIN") return "管理员";
  if (role === "DOCTOR") return "医生";
  if (role === "PATIENT") return "患者";
  return role;
};

export const todayString = () => {
  const d = new Date();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${d.getFullYear()}-${m}-${day}`;
};

export const dateString = (date: Date) => {
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${date.getFullYear()}-${m}-${day}`;
};

export const addDays = (base: Date, days: number) => {
  const d = new Date(base);
  d.setDate(d.getDate() + days);
  return d;
};

export const weekdayText = (date: string) => {
  const names = ["日", "一", "二", "三", "四", "五", "六"];
  return "周" + names[new Date(date + "T00:00:00").getDay()];
};
