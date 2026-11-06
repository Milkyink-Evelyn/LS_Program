package cn.edu.sdu.java.server.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 挂号列表项。
 */
@Getter
@Setter
public class AppointmentItem {

    private Integer id;
    private Integer scheduleId;
    private Integer patientId;
    private String patientName;
    private Integer doctorId;
    private String doctorName;
    private Integer deptId;
    private String deptName;
    private LocalDate workDate;
    private Integer timeSlot;
    private Integer status;
    private String createTime;
    private Boolean hasRecord;

    public AppointmentItem() {
    }
}
