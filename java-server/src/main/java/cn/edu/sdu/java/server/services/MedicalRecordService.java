package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.exception.BusinessException;
import cn.edu.sdu.java.server.models.Appointment;
import cn.edu.sdu.java.server.models.Doctor;
import cn.edu.sdu.java.server.models.MedicalRecord;
import cn.edu.sdu.java.server.models.Schedule;
import cn.edu.sdu.java.server.payload.response.MedicalRecordItem;
import cn.edu.sdu.java.server.repositorys.AppointmentRepository;
import cn.edu.sdu.java.server.repositorys.DoctorRepository;
import cn.edu.sdu.java.server.repositorys.MedicalRecordRepository;
import cn.edu.sdu.java.server.repositorys.ScheduleRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.RoleUtil;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 病历查询。
 */
@Service
public class MedicalRecordService {

    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentViewBuilder viewBuilder;

    public MedicalRecordService(AppointmentRepository appointmentRepository,
                                MedicalRecordRepository medicalRecordRepository,
                                ScheduleRepository scheduleRepository,
                                DoctorRepository doctorRepository,
                                AppointmentViewBuilder viewBuilder) {
        this.appointmentRepository = appointmentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.scheduleRepository = scheduleRepository;
        this.doctorRepository = doctorRepository;
        this.viewBuilder = viewBuilder;
    }

    public List<MedicalRecordItem> listMyRecords() {
        Integer patientId = CommonMethod.getPersonId();
        List<Integer> appointmentIds = appointmentRepository
                .findByPatientIdOrderByCreateTimeDesc(patientId).stream()
                .map(Appointment::getId)
                .toList();
        if (appointmentIds.isEmpty()) {
            return List.of();
        }
        List<MedicalRecord> records = medicalRecordRepository
                .findByAppointmentIdInOrderByCreateTimeDesc(appointmentIds);
        return viewBuilder.buildRecordItems(records);
    }

    public MedicalRecordItem getRecordByAppointment(Integer appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new BusinessException("病历不存在"));
        checkViewPermission(appointment);
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new BusinessException("病历不存在"));
        return viewBuilder.buildRecordItems(List.of(record)).get(0);
    }

    private void checkViewPermission(Appointment appointment) {
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
        throw new AccessDeniedException("无权查看该病历");
    }
}
