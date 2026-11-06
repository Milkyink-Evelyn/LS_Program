import http from "~/utils/request";
import type {
  BatchScheduleRequest,
  BatchScheduleResult,
  CreateScheduleRequest,
  Schedule,
  TimeSlot,
  UpdateScheduleRequest,
} from "~/types";

export const listSchedules = (params?: {
  date?: string;
  deptId?: number;
  doctorId?: number;
  timeSlot?: TimeSlot;
}) => http.get<Schedule[]>("/api/schedules", params);

export const getSchedule = (id: number) =>
  http.get<Schedule>(`/api/schedules/${id}`);

export const listMySchedules = (params?: { date?: string }) =>
  http.get<Schedule[]>("/api/schedules/my", params);

export const createSchedule = (data: CreateScheduleRequest) =>
  http.post<{ id: number }>("/api/schedules", data);

export const updateSchedule = (id: number, data: UpdateScheduleRequest) =>
  http.put<null>(`/api/schedules/${id}`, data);

export const deleteSchedule = (id: number) =>
  http.delete<null>(`/api/schedules/${id}`);

// P1，后端未实现时不要调用
export const batchCreateSchedules = (data: BatchScheduleRequest) =>
  http.post<BatchScheduleResult>("/api/schedules/batch", data);
