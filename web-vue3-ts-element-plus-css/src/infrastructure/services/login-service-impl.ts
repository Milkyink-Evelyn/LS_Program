import axios from "axios";
import { ILoginService } from "../boundaries/login-service";
import { LoginRequest, JwtResponse } from "../models/login";
export class LoginServiceImpl implements ILoginService {
  async login(loginRequest: LoginRequest): Promise<JwtResponse> {
    try {
      const res = await axios.post("/auth/login", loginRequest);
      //等待从服务器返还
      if (
        res.status == 200 &&
        res.data.token != null &&
        res.data.token != "" &&
        res.data.token != undefined
      ) {
        return res.data;
      } else {
        throw new Error("登录错误");
      }
    } catch (error: any) {
      throw error;
    }
  }
}
