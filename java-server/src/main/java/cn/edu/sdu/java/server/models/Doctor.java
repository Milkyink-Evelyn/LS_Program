package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 医生档案表 t_doctor
 * userId 对应 user.person_id，deptId 对应 t_department.id
 */
@Getter
@Setter
@Entity
@Table(name = "t_doctor")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @Column(name = "dept_id", nullable = false)
    private Integer deptId;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(length = 500)
    private String introduction;
}
