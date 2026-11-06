package cn.edu.sdu.java.server.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 排班列表项 / 详情。
 */
@Getter
@Setter
public class ScheduleItem {

    private Integer id;
    private Integer doctorId;
    private String doctorName;
    private Integer deptId;
    private String deptName;
    private String title;
    private LocalDate workDate;
    private Integer timeSlot;
    private Integer totalQuota;
    private Integer remainQuota;

    public ScheduleItem() {
    }

    public ScheduleItem(Integer id, Integer doctorId, String doctorName,
                        Integer deptId, String deptName, String title,
                        LocalDate workDate, Integer timeSlot,
                        Integer totalQuota, Integer remainQuota) {
        this.id = id;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.deptId = deptId;
        this.deptName = deptName;
        this.title = title;
        this.workDate = workDate;
        this.timeSlot = timeSlot;
        this.totalQuota = totalQuota;
        this.remainQuota = remainQuota;
    }
}
