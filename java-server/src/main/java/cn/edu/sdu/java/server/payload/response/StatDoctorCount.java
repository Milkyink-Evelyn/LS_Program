package cn.edu.sdu.java.server.payload.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 医生接诊量统计，用于管理员 Top5。
 */
@Getter
@Setter
public class StatDoctorCount {

    private Integer doctorId;
    private String doctorName;
    private String deptName;
    private Long count;

    public StatDoctorCount() {
    }

    public StatDoctorCount(Integer doctorId, String doctorName, String deptName, Long count) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.deptName = deptName;
        this.count = count;
    }
}
