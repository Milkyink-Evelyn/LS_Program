package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 病历表 t_medical_record
 * appointmentId 与 t_appointment.id 一对一
 */
@Getter
@Setter
@Entity
@Table(name = "t_medical_record")
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "appointment_id", nullable = false, unique = true)
    private Integer appointmentId;

    @Column(nullable = false, length = 500)
    private String symptom;

    @Column(nullable = false, length = 500)
    private String diagnosis;

    @Column(nullable = false, length = 500)
    private String prescription;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;
}
