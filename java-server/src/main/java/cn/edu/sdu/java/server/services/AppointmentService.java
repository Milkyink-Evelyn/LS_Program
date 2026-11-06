package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.exception.BusinessException;
import cn.edu.sdu.java.server.models.Appointment;
import cn.edu.sdu.java.server.models.Doctor;
import cn.edu.sdu.java.server.models.MedicalRecord;
import cn.edu.sdu.java.server.models.Schedule;
import cn.edu.sdu.java.server.payload.request.AppointmentCreateRequest;
import cn.edu.sdu.java.server.payload.request.CompleteAppointmentRequest;
import cn.edu.sdu.java.server.payload.response.AppointmentDetail;
import cn.edu.sdu.java.server.payload.response.AppointmentItem;
import cn.edu.sdu.java.server.repositorys.AppointmentRepository;
import cn.edu.sdu.java.server.repositorys.DoctorRepository;
import cn.edu.sdu.java.server.repositorys.MedicalRecordRepository;
import cn.edu.sdu.java.server.repositorys.ScheduleRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.RoleUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 挂号：患者挂号/取消，医生接诊，管理员查询。
 */
@Service
public class AppointmentService {

    private static final int WAITING = 0;
    private static final int ATTENDED = 1;
    private static final int CANCELLED = 2;

    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentViewBuilder viewBuilder;
    private final SystemService systemService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              ScheduleRepository scheduleRepository,
                              DoctorRepository doctorRepository,
                              MedicalRecordRepository medicalRecordRepository,
                              AppointmentViewBuilder viewBuilder,
                              SystemService systemService) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.doctorRepository = doctorRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.viewBuilder = viewBuilder;
        this.systemService = systemService;
    }

    /**
     * 患者挂号：号源用单条条件 UPDATE 扣减，避免超卖。
     */
    @Transactional
    public Map<String, Object> createAppointment(AppointmentCreateRequest request) {
        Integer patientId = CommonMethod.getPersonId();
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new BusinessException("排班不存在"));
        if (schedule.getWorkDate().isBefore(LocalDate.now())) {
            throw new BusinessException("该排班已过期");
        }
        if (Boolean.TRUE.equals(appointmentRepository.existsByPatientIdAndScheduleIdAndStatusNot(
                patientId, schedule.getId(), CANCELLED))) {
            throw new BusinessException("您已挂过该号");
        }
        if (scheduleRepository.decreaseQuota(schedule.getId()) == 0) {
            throw new BusinessException("号源已满");
        }

        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setScheduleId(schedule.getId());
        appointment.setStatus(WAITING);
        appointment.setCreateTime(LocalDateTime.now());
        appointmentRepository.saveAndFlush(appointment);
        systemService.modifyLog(appointment, true);

        int remainQuota = scheduleRepository.findById(schedule.getId())
                .map(Schedule::getRemainQuota)
                .orElse(0);
        return Map.of("id", appointment.getId(), "status", WAITING, "remainQuota", remainQuota);
    }

    public List<AppointmentItem> listMyAppointments(Integer status, LocalDate date) {
        Integer patientId = CommonMethod.getPersonId();
        List<Appointment> appointments = appointmentRepository
                .findByPatientIdOrderByCreateTimeDesc(patientId).stream()
                .filter(appointment -> status == null || status.equals(appointment.getStatus()))
                .toList();
        List<AppointmentItem> items = new ArrayList<>(viewBuilder.buildItems(appointments));
        if (date != null) {
            items.removeIf(item -> !date.equals(item.getWorkDate()));
        }
        return items;
    }

    /**
     * 取消挂号：只能取消本人的待诊挂号，取消后归还号源。
     */
    @Transactional
    public void cancelAppointment(Integer id) {
        Integer patientId = CommonMethod.getPersonId();
        Appointment appointment = appointmentRepository.findById(id)
                .filter(item -> patientId.equals(item.getPatientId()))
                .orElseThrow(() -> new BusinessException("挂号记录不存在"));
        if (appointment.getStatus() == ATTENDED) {
            throw new BusinessException("该挂号已就诊，不能取消");
        }
        if (appointment.getStatus() != WAITING) {
            throw new BusinessException("只能取消待诊状态的挂号");
        }
        appointment.setStatus(CANCELLED);
        appointmentRepository.save(appointment);
        scheduleRepository.increaseQuota(appointment.getScheduleId());
        systemService.modifyLog(appointment, false);
    }

    /**
     * 医生查看自己的就诊列表，按排班日期、时段、挂号时间升序。
     */
    public List<AppointmentItem> listDoctorAppointments(Integer status, LocalDate date) {
        Doctor doctor = currentDoctor();
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctor.getId()).stream()
                .filter(appointment -> status == null || status.equals(appointment.getStatus()))
                .toList();
        List<AppointmentItem> items = new ArrayList<>(viewBuilder.buildItems(appointments));
        if (date != null) {
            items.removeIf(item -> !date.equals(item.getWorkDate()));
        }
        items.sort(Comparator
                .comparing(AppointmentItem::getWorkDate,
                        Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(AppointmentItem::getTimeSlot,
                        Comparator.nullsLast(Integer::compareTo))
                .thenComparing(AppointmentItem::getCreateTime,
                        Comparator.nullsLast(String::compareTo)));
        return items;
    }

    /**
     * 医生接诊并写病历，同一事务内更新挂号状态。
     */
    @Transactional
    public Map<String, Object> completeAppointment(Integer id,
                                                   CompleteAppointmentRequest request) {
        Doctor doctor = currentDoctor();
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("挂号记录不存在"));
        Schedule schedule = scheduleRepository.findById(appointment.getScheduleId())
                .orElseThrow(() -> new BusinessException("排班不存在"));
        if (!doctor.getId().equals(schedule.getDoctorId())) {
            throw new BusinessException("无权接诊该挂号");
        }
        if (appointment.getStatus() == ATTENDED) {
            throw new BusinessException("该挂号已就诊，不能重复提交");
        }
        if (appointment.getStatus() == CANCELLED) {
            throw new BusinessException("该挂号已取消，不能接诊");
        }

        MedicalRecord record = new MedicalRecord();
        record.setAppointmentId(appointment.getId());
        record.setSymptom(request.getSymptom().trim());
        record.setDiagnosis(request.getDiagnosis().trim());
        record.setPrescription(request.getPrescription().trim());
        record.setCreateTime(LocalDateTime.now());
        medicalRecordRepository.saveAndFlush(record);
        systemService.modifyLog(record, true);

        appointment.setStatus(ATTENDED);
        appointmentRepository.save(appointment);
        systemService.modifyLog(appointment, false);
        return Map.of("recordId", record.getId());
    }

    /**
     * 管理员查看全部挂号，按挂号时间降序。
     */
    public List<AppointmentItem> listAllAppointments(LocalDate date, Integer deptId,
                                                     Integer doctorId, Integer status,
                                                     String keyword) {
        List<Appointment> appointments = appointmentRepository.findAllByOrderByCreateTimeDesc()
                .stream()
                .filter(appointment -> status == null || status.equals(appointment.getStatus()))
                .toList();
        String kw = keyword == null ? "" : keyword.trim();
        return viewBuilder.buildItems(appointments).stream()
                .filter(item -> date == null || date.equals(item.getWorkDate()))
                .filter(item -> deptId == null || deptId.equals(item.getDeptId()))
                .filter(item -> doctorId == null || doctorId.equals(item.getDoctorId()))
                .filter(item -> kw.isEmpty() || containsIgnoreCase(item.getPatientName(), kw))
                .toList();
    }

    public AppointmentDetail getAppointment(Integer id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("挂号记录不存在"));
        checkViewPermission(appointment, "无权查看该挂号");
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(id).orElse(null);
        return viewBuilder.buildDetail(appointment, record);
    }

    private Doctor currentDoctor() {
        return doctorRepository.findByUserId(CommonMethod.getPersonId())
                .orElseThrow(() -> new BusinessException("医生信息不存在"));
    }

    private void checkViewPermission(Appointment appointment, String message) {
        String role = RoleUtil.currentRole();
        Integer currentId = CommonMethod.getPersonId();
        if ("ADMIN".equals(role)) {
            return;
        }
        if ("PATIENT".equals(role) && currentId != null
                && currentId.equals(appointment.getPatientId())) {
            return;
        }
        if ("DOCTOR".equals(role)) {
            Doctor doctor = doctorRepository.findByUserId(currentId).orElse(null);
            Schedule schedule = scheduleRepository.findById(appointment.getScheduleId()).orElse(null);
            if (doctor != null && schedule != null
                    && doctor.getId().equals(schedule.getDoctorId())) {
                return;
            }
        }
        throw new AccessDeniedException(message);
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword.toLowerCase());
    }
}
