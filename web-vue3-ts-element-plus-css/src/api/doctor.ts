import http from "~/utils/request";
import type {
  CreateDoctorRequest,
  Doctor,
  UpdateDoctorRequest,
} from "~/types";

export const listDoctors = (params?: {
  deptId?: number;
  keyword?: string;
}) => http.get<Doctor[]>("/api/doctors", params);

export const getDoctor = (id: number) =>
  http.get<Doctor>(`/api/doctors/${id}`);

export const createDoctor = (data: CreateDoctorRequest) =>
  http.post<{ doctorId: number; userId: number }>("/api/admin/doctors", data);

export const updateDoctor = (id: number, data: UpdateDoctorRequest) =>
  http.put<null>(`/api/admin/doctors/${id}`, data);

export const deleteDoctor = (id: number) =>
  http.delete<null>(`/api/admin/doctors/${id}`);
