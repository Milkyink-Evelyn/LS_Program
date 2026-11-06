package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.ClinicStatisticsService;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.RoleUtil;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员统计接口，权限统一在方法内用 RoleUtil 校验。
 * 与模板原有的 StatisticsController（首页统计）共用 /api/statistics 前缀，方法路径不冲突。
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/statistics")
public class ClinicStatisticsController {

    private final ClinicStatisticsService clinicStatisticsService;

    public ClinicStatisticsController(ClinicStatisticsService clinicStatisticsService) {
        this.clinicStatisticsService = clinicStatisticsService;
    }

    @GetMapping("/appointments/last-7-days")
    public DataResponse last7Days() {
        RoleUtil.requireAdmin();
        return CommonMethod.getReturnData(clinicStatisticsService.last7Days());
    }

    @GetMapping("/departments")
    public DataResponse departmentStats() {
        RoleUtil.requireAdmin();
        return CommonMethod.getReturnData(clinicStatisticsService.departmentStats());
    }

    @GetMapping("/doctors/top5")
    public DataResponse doctorTop5() {
        RoleUtil.requireAdmin();
        return CommonMethod.getReturnData(clinicStatisticsService.doctorTop5());
    }
}
