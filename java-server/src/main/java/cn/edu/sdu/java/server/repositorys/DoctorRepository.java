package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    Optional<Doctor> findByUserId(Integer userId);

    boolean existsByUserId(Integer userId);

    List<Doctor> findAllByOrderByIdAsc();

    List<Doctor> findByDeptIdOrderByIdAsc(Integer deptId);

    long countByDeptId(Integer deptId);
}
