-- ============================================================
-- 校医院预约挂号系统 建表脚本
-- 目标库：java_1_03
-- 前置条件：已导入 web-db.sql，person / user / user_type 三张表存在
-- 执行方式：在 MySQL 客户端中整段执行；脚本可重复执行
-- 说明：认证沿用模板表，本脚本只新增 5 张业务表并准备演示数据
-- ============================================================

SET NAMES utf8mb4;

-- ------------------------------------------------------------
-- 1. 角色准备
-- ------------------------------------------------------------
INSERT IGNORE INTO user_type (id, name) VALUES (4, 'ROLE_DOCTOR'), (5, 'ROLE_PATIENT');

-- 2022030001 杨平 -> 医生；2022030002 张菊 -> 患者；admin 保持管理员
UPDATE `user` SET user_type_id = 4 WHERE user_name = '2022030001';
UPDATE `user` SET user_type_id = 5 WHERE user_name = '2022030002';

-- ------------------------------------------------------------
-- 2. 业务表
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS t_department (
  id INT PRIMARY KEY AUTO_INCREMENT,
  dept_name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_doctor (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL UNIQUE COMMENT '对应 user.person_id',
  dept_id INT NOT NULL,
  title VARCHAR(50) NOT NULL,
  introduction VARCHAR(500),
  CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES `user`(person_id),
  CONSTRAINT fk_doctor_dept FOREIGN KEY (dept_id) REFERENCES t_department(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_schedule (
  id INT PRIMARY KEY AUTO_INCREMENT,
  doctor_id INT NOT NULL COMMENT '对应 t_doctor.id',
  work_date DATE NOT NULL,
  time_slot TINYINT NOT NULL COMMENT '0上午 1下午',
  total_quota INT NOT NULL,
  remain_quota INT NOT NULL,
  UNIQUE KEY uk_doctor_date_slot (doctor_id, work_date, time_slot),
  CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES t_doctor(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_appointment (
  id INT PRIMARY KEY AUTO_INCREMENT,
  patient_id INT NOT NULL COMMENT '对应 user.person_id',
  schedule_id INT NOT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0待诊 1已诊 2取消',
  create_time DATETIME NOT NULL,
  KEY idx_appointment_patient (patient_id),
  KEY idx_appointment_schedule (schedule_id),
  CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES `user`(person_id),
  CONSTRAINT fk_appointment_schedule FOREIGN KEY (schedule_id) REFERENCES t_schedule(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS t_medical_record (
  id INT PRIMARY KEY AUTO_INCREMENT,
  appointment_id INT NOT NULL UNIQUE,
  symptom VARCHAR(500) NOT NULL,
  diagnosis VARCHAR(500) NOT NULL,
  prescription VARCHAR(500) NOT NULL,
  create_time DATETIME NOT NULL,
  CONSTRAINT fk_record_appointment FOREIGN KEY (appointment_id) REFERENCES t_appointment(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- 3. 演示科室
-- ------------------------------------------------------------
INSERT IGNORE INTO t_department (dept_name) VALUES
('内科'), ('外科'), ('儿科'), ('口腔科'), ('皮肤科');

-- ------------------------------------------------------------
-- 4. 演示账号（密码均为 123456，复用模板 BCrypt 哈希）
-- 2022030003 李娜 -> 医生；2022030004 刘芳 -> 患者
-- ------------------------------------------------------------
INSERT IGNORE INTO person (type, num, name, dept, gender, email)
VALUES ('2', '2022030003', '李娜', '校医院', '2', 'lina@sdu.edu.cn');

INSERT IGNORE INTO person (type, num, name, dept, gender, email)
VALUES ('1', '2022030004', '刘芳', '软件学院', '2', 'liufang@sdu.edu.cn');

INSERT IGNORE INTO `user` (person_id, user_type_id, user_name, password, create_time, creator_id)
SELECT p.person_id, 4, p.num,
       '$2a$10$FV5lm..jdQWmV7hFguxKDeTrGyiWg1u6HYD2QiQc0tRROrNtSQVOy',
       DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 1
FROM person p
WHERE p.num = '2022030003';

INSERT IGNORE INTO `user` (person_id, user_type_id, user_name, password, create_time, creator_id)
SELECT p.person_id, 5, p.num,
       '$2a$10$FV5lm..jdQWmV7hFguxKDeTrGyiWg1u6HYD2QiQc0tRROrNtSQVOy',
       DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:%s'), 1
FROM person p
WHERE p.num = '2022030004';

-- ------------------------------------------------------------
-- 5. 医生档案
-- ------------------------------------------------------------
INSERT IGNORE INTO t_doctor (user_id, dept_id, title, introduction)
SELECT p.person_id, d.id, '主任医师', '擅长高血压、糖尿病等慢性病诊治。'
FROM person p
JOIN t_department d ON d.dept_name = '内科'
WHERE p.num = '2022030001';

INSERT IGNORE INTO t_doctor (user_id, dept_id, title, introduction)
SELECT p.person_id, d.id, '副主任医师', '擅长普外科常见病、多发病的诊治。'
FROM person p
JOIN t_department d ON d.dept_name = '外科'
WHERE p.num = '2022030003';

-- ------------------------------------------------------------
-- 6. 演示排班：两位医生未来 3 天，上午/下午各 20 个号
--    重复执行时由 uk_doctor_date_slot 自动忽略
-- ------------------------------------------------------------
INSERT IGNORE INTO t_schedule (doctor_id, work_date, time_slot, total_quota, remain_quota)
SELECT d.id, DATE_ADD(CURDATE(), INTERVAL t.n DAY), t.slot, 20, 20
FROM t_doctor d
CROSS JOIN (
    SELECT 1 AS n, 0 AS slot UNION ALL
    SELECT 1, 1 UNION ALL
    SELECT 2, 0 UNION ALL
    SELECT 2, 1 UNION ALL
    SELECT 3, 0 UNION ALL
    SELECT 3, 1
) t
WHERE d.user_id IN (
    SELECT person_id FROM person WHERE num IN ('2022030001', '2022030003')
);

-- ------------------------------------------------------------
-- 7. 执行后自检（可单独运行）
-- ------------------------------------------------------------
-- SELECT * FROM user_type;
-- SELECT u.user_name, u.user_type_id, p.name FROM `user` u JOIN person p ON p.person_id = u.person_id;
-- SELECT d.id, p.name, dept.dept_name, d.title FROM t_doctor d
--   JOIN person p ON p.person_id = d.user_id
--   JOIN t_department dept ON dept.id = d.dept_id;
-- SELECT * FROM t_schedule ORDER BY work_date, time_slot;
