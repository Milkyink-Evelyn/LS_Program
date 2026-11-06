package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.ScheduleCreateRequest;
import cn.edu.sdu.java.server.payload.request.ScheduleUpdateRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.ScheduleService;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.RoleUtil;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public DataResponse listSchedules(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) Integer doctorId,
            @RequestParam(required = false) Integer timeSlot) {
        return CommonMethod.getReturnData(
                scheduleService.listSchedules(date, deptId, doctorId, timeSlot));
    }

    @GetMapping("/my")
    public DataResponse listMySchedules(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        RoleUtil.requireDoctor();
        return CommonMethod.getReturnData(scheduleService.listMySchedules(date));
    }

    @GetMapping("/{id}")
    public DataResponse getSchedule(@PathVariable Integer id) {
        return CommonMethod.getReturnData(scheduleService.getSchedule(id));
    }

    @PostMapping
    public DataResponse createSchedule(@Valid @RequestBody ScheduleCreateRequest request) {
        RoleUtil.requireAdmin();
        return CommonMethod.getReturnData(scheduleService.createSchedule(request));
    }

    @PutMapping("/{id}")
    public DataResponse updateTotalQuota(@PathVariable Integer id,
                                         @Valid @RequestBody ScheduleUpdateRequest request) {
        RoleUtil.requireAdmin();
        scheduleService.updateTotalQuota(id, request);
        return CommonMethod.getReturnMessageOK();
    }

    @DeleteMapping("/{id}")
    public DataResponse deleteSchedule(@PathVariable Integer id) {
        RoleUtil.requireAdmin();
        scheduleService.deleteSchedule(id);
        return CommonMethod.getReturnMessageOK();
    }
}
