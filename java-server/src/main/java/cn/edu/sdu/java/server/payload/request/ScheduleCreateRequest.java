package cn.edu.sdu.java.server.payload.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 新增排班。
 */
@Getter
@Setter
public class ScheduleCreateRequest {

    @NotNull(message = "医生不能为空")
    private Integer doctorId;

    @NotNull(message = "排班日期不能为空")
    private LocalDate workDate;

    @NotNull(message = "时段不能为空")
    @Min(value = 0, message = "时段只能是 0 或 1")
    @Max(value = 1, message = "时段只能是 0 或 1")
    private Integer timeSlot;

    @NotNull(message = "总号数必须大于 0")
    @Min(value = 1, message = "总号数必须大于 0")
    private Integer totalQuota;
}
