import { LoginRequest, JwtResponse } from "../models/login";
export interface ILoginService {
    login(loginRequest: LoginRequest): Promise<JwtResponse>;
}