import http from "~/utils/request";
import type {
  CaptchaResponse,
  ChangePasswordRequest,
  CurrentUser,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  ResetPasswordRequest,
  UpdateProfileRequest,
} from "~/types";

export const login = (data: LoginRequest) =>
  http.post<LoginResponse>("/auth/login", data);

export const getValidateCode = () =>
  http.post<CaptchaResponse>("/auth/getValidateCode", {});

export const register = (data: RegisterRequest) =>
  http.post<{ id: number }>("/auth/register", data);

export const resetPassword = (data: ResetPasswordRequest) =>
  http.post<null>("/auth/resetPassword", data);

export const getMe = () => http.get<CurrentUser>("/api/users/me");

export const updateProfile = (data: UpdateProfileRequest) =>
  http.put<null>("/api/users/me", data);

export const changePassword = (data: ChangePasswordRequest) =>
  http.put<null>("/api/users/me/password", data);
