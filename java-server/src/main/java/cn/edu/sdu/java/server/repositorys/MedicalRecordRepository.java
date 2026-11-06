package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Integer> {
    Optional<MedicalRecord> findByAppointmentId(Integer appointmentId);

    boolean existsByAppointmentId(Integer appointmentId);

    List<MedicalRecord> findByAppointmentIdInOrderByCreateTimeDesc(Collection<Integer> appointmentIds);

    List<MedicalRecord> findByAppointmentIdIn(Collection<Integer> appointmentIds);
}
