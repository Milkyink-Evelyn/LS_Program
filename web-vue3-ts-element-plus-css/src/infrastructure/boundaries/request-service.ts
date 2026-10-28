import { LoginRequest, JwtResponse } from "../models/login";
export interface IRequestService {
  getAuthHeader(): any
  generalRequest(url: string, data: any | null): Promise<any>
  jwtRequest(
    jwt: string,
    url: string,
    data: any | null
  ): Promise<any>
  uploadRequest(
    url: string,
    data: any | null
  ): Promise<any>
  downloadPost(url: string, label: string, data: any): Promise<any>
  getPdfData(url: string, data: any): Promise<string>
  doExport(url: string, fileName: string, data: any): Promise<any>
}