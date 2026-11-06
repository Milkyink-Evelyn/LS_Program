import http from "~/utils/request";
import type {
  Appointment,
  AppointmentDetail,
  AppointmentStatus,
  CompleteAppointmentRequest,
  CompleteAppointmentResult,
  CreateAppointmentRequest,
  CreateAppointmentResult,
} from "~/types";

export const createAppointment = (data: CreateAppointmentRequest) =>
  http.post<CreateAppointmentResult>("/api/appointments", data);

export const listMyAppointments = (params?: {
  status?: AppointmentStatus;
  date?: string;
}) => http.get<Appointment[]>("/api/appointments/my", params);

export const cancelAppointment = (id: number) =>
  http.put<null>(`/api/appointments/${id}/cancel`);

export const listDoctorAppointments = (params?: {
  status?: AppointmentStatus;
  date?: string;
}) => http.get<Appointment[]>("/api/appointments/doctor", params);

export const completeAppointment = (
  id: number,
  data: CompleteAppointmentRequest
) =>
  http.post<CompleteAppointmentResult>(
    `/api/appointments/${id}/complete`,
    data
  );

export const listAppointments = (params?: {
  date?: string;
  deptId?: number;
  doctorId?: number;
  status?: AppointmentStatus;
  keyword?: string;
}) => http.get<Appointment[]>("/api/appointments", params);

export const getAppointment = (id: number) =>
  http.get<AppointmentDetail>(`/api/appointments/${id}`);
