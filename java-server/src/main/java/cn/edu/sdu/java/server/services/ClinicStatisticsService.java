package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Appointment;
import cn.edu.sdu.java.server.models.Department;
import cn.edu.sdu.java.server.models.Doctor;
import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.models.Schedule;
import cn.edu.sdu.java.server.payload.response.StatDateCount;
import cn.edu.sdu.java.server.payload.response.StatDeptCount;
import cn.edu.sdu.java.server.payload.response.StatDoctorCount;
import cn.edu.sdu.java.server.repositorys.AppointmentRepository;
import cn.edu.sdu.java.server.repositorys.DepartmentRepository;
import cn.edu.sdu.java.server.repositorys.DoctorRepository;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import cn.edu.sdu.java.server.repositorys.ScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员统计：近 7 天挂号量、科室占比、医生接诊 Top5。
 * 统一先查挂号，再按外键批量查排班/医生/人员/科室，最后在内存分组，不做联表查询。
 */
@Service
public class ClinicStatisticsService {

    /** 已取消 */
    private static final int STATUS_CANCELLED = 2;
    /** 已诊 */
    private static final int STATUS_ATTENDED = 1;

    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final PersonRepository personRepository;
    private final DepartmentRepository departmentRepository;

    public ClinicStatisticsService(AppointmentRepository appointmentRepository,
                                   ScheduleRepository scheduleRepository,
                                   DoctorRepository doctorRepository,
                                   PersonRepository personRepository,
                                   DepartmentRepository departmentRepository) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.doctorRepository = doctorRepository;
        this.personRepository = personRepository;
        this.departmentRepository = departmentRepository;
    }

    /**
     * 近 7 天挂号量，日期升序，没有数据的日期补 0，排除已取消。
     */
    public List<StatDateCount> last7Days() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);

        List<Appointment> appointments = appointmentRepository.findByCreateTimeBetween(start, end);
        Map<LocalDate, Long> counter = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            counter.put(startDate.plusDays(i), 0L);
        }
        for (Appointment appointment : appointments) {
            if (appointment.getStatus() != null
                    && appointment.getStatus() != STATUS_CANCELLED
                    && appointment.getCreateTime() != null) {
                LocalDate day = appointment.getCreateTime().toLocalDate();
                if (counter.containsKey(day)) {
                    counter.put(day, counter.get(day) + 1);
                }
            }
        }
        return counter.entrySet().stream()
                .map(entry -> new StatDateCount(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * 各科室挂号人数占比，按数量降序，只返回有挂号记录的科室。
     */
    public List<StatDeptCount> departmentStats() {
        List<Appointment> appointments = activeAppointments();
        if (appointments.isEmpty()) {
            return List.of();
        }
        Map<Integer, Schedule> scheduleMap = loadSchedules(appointments);
        Map<Integer, Doctor> doctorMap = loadDoctors(scheduleMap.values());
        Map<Integer, Department> deptMap = loadDepartments(doctorMap.values());

        Map<String, Long> counter = new LinkedHashMap<>();
        for (Appointment appointment : appointments) {
            Department dept = departmentOf(appointment, scheduleMap, doctorMap, deptMap);
            if (dept != null) {
                counter.merge(dept.getDeptName(), 1L, Long::sum);
            }
        }
        return counter.entrySet().stream()
                .map(entry -> new StatDeptCount(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(StatDeptCount::getCount).reversed()
                        .thenComparing(StatDeptCount::getDeptName))
                .toList();
    }

    /**
     * 医生接诊量 Top5，只统计已诊挂号。
     */
    public List<StatDoctorCount> doctorTop5() {
        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(appointment -> appointment.getStatus() != null
                        && appointment.getStatus() == STATUS_ATTENDED)
                .toList();
        if (appointments.isEmpty()) {
            return List.of();
        }
        Map<Integer, Schedule> scheduleMap = loadSchedules(appointments);
        Map<Integer, Doctor> doctorMap = loadDoctors(scheduleMap.values());
        Map<Integer, Person> personMap = loadPersons(doctorMap.values());
        Map<Integer, Department> deptMap = loadDepartments(doctorMap.values());

        Map<Integer, Long> counter = new LinkedHashMap<>();
        for (Appointment appointment : appointments) {
            Schedule schedule = scheduleMap.get(appointment.getScheduleId());
            Doctor doctor = schedule == null ? null : doctorMap.get(schedule.getDoctorId());
            if (doctor != null) {
                counter.merge(doctor.getId(), 1L, Long::sum);
            }
        }
        return counter.entrySet().stream()
                .map(entry -> {
                    Doctor doctor = doctorMap.get(entry.getKey());
                    Person person = personMap.get(doctor.getUserId());
                    Department dept = deptMap.get(doctor.getDeptId());
                    return new StatDoctorCount(
                            doctor.getId(),
                            person == null ? null : person.getName(),
                            dept == null ? null : dept.getDeptName(),
                            entry.getValue());
                })
                .sorted(Comparator.comparingLong(StatDoctorCount::getCount).reversed()
                        .thenComparing(StatDoctorCount::getDoctorId))
                .limit(5)
                .toList();
    }

    private List<Appointment> activeAppointments() {
        return appointmentRepository.findAll().stream()
                .filter(appointment -> appointment.getStatus() != null
                        && appointment.getStatus() != STATUS_CANCELLED)
                .toList();
    }

    private Department departmentOf(Appointment appointment,
                                    Map<Integer, Schedule> scheduleMap,
                                    Map<Integer, Doctor> doctorMap,
                                    Map<Integer, Department> deptMap) {
        Schedule schedule = scheduleMap.get(appointment.getScheduleId());
        Doctor doctor = schedule == null ? null : doctorMap.get(schedule.getDoctorId());
        return doctor == null ? null : deptMap.get(doctor.getDeptId());
    }

    private Map<Integer, Schedule> loadSchedules(Collection<Appointment> appointments) {
        Set<Integer> ids = appointments.stream()
                .map(Appointment::getScheduleId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return scheduleRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Schedule::getId, schedule -> schedule));
    }

    private Map<Integer, Doctor> loadDoctors(Collection<Schedule> schedules) {
        Set<Integer> ids = schedules.stream()
                .map(Schedule::getDoctorId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return doctorRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Doctor::getId, doctor -> doctor));
    }

    private Map<Integer, Person> loadPersons(Collection<Doctor> doctors) {
        Set<Integer> ids = doctors.stream()
                .map(Doctor::getUserId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return personRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Person::getPersonId, person -> person));
    }

    private Map<Integer, Department> loadDepartments(Collection<Doctor> doctors) {
        Set<Integer> ids = doctors.stream()
                .map(Doctor::getDeptId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return departmentRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Department::getId, dept -> dept));
    }
}
