import http from "~/utils/request";
import type { ChartPoint, DeptStat, DoctorStat } from "~/types";

export const getLast7Days = () =>
  http.get<ChartPoint[]>("/api/statistics/appointments/last-7-days");

export const getDeptStats = () =>
  http.get<DeptStat[]>("/api/statistics/departments");

export const getDoctorTop5 = () =>
  http.get<DoctorStat[]>("/api/statistics/doctors/top5");
