package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.exception.BusinessException;
import cn.edu.sdu.java.server.models.Department;
import cn.edu.sdu.java.server.models.Doctor;
import cn.edu.sdu.java.server.payload.request.DepartmentRequest;
import cn.edu.sdu.java.server.payload.response.DepartmentItem;
import cn.edu.sdu.java.server.repositorys.DepartmentRepository;
import cn.edu.sdu.java.server.repositorys.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 科室管理。
 */
@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    public DepartmentService(DepartmentRepository departmentRepository, DoctorRepository doctorRepository) {
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
    }

    /**
     * 科室列表，附带每个科室的医生人数。
     */
    public List<DepartmentItem> listDepartments() {
        Map<Integer, Long> doctorCounts = doctorRepository.findAll().stream()
                .collect(Collectors.groupingBy(Doctor::getDeptId, Collectors.counting()));
        return departmentRepository.findAllByOrderByIdAsc().stream()
                .map(dept -> new DepartmentItem(
                        dept.getId(),
                        dept.getDeptName(),
                        doctorCounts.getOrDefault(dept.getId(), 0L).intValue()))
                .toList();
    }

    @Transactional
    public Map<String, Object> createDepartment(DepartmentRequest request) {
        String deptName = request.getDeptName().trim();
        if (departmentRepository.existsByDeptName(deptName)) {
            throw new BusinessException("科室名称已存在");
        }
        Department dept = new Department();
        dept.setDeptName(deptName);
        departmentRepository.saveAndFlush(dept);
        return Map.of("id", dept.getId());
    }

    @Transactional
    public void updateDepartment(Integer id, DepartmentRequest request) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科室不存在"));
        String deptName = request.getDeptName().trim();
        if (departmentRepository.existsByDeptNameAndIdNot(deptName, id)) {
            throw new BusinessException("科室名称已存在");
        }
        dept.setDeptName(deptName);
        departmentRepository.save(dept);
    }

    @Transactional
    public void deleteDepartment(Integer id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("科室不存在"));
        if (doctorRepository.countByDeptId(id) > 0) {
            throw new BusinessException("该科室下还有医生，不能删除");
        }
        departmentRepository.delete(dept);
    }
}
