package cn.edu.sdu.java.server.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 修改当前登录用户个人资料。
 */
@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过 50 位")
    private String realName;

    @Email(message = "邮箱格式不正确")
    @Size(max = 60, message = "邮箱长度不能超过 60 位")
    private String email;
}
