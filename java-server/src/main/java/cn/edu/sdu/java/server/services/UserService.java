package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.exception.BusinessException;
import cn.edu.sdu.java.server.models.EUserType;
import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.models.UserType;
import cn.edu.sdu.java.server.payload.request.ChangePasswordRequest;
import cn.edu.sdu.java.server.payload.request.UpdateProfileRequest;
import cn.edu.sdu.java.server.payload.request.UserCreateRequest;
import cn.edu.sdu.java.server.payload.request.UserUpdateRequest;
import cn.edu.sdu.java.server.payload.response.UserItem;
import cn.edu.sdu.java.server.repositorys.AppointmentRepository;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.repositorys.UserTypeRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 个人资料与管理员用户管理。
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final UserTypeRepository userTypeRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository,
                       PersonRepository personRepository,
                       UserTypeRepository userTypeRepository,
                       AppointmentRepository appointmentRepository,
                       PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.userTypeRepository = userTypeRepository;
        this.appointmentRepository = appointmentRepository;
        this.encoder = encoder;
    }

    public UserItem getCurrentUser() {
        return toItem(currentUser());
    }

    @Transactional
    public void updateProfile(UpdateProfileRequest request) {
        User user = currentUser();
        Person person = user.getPerson();
        if (person == null) {
            throw new BusinessException("用户资料不存在");
        }
        person.setName(request.getRealName().trim());
        person.setEmail(normalizeEmail(request.getEmail()));
        personRepository.save(person);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUser();
        if (!encoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new BusinessException("新密码长度不能少于 6 位");
        }
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * 管理员用户列表：只返回 ADMIN / PATIENT，医生账号由医生管理维护。
     */
    public List<UserItem> listUsers(String role, String keyword) {
        String normalizedRole = normalizeRole(role);
        List<String> typeNames;
        if (normalizedRole == null || normalizedRole.isBlank()) {
            typeNames = List.of(EUserType.ROLE_ADMIN.name(), EUserType.ROLE_PATIENT.name());
        } else if ("ADMIN".equals(normalizedRole) || "PATIENT".equals(normalizedRole)) {
            typeNames = List.of("ROLE_" + normalizedRole);
        } else {
            throw new BusinessException("角色不合法");
        }

        String kw = keyword == null ? "" : keyword.trim().toLowerCase();
        return userRepository.findByUserType_NameIn(typeNames).stream()
                .filter(user -> matchesKeyword(user, kw))
                .sorted(Comparator.comparing(User::getPersonId))
                .map(this::toItem)
                .toList();
    }

    @Transactional
    public Map<String, Object> createUser(UserCreateRequest request) {
        String role = normalizeRole(request.getRole());
        if (!"ADMIN".equals(role) && !"PATIENT".equals(role)) {
            throw new BusinessException("角色不合法");
        }
        String username = request.getUsername().trim();
        if (Boolean.TRUE.equals(userRepository.existsByUserName(username))
                || personRepository.existsByNum(username)) {
            throw new BusinessException("用户名已存在");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于 6 位");
        }
        UserType userType = userTypeRepository.findByName("ROLE_" + role);
        if (userType == null) {
            throw new BusinessException("角色未初始化，请先执行建表脚本");
        }

        Person person = new Person();
        person.setNum(username);
        person.setName(request.getRealName().trim());
        person.setEmail(normalizeEmail(request.getEmail()));
        personRepository.saveAndFlush(person);

        User user = new User();
        // 与 person 共用主键列，只设主键不设 person 关联，避免 Hibernate merge 报 null identifier。
        user.setPersonId(person.getPersonId());
        user.setUserType(userType);
        user.setUserName(username);
        user.setPassword(encoder.encode(request.getPassword()));
        user.setCreateTime(DateTimeTool.parseDateTime(new Date()));
        user.setLoginCount(0);
        user.setCreatorId(CommonMethod.getPersonId());
        userRepository.saveAndFlush(user);

        return Map.of("id", person.getPersonId());
    }

    @Transactional
    public void updateUser(Integer id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (isDoctor(user)) {
            throw new BusinessException("医生账号请在医生管理中修改");
        }
        Person person = user.getPerson();
        if (person == null) {
            throw new BusinessException("用户资料不存在");
        }
        person.setName(request.getRealName().trim());
        person.setEmail(normalizeEmail(request.getEmail()));
        personRepository.save(person);

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getNewPassword().length() < 6) {
                throw new BusinessException("新密码长度不能少于 6 位");
            }
            user.setPassword(encoder.encode(request.getNewPassword()));
            userRepository.save(user);
        }
    }

    @Transactional
    public void deleteUser(Integer id) {
        if (id.equals(CommonMethod.getPersonId())) {
            throw new BusinessException("不能删除当前登录账号");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        if (isDoctor(user)) {
            throw new BusinessException("医生账号请在医生管理中删除");
        }
        if ("PATIENT".equals(normalizeRole(user.getUserType() == null ? null : user.getUserType().getName()))
                && appointmentRepository.existsByPatientId(id)) {
            throw new BusinessException("该用户存在关联数据，不能删除");
        }
        try {
            userRepository.delete(user);
            userRepository.flush();
            personRepository.deleteById(id);
            personRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("该用户存在关联数据，不能删除");
        }
    }

    private User currentUser() {
        Integer personId = CommonMethod.getPersonId();
        if (personId == null) {
            throw new BusinessException("登录已失效");
        }
        return userRepository.findById(personId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private boolean matchesKeyword(User user, String kw) {
        if (kw.isEmpty()) {
            return true;
        }
        String username = user.getUserName() == null ? "" : user.getUserName().toLowerCase();
        String realName = user.getPerson() == null || user.getPerson().getName() == null
                ? "" : user.getPerson().getName().toLowerCase();
        return username.contains(kw) || realName.contains(kw);
    }

    private boolean isDoctor(User user) {
        return user.getUserType() != null && EUserType.ROLE_DOCTOR.name().equals(user.getUserType().getName());
    }

    private UserItem toItem(User user) {
        Person person = user.getPerson();
        return new UserItem(
                user.getPersonId(),
                user.getUserName(),
                person == null ? null : person.getName(),
                person == null ? null : person.getEmail(),
                normalizeRole(user.getUserType() == null ? null : user.getUserType().getName()));
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return null;
        }
        String value = role.trim().toUpperCase();
        return value.startsWith("ROLE_") ? value.substring(5) : value;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim();
    }
}
