import type { Role } from "~/types";

export interface DemoAccount {
  role: Role;
  label: string;
  username: string;
  password: string;
}

export const demoAccounts: DemoAccount[] = [
  { role: "ADMIN", label: "管理员", username: "admin", password: "123456" },
  { role: "DOCTOR", label: "医生", username: "2022030001", password: "123456" },
  { role: "PATIENT", label: "患者", username: "2022030002", password: "123456" },
];
