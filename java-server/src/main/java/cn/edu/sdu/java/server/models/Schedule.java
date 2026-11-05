package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 排班表 t_schedule
 * timeSlot：0 上午，1 下午
 */
@Getter
@Setter
@Entity
@Table(name = "t_schedule",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"doctor_id", "work_date", "time_slot"})
        })
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "doctor_id", nullable = false)
    private Integer doctorId;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "time_slot", nullable = false)
    private Integer timeSlot;

    @Column(name = "total_quota", nullable = false)
    private Integer totalQuota;

    @Column(name = "remain_quota", nullable = false)
    private Integer remainQuota;
}
