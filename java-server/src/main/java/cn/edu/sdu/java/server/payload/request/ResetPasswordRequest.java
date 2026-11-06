package cn.edu.sdu.java.server.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 忘记密码重置请求：用“用户名 + 真实姓名”做简易身份校验。
 */
@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 60, message = "新密码长度不能少于 6 位")
    private String newPassword;

    @NotNull(message = "验证码不能为空")
    private Integer validateCodeId;

    @NotBlank(message = "验证码不能为空")
    private String validateCode;
}
