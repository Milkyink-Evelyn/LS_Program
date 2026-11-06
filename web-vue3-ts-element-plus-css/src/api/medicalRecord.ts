import http from "~/utils/request";
import type { MedicalRecord } from "~/types";

export const listMyRecords = (params?: { date?: string }) =>
  http.get<MedicalRecord[]>("/api/medical-records/my", params);

export const getRecordByAppointment = (appointmentId: number) =>
  http.get<MedicalRecord>(
    `/api/medical-records/appointment/${appointmentId}`
  );
