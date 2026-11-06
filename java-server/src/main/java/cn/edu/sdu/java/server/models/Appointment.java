package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 挂号表 t_appointment
 * status：0 待诊，1 已诊，2 已取消
 */
@Getter
@Setter
@Entity
@Table(name = "t_appointment")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @Column(name = "schedule_id", nullable = false)
    private Integer scheduleId;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
}
