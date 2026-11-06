package cn.edu.sdu.java.server.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 修改排班总号数。
 */
@Getter
@Setter
public class ScheduleUpdateRequest {

    @NotNull(message = "总号数必须大于 0")
    @Min(value = 1, message = "总号数必须大于 0")
    private Integer totalQuota;
}
