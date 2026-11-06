package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DepartmentRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.DepartmentService;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.RoleUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public DataResponse listDepartments() {
        return CommonMethod.getReturnData(departmentService.listDepartments());
    }

    @PostMapping
    public DataResponse createDepartment(@Valid @RequestBody DepartmentRequest request) {
        RoleUtil.requireAdmin();
        return CommonMethod.getReturnData(departmentService.createDepartment(request));
    }

    @PutMapping("/{id}")
    public DataResponse updateDepartment(@PathVariable Integer id,
                                         @Valid @RequestBody DepartmentRequest request) {
        RoleUtil.requireAdmin();
        departmentService.updateDepartment(id, request);
        return CommonMethod.getReturnMessageOK();
    }

    @DeleteMapping("/{id}")
    public DataResponse deleteDepartment(@PathVariable Integer id) {
        RoleUtil.requireAdmin();
        departmentService.deleteDepartment(id);
        return CommonMethod.getReturnMessageOK();
    }
}
