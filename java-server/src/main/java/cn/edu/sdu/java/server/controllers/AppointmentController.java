package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.AppointmentCreateRequest;
import cn.edu.sdu.java.server.payload.request.CompleteAppointmentRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.AppointmentService;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.RoleUtil;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public DataResponse createAppointment(@Valid @RequestBody AppointmentCreateRequest request) {
        RoleUtil.requirePatient();
        return CommonMethod.getReturnData(appointmentService.createAppointment(request));
    }

    @GetMapping("/my")
    public DataResponse listMyAppointments(@RequestParam(required = false) Integer status) {
        RoleUtil.requirePatient();
        return CommonMethod.getReturnData(appointmentService.listMyAppointments(status));
    }

    @PutMapping("/{id}/cancel")
    public DataResponse cancelAppointment(@PathVariable Integer id) {
        RoleUtil.requirePatient();
        appointmentService.cancelAppointment(id);
        return CommonMethod.getReturnMessageOK();
    }

    @GetMapping("/doctor")
    public DataResponse listDoctorAppointments(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        RoleUtil.requireDoctor();
        return CommonMethod.getReturnData(
                appointmentService.listDoctorAppointments(status, date));
    }

    @PostMapping("/{id}/complete")
    public DataResponse completeAppointment(@PathVariable Integer id,
                                            @Valid @RequestBody CompleteAppointmentRequest request) {
        RoleUtil.requireDoctor();
        return CommonMethod.getReturnData(
                appointmentService.completeAppointment(id, request));
    }

    @GetMapping
    public DataResponse listAllAppointments(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer deptId,
            @RequestParam(required = false) Integer doctorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        RoleUtil.requireAdmin();
        return CommonMethod.getReturnData(
                appointmentService.listAllAppointments(date, deptId, doctorId, status, keyword));
    }

    @GetMapping("/{id}")
    public DataResponse getAppointment(@PathVariable Integer id) {
        return CommonMethod.getReturnData(appointmentService.getAppointment(id));
    }
}
