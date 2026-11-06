package cn.edu.sdu.java.server.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 患者注册请求。
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 20, message = "用户名长度不能超过 20 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 60, message = "密码长度不能少于 6 位")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过 50 位")
    private String realName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 60, message = "邮箱长度不能超过 60 位")
    private String email;

    @NotNull(message = "验证码不能为空")
    private Integer validateCodeId;

    @NotBlank(message = "验证码不能为空")
    private String validateCode;
}
