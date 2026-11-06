package cn.edu.sdu.java.server.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 新增医生：同时创建登录账号。
 */
@Getter
@Setter
public class DoctorCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 20, message = "用户名长度不能超过 20 位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 60, message = "密码长度不能少于 6 位")
    private String password;

    @NotBlank(message = "医生姓名不能为空")
    @Size(max = 50, message = "医生姓名长度不能超过 50 位")
    private String realName;

    @NotNull(message = "科室不能为空")
    private Integer deptId;

    @NotBlank(message = "职称不能为空")
    @Size(max = 50, message = "职称长度不能超过 50 位")
    private String title;

    @Size(max = 500, message = "简介长度不能超过 500 位")
    private String introduction;
}
