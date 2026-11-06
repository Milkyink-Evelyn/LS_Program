import http from "~/utils/request";
import type { Department, DepartmentRequest } from "~/types";

export const listDepartments = () =>
  http.get<Department[]>("/api/departments");

export const createDepartment = (data: DepartmentRequest) =>
  http.post<{ id: number }>("/api/departments", data);

export const updateDepartment = (id: number, data: DepartmentRequest) =>
  http.put<null>(`/api/departments/${id}`, data);

export const deleteDepartment = (id: number) =>
  http.delete<null>(`/api/departments/${id}`);
