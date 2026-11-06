package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DoctorCreateRequest;
import cn.edu.sdu.java.server.payload.request.DoctorUpdateRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.DoctorService;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.RoleUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 医生查询对已登录用户开放，增删改仅 ADMIN。
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/doctors")
    public DataResponse listDoctors(@RequestParam(required = false) Integer deptId,
                                    @RequestParam(required = false) String keyword) {
        return CommonMethod.getReturnData(doctorService.listDoctors(deptId, keyword));
    }

    @GetMapping("/doctors/{id}")
    public DataResponse getDoctor(@PathVariable Integer id) {
        return CommonMethod.getReturnData(doctorService.getDoctor(id));
    }

    @PostMapping("/admin/doctors")
    public DataResponse createDoctor(@Valid @RequestBody DoctorCreateRequest request) {
        RoleUtil.requireAdmin();
        return CommonMethod.getReturnData(doctorService.createDoctor(request));
    }

    @PutMapping("/admin/doctors/{id}")
    public DataResponse updateDoctor(@PathVariable Integer id,
                                     @Valid @RequestBody DoctorUpdateRequest request) {
        RoleUtil.requireAdmin();
        doctorService.updateDoctor(id, request);
        return CommonMethod.getReturnMessageOK();
    }

    @DeleteMapping("/admin/doctors/{id}")
    public DataResponse deleteDoctor(@PathVariable Integer id) {
        RoleUtil.requireAdmin();
        doctorService.deleteDoctor(id);
        return CommonMethod.getReturnMessageOK();
    }
}
