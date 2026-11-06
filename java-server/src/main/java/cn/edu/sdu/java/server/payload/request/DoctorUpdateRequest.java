package cn.edu.sdu.java.server.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 修改医生：newPassword 为空表示不改登录密码。
 */
@Getter
@Setter
public class DoctorUpdateRequest {

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

    private String newPassword;
}
