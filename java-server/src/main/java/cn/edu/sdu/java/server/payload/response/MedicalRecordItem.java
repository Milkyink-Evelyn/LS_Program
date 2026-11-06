package cn.edu.sdu.java.server.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 病历列表项 / 详情。
 */
@Getter
@Setter
public class MedicalRecordItem {

    private Integer id;
    private Integer appointmentId;
    private String symptom;
    private String diagnosis;
    private String prescription;
    private String createTime;
    private String doctorName;
    private String deptName;
    private LocalDate workDate;
    private Integer timeSlot;

    public MedicalRecordItem() {
    }
}
