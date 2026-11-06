package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Appointment;
import cn.edu.sdu.java.server.models.Department;
import cn.edu.sdu.java.server.models.Doctor;
import cn.edu.sdu.java.server.models.MedicalRecord;
import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.models.Schedule;
import cn.edu.sdu.java.server.payload.response.AppointmentDetail;
import cn.edu.sdu.java.server.payload.response.AppointmentItem;
import cn.edu.sdu.java.server.payload.response.MedicalRecordItem;
import cn.edu.sdu.java.server.repositorys.AppointmentRepository;
import cn.edu.sdu.java.server.repositorys.DepartmentRepository;
import cn.edu.sdu.java.server.repositorys.DoctorRepository;
import cn.edu.sdu.java.server.repositorys.MedicalRecordRepository;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import cn.edu.sdu.java.server.repositorys.ScheduleRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 挂号、病历读模型的批量拼装：先按外键批量查，再用 Map 组装，避免联表查询和 N+1。
 */
@Component
public class AppointmentViewBuilder {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final PersonRepository personRepository;
    private final DepartmentRepository departmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AppointmentRepository appointmentRepository;

    public AppointmentViewBuilder(ScheduleRepository scheduleRepository,
                                  DoctorRepository doctorRepository,
                                  PersonRepository personRepository,
                                  DepartmentRepository departmentRepository,
                                  MedicalRecordRepository medicalRecordRepository,
                                  AppointmentRepository appointmentRepository) {
        this.scheduleRepository = scheduleRepository;
        this.doctorRepository = doctorRepository;
        this.personRepository = personRepository;
        this.departmentRepository = departmentRepository;
        this.medicalRecordRepository = medicalRecordRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<AppointmentItem> buildItems(List<Appointment> appointments) {
        if (appointments.isEmpty()) {
            return List.of();
        }
        Map<Integer, Schedule> scheduleMap = loadSchedules(appointments);
        Map<Integer, Doctor> doctorMap = loadDoctors(scheduleMap.values());
        Set<Integer> personIds = new HashSet<>();
        appointments.forEach(appointment -> personIds.add(appointment.getPatientId()));
        doctorMap.values().forEach(doctor -> personIds.add(doctor.getUserId()));
        Map<Integer, Person> personMap = loadPersons(personIds);
        Map<Integer, Department> deptMap = loadDepartments(doctorMap.values());

        Set<Integer> appointmentIds = appointments.stream()
                .map(Appointment::getId)
                .collect(Collectors.toSet());
        Set<Integer> recordedIds = medicalRecordRepository
                .findByAppointmentIdIn(appointmentIds).stream()
                .map(MedicalRecord::getAppointmentId)
                .collect(Collectors.toSet());

        return appointments.stream().map(appointment -> {
            Schedule schedule = scheduleMap.get(appointment.getScheduleId());
            Doctor doctor = schedule == null ? null : doctorMap.get(schedule.getDoctorId());
            Person patient = personMap.get(appointment.getPatientId());
            Person doctorPerson = doctor == null ? null : personMap.get(doctor.getUserId());
            Department dept = doctor == null ? null : deptMap.get(doctor.getDeptId());

            AppointmentItem item = new AppointmentItem();
            item.setId(appointment.getId());
            item.setScheduleId(appointment.getScheduleId());
            item.setPatientId(appointment.getPatientId());
            item.setPatientName(patient == null ? null : patient.getName());
            item.setDoctorId(doctor == null ? null : doctor.getId());
            item.setDoctorName(doctorPerson == null ? null : doctorPerson.getName());
            item.setDeptId(doctor == null ? null : doctor.getDeptId());
            item.setDeptName(dept == null ? null : dept.getDeptName());
            item.setWorkDate(schedule == null ? null : schedule.getWorkDate());
            item.setTimeSlot(schedule == null ? null : schedule.getTimeSlot());
            item.setStatus(appointment.getStatus());
            item.setCreateTime(formatTime(appointment.getCreateTime()));
            item.setHasRecord(recordedIds.contains(appointment.getId()));
            return item;
        }).toList();
    }

    public AppointmentDetail buildDetail(Appointment appointment, MedicalRecord record) {
        AppointmentDetail detail = new AppointmentDetail(
                buildItems(List.of(appointment)).get(0));
        if (record != null) {
            detail.setMedicalRecord(buildRecordItems(List.of(record)).get(0));
        }
        return detail;
    }

    public List<MedicalRecordItem> buildRecordItems(List<MedicalRecord> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        Set<Integer> appointmentIds = records.stream()
                .map(MedicalRecord::getAppointmentId)
                .collect(Collectors.toSet());
        Map<Integer, Appointment> appointmentMap = appointmentRepository.findAllById(appointmentIds).stream()
                .collect(Collectors.toMap(Appointment::getId, appointment -> appointment));
        Map<Integer, Schedule> scheduleMap = loadSchedules(appointmentMap.values());
        Map<Integer, Doctor> doctorMap = loadDoctors(scheduleMap.values());
        Map<Integer, Person> personMap = loadPersons(doctorMap.values().stream()
                .map(Doctor::getUserId).collect(Collectors.toSet()));
        Map<Integer, Department> deptMap = loadDepartments(doctorMap.values());

        return records.stream().map(record -> {
            Appointment appointment = appointmentMap.get(record.getAppointmentId());
            Schedule schedule = appointment == null ? null
                    : scheduleMap.get(appointment.getScheduleId());
            Doctor doctor = schedule == null ? null : doctorMap.get(schedule.getDoctorId());
            Person doctorPerson = doctor == null ? null : personMap.get(doctor.getUserId());
            Department dept = doctor == null ? null : deptMap.get(doctor.getDeptId());

            MedicalRecordItem item = new MedicalRecordItem();
            item.setId(record.getId());
            item.setAppointmentId(record.getAppointmentId());
            item.setSymptom(record.getSymptom());
            item.setDiagnosis(record.getDiagnosis());
            item.setPrescription(record.getPrescription());
            item.setCreateTime(formatTime(record.getCreateTime()));
            item.setDoctorName(doctorPerson == null ? null : doctorPerson.getName());
            item.setDeptName(dept == null ? null : dept.getDeptName());
            item.setWorkDate(schedule == null ? null : schedule.getWorkDate());
            item.setTimeSlot(schedule == null ? null : schedule.getTimeSlot());
            return item;
        }).toList();
    }

    private Map<Integer, Schedule> loadSchedules(Collection<Appointment> appointments) {
        Set<Integer> ids = appointments.stream()
                .map(Appointment::getScheduleId)
                .collect(Collectors.toSet());
        return scheduleRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Schedule::getId, schedule -> schedule));
    }

    private Map<Integer, Doctor> loadDoctors(Collection<Schedule> schedules) {
        Set<Integer> ids = schedules.stream()
                .map(Schedule::getDoctorId)
                .collect(Collectors.toSet());
        return doctorRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Doctor::getId, doctor -> doctor));
    }

    private Map<Integer, Person> loadPersons(Set<Integer> personIds) {
        return personRepository.findAllById(personIds).stream()
                .collect(Collectors.toMap(Person::getPersonId, person -> person));
    }

    private Map<Integer, Department> loadDepartments(Collection<Doctor> doctors) {
        Set<Integer> ids = doctors.stream()
                .map(Doctor::getDeptId)
                .collect(Collectors.toSet());
        return departmentRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Department::getId, dept -> dept));
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? null : TIME_FORMAT.format(time);
    }
}
