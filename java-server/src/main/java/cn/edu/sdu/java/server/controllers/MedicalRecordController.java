package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.MedicalRecordService;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.RoleUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping("/my")
    public DataResponse listMyRecords(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        RoleUtil.requirePatient();
        return CommonMethod.getReturnData(medicalRecordService.listMyRecords(date));
    }

    @GetMapping("/appointment/{appointmentId}")
    public DataResponse getRecordByAppointment(@PathVariable Integer appointmentId) {
        return CommonMethod.getReturnData(
                medicalRecordService.getRecordByAppointment(appointmentId));
    }
}
