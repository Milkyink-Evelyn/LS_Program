package cn.edu.sdu.java.server.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 患者挂号。
 */
@Getter
@Setter
public class AppointmentCreateRequest {

    @NotNull(message = "排班不能为空")
    private Integer scheduleId;
}
