import http from "~/utils/request";
import type { CreateUserRequest, UpdateUserRequest, UserItem } from "~/types";

export const listUsers = (params?: {
  role?: "ADMIN" | "PATIENT";
  keyword?: string;
}) => http.get<UserItem[]>("/api/admin/users", params);

export const createUser = (data: CreateUserRequest) =>
  http.post<{ id: number }>("/api/admin/users", data);

export const updateUser = (id: number, data: UpdateUserRequest) =>
  http.put<null>(`/api/admin/users/${id}`, data);

export const deleteUser = (id: number) =>
  http.delete<null>(`/api/admin/users/${id}`);
