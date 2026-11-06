package cn.edu.sdu.java.server.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 新增 / 修改科室。
 */
@Getter
@Setter
public class DepartmentRequest {

    @NotBlank(message = "科室名称不能为空")
    @Size(max = 50, message = "科室名称长度不能超过 50 位")
    private String deptName;
}
