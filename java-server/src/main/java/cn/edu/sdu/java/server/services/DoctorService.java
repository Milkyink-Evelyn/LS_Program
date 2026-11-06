package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.exception.BusinessException;
import cn.edu.sdu.java.server.models.Department;
import cn.edu.sdu.java.server.models.Doctor;
import cn.edu.sdu.java.server.models.EUserType;
import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.models.UserType;
import cn.edu.sdu.java.server.payload.request.DoctorCreateRequest;
import cn.edu.sdu.java.server.payload.request.DoctorUpdateRequest;
import cn.edu.sdu.java.server.payload.response.DoctorItem;
import cn.edu.sdu.java.server.repositorys.DepartmentRepository;
import cn.edu.sdu.java.server.repositorys.DoctorRepository;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import cn.edu.sdu.java.server.repositorys.ScheduleRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.repositorys.UserTypeRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 医生管理：医生档案与登录账号一起维护。
 */
@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final ScheduleRepository scheduleRepository;
    private final PasswordEncoder encoder;
    private final SystemService systemService;

    public DoctorService(DoctorRepository doctorRepository,
                         DepartmentRepository departmentRepository,
                         PersonRepository personRepository,
                         UserRepository userRepository,
                         UserTypeRepository userTypeRepository,
                         ScheduleRepository scheduleRepository,
                         PasswordEncoder encoder,
                         SystemService systemService) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.scheduleRepository = scheduleRepository;
        this.encoder = encoder;
        this.systemService = systemService;
    }

    public List<DoctorItem> listDoctors(Integer deptId, String keyword) {
        List<Doctor> doctors = deptId == null
                ? doctorRepository.findAllByOrderByIdAsc()
                : doctorRepository.findByDeptIdOrderByIdAsc(deptId);
        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        return buildItems(doctors).stream()
                .filter(item -> kw.isEmpty()
                        || containsIgnoreCase(item.getUsername(), kw)
                        || containsIgnoreCase(item.getRealName(), kw))
                .toList();
    }

    public DoctorItem getDoctor(Integer id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("医生不存在"));
        return buildItems(List.of(doctor)).get(0);
    }

    /**
     * 新增医生：person -> user(ROLE_DOCTOR) -> t_doctor。
     */
    @Transactional
    public Map<String, Object> createDoctor(DoctorCreateRequest request) {
        String username = request.getUsername().trim();
        if (Boolean.TRUE.equals(userRepository.existsByUserName(username))
                || personRepository.existsByNum(username)) {
            throw new BusinessException("用户名已存在");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于 6 位");
        }
        Department dept = departmentRepository.findById(request.getDeptId())
                .orElseThrow(() -> new BusinessException("科室不存在"));
        UserType doctorType = userTypeRepository.findByName(EUserType.ROLE_DOCTOR.name());
        if (doctorType == null) {
            throw new BusinessException("医生角色未初始化，请先执行建表脚本");
        }

        Person person = new Person();
        person.setNum(username);
        person.setName(request.getRealName().trim());
        personRepository.saveAndFlush(person);

        User user = new User();
        // 与 person 共用主键列，只设主键不设 person 关联，避免 Hibernate merge 报 null identifier。
        user.setPersonId(person.getPersonId());
        user.setUserType(doctorType);
        user.setUserName(username);
        user.setPassword(encoder.encode(request.getPassword()));
        user.setCreateTime(DateTimeTool.parseDateTime(new Date()));
        user.setLoginCount(0);
        user.setCreatorId(CommonMethod.getPersonId());
        userRepository.saveAndFlush(user);

        Doctor doctor = new Doctor();
        doctor.setUserId(person.getPersonId());
        doctor.setDeptId(dept.getId());
        doctor.setTitle(request.getTitle().trim());
        doctor.setIntroduction(normalizeText(request.getIntroduction()));
        doctorRepository.saveAndFlush(doctor);
        systemService.modifyLog(person, true);
        systemService.modifyLog(doctor, true);

        return Map.of("doctorId", doctor.getId(), "userId", person.getPersonId());
    }

    @Transactional
    public void updateDoctor(Integer id, DoctorUpdateRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("医生不存在"));
        Department dept = departmentRepository.findById(request.getDeptId())
                .orElseThrow(() -> new BusinessException("科室不存在"));
        Person person = personRepository.findById(doctor.getUserId())
                .orElseThrow(() -> new BusinessException("医生资料不存在"));
        User user = userRepository.findById(doctor.getUserId())
                .orElseThrow(() -> new BusinessException("医生账号不存在"));

        person.setName(request.getRealName().trim());
        personRepository.save(person);

        doctor.setDeptId(dept.getId());
        doctor.setTitle(request.getTitle().trim());
        doctor.setIntroduction(normalizeText(request.getIntroduction()));
        doctorRepository.save(doctor);
        systemService.modifyLog(person, false);
        systemService.modifyLog(doctor, false);

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getNewPassword().length() < 6) {
                throw new BusinessException("新密码长度不能少于 6 位");
            }
            user.setPassword(encoder.encode(request.getNewPassword()));
            userRepository.save(user);
        }
    }

    @Transactional
    public void deleteDoctor(Integer id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("医生不存在"));
        if (scheduleRepository.existsByDoctorId(id)) {
            throw new BusinessException("该医生还有排班或挂号记录，不能删除");
        }
        Integer userId = doctor.getUserId();
        try {
            doctorRepository.delete(doctor);
            doctorRepository.flush();
            userRepository.deleteById(userId);
            userRepository.flush();
            personRepository.deleteById(userId);
            personRepository.flush();
            systemService.modifyLog(doctor, false);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("该医生还有排班或挂号记录，不能删除");
        }
    }

    private List<DoctorItem> buildItems(List<Doctor> doctors) {
        if (doctors.isEmpty()) {
            return List.of();
        }
        Set<Integer> userIds = doctors.stream().map(Doctor::getUserId).collect(Collectors.toSet());
        Map<Integer, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getPersonId, user -> user));
        Map<Integer, Person> personMap = personRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(Person::getPersonId, person -> person));
        Set<Integer> deptIds = doctors.stream().map(Doctor::getDeptId).collect(Collectors.toSet());
        Map<Integer, Department> deptMap = departmentRepository.findAllById(deptIds).stream()
                .collect(Collectors.toMap(Department::getId, dept -> dept));

        return doctors.stream().map(doctor -> {
            User user = userMap.get(doctor.getUserId());
            Person person = personMap.get(doctor.getUserId());
            Department dept = deptMap.get(doctor.getDeptId());
            return new DoctorItem(
                    doctor.getId(),
                    doctor.getUserId(),
                    user == null ? null : user.getUserName(),
                    person == null ? null : person.getName(),
                    doctor.getDeptId(),
                    dept == null ? null : dept.getDeptName(),
                    doctor.getTitle(),
                    doctor.getIntroduction());
        }).toList();
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    private String normalizeText(String text) {
        return text == null || text.isBlank() ? null : text.trim();
    }
}
