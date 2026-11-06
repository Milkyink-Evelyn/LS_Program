package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.exception.BusinessException;
import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.request.LoginRequest;
import cn.edu.sdu.java.server.payload.request.RegisterRequest;
import cn.edu.sdu.java.server.payload.request.ResetPasswordRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.JwtResponse;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import cn.edu.sdu.java.server.util.LoginControlUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final StudentRepository studentRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;

    public AuthService(PersonRepository personRepository, UserRepository userRepository, UserTypeRepository userTypeRepository, StudentRepository studentRepository,AuthenticationManager authenticationManager, JwtService jwtService, PasswordEncoder encoder) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.studentRepository = studentRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.encoder = encoder;
    }

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        Optional<User> op= userRepository.findByUserName(loginRequest.getUsername());
        if(op.isPresent()) {
            User user= op.get();
            user.setLastLoginTime(DateTimeTool.parseDateTime(new Date()));
            Integer count = user.getLoginCount();
            if (count == null)
                count = 1;
            else count += 1;
            user.setLoginCount(count);
            userRepository.save(user);
        }
        String jwt = jwtService.generateToken(userDetails);
        // 前端只认 ADMIN / DOCTOR / PATIENT，这里去掉 Spring Security 的 ROLE_ 前缀
        String role = roles.isEmpty() ? "" : roles.getFirst();
        if (role.startsWith("ROLE_")) {
            role = role.substring(5);
        }
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getPerName(),
                role));
    }

    public DataResponse getValidateCode(DataRequest dataRequest) {
        return CommonMethod.getReturnData(LoginControlUtil.getInstance().getValidateCodeDataMap());
    }

    public DataResponse testValidateInfo( DataRequest dataRequest) {
        Integer validateCodeId = dataRequest.getInteger("validateCodeId");
        String validateCode = dataRequest.getString("validateCode");
        LoginControlUtil li =  LoginControlUtil.getInstance();
        if(validateCodeId == null || validateCode== null || validateCode.isEmpty()) {
            return CommonMethod.getReturnMessageError("验证码为空！");
        }
        String value = li.getValidateCode(validateCodeId);
        if(!validateCode.equals(value))
            return CommonMethod.getReturnMessageError("验证码错位！");
        return CommonMethod.getReturnMessageOK();
    }

    /**
     * 患者注册：创建 person + user，角色固定为 ROLE_PATIENT，不创建 student 记录。
     */
    @Transactional
    public DataResponse register(RegisterRequest request) {
        validateCaptcha(request.getValidateCodeId(), request.getValidateCode());

        String username = request.getUsername().trim();
        if (Boolean.TRUE.equals(userRepository.existsByUserName(username))
                || personRepository.existsByNum(username)) {
            throw new BusinessException("用户名已存在");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException("密码长度不能少于 6 位");
        }
        UserType patientType = userTypeRepository.findByName(EUserType.ROLE_PATIENT.name());
        if (patientType == null) {
            throw new BusinessException("患者角色未初始化，请先执行建表脚本");
        }

        Person person = new Person();
        person.setNum(username);
        person.setName(request.getRealName().trim());
        person.setEmail(normalizeEmail(request.getEmail()));
        personRepository.saveAndFlush(person);

        User user = new User();
        // User 的主键 personId 与 person 共用一列，这里只设主键，不设 person 关联。
        // 设置 person 对象会让 Hibernate 在 merge 时把它当作分离对象处理，触发 null identifier。
        user.setPersonId(person.getPersonId());
        user.setUserType(patientType);
        user.setUserName(username);
        user.setPassword(encoder.encode(request.getPassword()));
        user.setCreateTime(DateTimeTool.parseDateTime(new Date()));
        user.setLoginCount(0);
        user.setCreatorId(person.getPersonId());
        userRepository.saveAndFlush(user);

        return CommonMethod.getReturnData(Map.of("id", person.getPersonId()));
    }

    /**
     * 忘记密码：用户名 + 真实姓名匹配后重置密码。
     */
    @Transactional
    public DataResponse resetPassword(ResetPasswordRequest request) {
        validateCaptcha(request.getValidateCodeId(), request.getValidateCode());

        String username = request.getUsername().trim();
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new BusinessException("用户信息不匹配"));
        if (user.getPerson() == null
                || !request.getRealName().trim().equals(user.getPerson().getName())) {
            throw new BusinessException("用户信息不匹配");
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            throw new BusinessException("新密码长度不能少于 6 位");
        }
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return CommonMethod.getReturnMessageOK("密码重置成功");
    }

    private void validateCaptcha(Integer validateCodeId, String validateCode) {
        LoginControlUtil util = LoginControlUtil.getInstance();
        String expected = validateCodeId == null ? null : util.getValidateCode(validateCodeId);
        if (expected == null || validateCode == null || !expected.equals(validateCode.trim())) {
            throw new BusinessException("验证码错误或已失效");
        }
        // 校验通过立即失效，防止同一个验证码重复使用
        util.removeValidateCode(validateCodeId);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim();
    }

    /*
     *  注册用户示例，我们项目暂时不用， 所有用户通过管理员添加，这里注册，没有考虑关联人员信息的创建，使用时参加学生添加功能的实现
     */
    @PostMapping("/registerUser")
    public DataResponse registerUser(@Valid @RequestBody DataRequest dataRequest) {
        String username = dataRequest.getString("username");
        String password = dataRequest.getString("password");
        String perName = dataRequest.getString("perName");
        String email = dataRequest.getString("email");
        String role = dataRequest.getString("role");
        UserType ut = null;
        Optional<User> uOp = userRepository.findByUserName(username);
        if(uOp.isPresent()) {
            return CommonMethod.getReturnMessageError("用户已经存在，不能注册！");
        }
        Person p = new Person();
        p.setNum(username);
        p.setName(perName);
        p.setEmail(email);
        if("ADMIN".equals(role)) {
            p.setType("0");
            ut = userTypeRepository.findByName(EUserType.ROLE_ADMIN.name());
        }else if("STUDENT".equals(role)) {
            p.setType("1");
            ut = userTypeRepository.findByName(EUserType.ROLE_STUDENT.name());
        }else if("TEACHER".equals(role)) {
            p.setType("2");
            ut = userTypeRepository.findByName(EUserType.ROLE_TEACHER.name());
        }
        personRepository.saveAndFlush(p);
        User u = new User();
        u.setPersonId(p.getPersonId());
        u.setUserType(ut);
        u.setUserName(username);
        u.setPassword(encoder.encode(password));
        u.setCreateTime(DateTimeTool.parseDateTime(new Date()));
        u.setCreatorId(p.getPersonId());
        u.setLoginCount(0);
        userRepository.saveAndFlush(u);
        if("STUDENT".equals(role)) {
            Student s = new Student();   // 创建实体对象
            s.setPerson(p);
            studentRepository.saveAndFlush(s);  //插入新的Student记录
        }
        return CommonMethod.getReturnData(LoginControlUtil.getInstance().getValidateCodeDataMap());
    }

}
