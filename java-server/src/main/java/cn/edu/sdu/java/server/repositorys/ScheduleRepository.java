package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    List<Schedule> findByDoctorIdOrderByWorkDateAscTimeSlotAsc(Integer doctorId);

    List<Schedule> findByDoctorIdAndWorkDateGreaterThanEqualOrderByWorkDateAscTimeSlotAsc(
            Integer doctorId, LocalDate date);

    List<Schedule> findByWorkDateGreaterThanEqualOrderByWorkDateAscTimeSlotAsc(LocalDate date);

    List<Schedule> findByDoctorIdAndWorkDateBetweenOrderByWorkDateAscTimeSlotAsc(
            Integer doctorId, LocalDate start, LocalDate end);

    List<Schedule> findByDoctorIdInAndWorkDateGreaterThanEqualOrderByWorkDateAscTimeSlotAsc(
            Collection<Integer> doctorIds, LocalDate date);

    List<Schedule> findAllByOrderByWorkDateAscTimeSlotAsc();

    Optional<Schedule> findByDoctorIdAndWorkDateAndTimeSlot(
            Integer doctorId, LocalDate workDate, Integer timeSlot);

    boolean existsByDoctorIdAndWorkDateAndTimeSlot(
            Integer doctorId, LocalDate workDate, Integer timeSlot);

    /**
     * 条件更新扣减号源，返回 1 表示成功，0 表示号源已满。
     * 用单条 UPDATE 保证并发下不会超卖，不引入悲观锁/乐观锁。
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Schedule s set s.remainQuota = s.remainQuota - 1 " +
            "where s.id = :id and s.remainQuota > 0")
    int decreaseQuota(@Param("id") Integer id);

    /**
     * 取消挂号时归还号源，最多恢复到总号数。
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Schedule s set s.remainQuota = s.remainQuota + 1 " +
            "where s.id = :id and s.remainQuota < s.totalQuota")
    int increaseQuota(@Param("id") Integer id);
}
