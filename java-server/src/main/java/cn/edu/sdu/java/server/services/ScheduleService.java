package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.exception.BusinessException;
import cn.edu.sdu.java.server.models.Department;
import cn.edu.sdu.java.server.models.Doctor;
import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.models.Schedule;
import cn.edu.sdu.java.server.payload.request.ScheduleCreateRequest;
import cn.edu.sdu.java.server.payload.request.ScheduleUpdateRequest;
import cn.edu.sdu.java.server.payload.response.ScheduleItem;
import cn.edu.sdu.java.server.repositorys.AppointmentRepository;
import cn.edu.sdu.java.server.repositorys.DepartmentRepository;
import cn.edu.sdu.java.server.repositorys.DoctorRepository;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import cn.edu.sdu.java.server.repositorys.ScheduleRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 排班管理。
 */
@Service
public class ScheduleService {

    private static final int CANCELLED = 2;

    private final ScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final PersonRepository personRepository;
    private final AppointmentRepository appointmentRepository;
    private final SystemService systemService;

    public ScheduleService(ScheduleRepository scheduleRepository,
                           DoctorRepository doctorRepository,
                           DepartmentRepository departmentRepository,
                           PersonRepository personRepository,
                           AppointmentRepository appointmentRepository,
                           SystemService systemService) {
        this.scheduleRepository = scheduleRepository;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.personRepository = personRepository;
        this.appointmentRepository = appointmentRepository;
        this.systemService = systemService;
    }

    /**
     * 排班列表：date 为空时返回今天起 7 天，否则只看当天。
     */
    public List<ScheduleItem> listSchedules(LocalDate date, Integer deptId,
                                            Integer doctorId, Integer timeSlot) {
        LocalDate start = date == null ? LocalDate.now() : date;
        LocalDate end = date == null ? start.plusDays(6) : date;
        List<Schedule> schedules = scheduleRepository
                .findByWorkDateBetweenOrderByWorkDateAscTimeSlotAsc(start, end).stream()
                .filter(schedule -> doctorId == null || doctorId.equals(schedule.getDoctorId()))
                .filter(schedule -> timeSlot == null || timeSlot.equals(schedule.getTimeSlot()))
                .toList();
        List<ScheduleItem> items = buildItems(schedules);
        if (deptId == null) {
            return items;
        }
        return items.stream()
                .filter(item -> deptId.equals(item.getDeptId()))
                .toList();
    }

    public ScheduleItem getSchedule(Integer id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在"));
        return buildItems(List.of(schedule)).get(0);
    }

    /**
     * 当前登录医生的排班。
     */
    public List<ScheduleItem> listMySchedules(LocalDate date) {
        Doctor doctor = doctorRepository.findByUserId(CommonMethod.getPersonId())
                .orElseThrow(() -> new BusinessException("医生信息不存在"));
        LocalDate start = date == null ? LocalDate.now() : date;
        LocalDate end = date == null ? start.plusDays(6) : date;
        return buildItems(scheduleRepository
                .findByDoctorIdAndWorkDateBetweenOrderByWorkDateAscTimeSlotAsc(
                        doctor.getId(), start, end));
    }

    @Transactional
    public Map<String, Object> createSchedule(ScheduleCreateRequest request) {
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new BusinessException("医生不存在"));
        if (request.getWorkDate().isBefore(LocalDate.now())) {
            throw new BusinessException("排班日期不能早于今天");
        }
        if (scheduleRepository.existsByDoctorIdAndWorkDateAndTimeSlot(
                doctor.getId(), request.getWorkDate(), request.getTimeSlot())) {
            throw new BusinessException("该医生当天该时段已排班");
        }
        int total = request.getTotalQuota();
        if (total <= 0) {
            throw new BusinessException("总号数必须大于 0");
        }
        Schedule schedule = new Schedule();
        schedule.setDoctorId(doctor.getId());
        schedule.setWorkDate(request.getWorkDate());
        schedule.setTimeSlot(request.getTimeSlot());
        schedule.setTotalQuota(total);
        schedule.setRemainQuota(total);
        scheduleRepository.saveAndFlush(schedule);
        systemService.modifyLog(schedule, true);
        return Map.of("id", schedule.getId());
    }

    /**
     * 修改总号数：剩余号源按 总号数 - 未取消挂号数 重算。
     */
    @Transactional
    public void updateTotalQuota(Integer id, ScheduleUpdateRequest request) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在"));
        int total = request.getTotalQuota();
        if (total <= 0) {
            throw new BusinessException("总号数必须大于 0");
        }
        long booked = appointmentRepository.countByScheduleIdAndStatusNot(id, CANCELLED);
        if (total < booked) {
            throw new BusinessException("总号数不能小于已挂号数");
        }
        schedule.setTotalQuota(total);
        schedule.setRemainQuota((int) (total - booked));
        scheduleRepository.save(schedule);
        systemService.modifyLog(schedule, false);
    }

    @Transactional
    public void deleteSchedule(Integer id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在"));
        if (appointmentRepository.countByScheduleId(id) > 0) {
            throw new BusinessException("该排班已有挂号记录，不能删除");
        }
        scheduleRepository.delete(schedule);
        systemService.modifyLog(schedule, false);
    }

    private List<ScheduleItem> buildItems(List<Schedule> schedules) {
        if (schedules.isEmpty()) {
            return List.of();
        }
        Set<Integer> doctorIds = schedules.stream()
                .map(Schedule::getDoctorId)
                .collect(Collectors.toSet());
        Map<Integer, Doctor> doctorMap = doctorRepository.findAllById(doctorIds).stream()
                .collect(Collectors.toMap(Doctor::getId, doctor -> doctor));
        Set<Integer> userIds = doctorMap.values().stream()
                .map(Doctor::getUserId)
                .collect(Collectors.toSet());
        Map<Integer, Person> personMap = personRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(Person::getPersonId, person -> person));
        Set<Integer> deptIds = doctorMap.values().stream()
                .map(Doctor::getDeptId)
                .collect(Collectors.toSet());
        Map<Integer, Department> deptMap = departmentRepository.findAllById(deptIds).stream()
                .collect(Collectors.toMap(Department::getId, dept -> dept));

        return schedules.stream().map(schedule -> {
            Doctor doctor = doctorMap.get(schedule.getDoctorId());
            Person person = doctor == null ? null : personMap.get(doctor.getUserId());
            Department dept = doctor == null ? null : deptMap.get(doctor.getDeptId());
            return new ScheduleItem(
                    schedule.getId(),
                    schedule.getDoctorId(),
                    person == null ? null : person.getName(),
                    doctor == null ? null : doctor.getDeptId(),
                    dept == null ? null : dept.getDeptName(),
                    doctor == null ? null : doctor.getTitle(),
                    schedule.getWorkDate(),
                    schedule.getTimeSlot(),
                    schedule.getTotalQuota(),
                    schedule.getRemainQuota());
        }).sorted(Comparator.comparing(ScheduleItem::getWorkDate)
                .thenComparing(ScheduleItem::getTimeSlot)
                .thenComparing(item -> item.getDoctorName() == null ? "" : item.getDoctorName()))
         .toList();
    }
}
