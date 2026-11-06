package cn.edu.sdu.java.server.models;

/**
 *  EUserType 用户类型的枚举类型 对应 user_type 表里数据
 *  ROLE_ADMIN 管理员
 *  ROLE_STUDENT 学生（模板保留）
 *  ROLE_TEACHER 教师（模板保留）
 *  ROLE_DOCTOR 医生
 *  ROLE_PATIENT 患者
 */
public enum EUserType {
    ROLE_ADMIN,
    ROLE_STUDENT,
    ROLE_TEACHER,
    ROLE_DOCTOR,
    ROLE_PATIENT
}
