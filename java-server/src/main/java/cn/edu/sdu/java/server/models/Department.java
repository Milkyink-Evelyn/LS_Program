package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 科室表 t_department
 */
@Getter
@Setter
@Entity
@Table(name = "t_department")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "dept_name", nullable = false, unique = true, length = 50)
    private String deptName;
}
