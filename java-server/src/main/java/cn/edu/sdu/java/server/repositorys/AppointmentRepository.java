package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByPatientIdOrderByCreateTimeDesc(Integer patientId);

    List<Appointment> findAllByOrderByCreateTimeDesc();

    boolean existsByPatientId(Integer patientId);

    /**
     * 同一患者对同一排班，只要存在待诊或已诊记录，就视为重复挂号。
     */
    boolean existsByPatientIdAndScheduleIdAndStatusNot(
            Integer patientId, Integer scheduleId, Integer status);

    List<Appointment> findByScheduleIdInOrderByCreateTimeDesc(Collection<Integer> scheduleIds);

    List<Appointment> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end);

    long countByScheduleId(Integer scheduleId);

    long countByScheduleIdAndStatusNot(Integer scheduleId, Integer status);

    @Query("select a from Appointment a " +
            "where a.scheduleId in (select s.id from Schedule s where s.doctorId = :doctorId) " +
            "order by a.createTime desc")
    List<Appointment> findByDoctorId(@Param("doctorId") Integer doctorId);
}
