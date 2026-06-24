package com.zhihuiqiao.controller;

import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据看板 Controller
 * 提供首页与后台管理所需的统计数据接口
 */
@Tag(name = "数据看板", description = "首页与管理后台统计指标接口")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final SysUserService sysUserService;
    private final ResearchProjectService researchProjectService;
    private final EnterpriseDemandService enterpriseDemandService;
    private final IdleResourceService idleResourceService;
    private final ResourceBookingService resourceBookingService;
    private final ProjectApplicationService projectApplicationService;
    private final LearningResourceService learningResourceService;

    /**
     * 获取全平台统计数据
     */
    @Operation(summary = "获取全平台统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 用户统计
        long userCount = sysUserService.count();
        long studentCount = sysUserService.lambdaQuery().eq(com.zhihuiqiao.entity.SysUser::getRoleType, "student").count();
        long teacherCount = sysUserService.lambdaQuery().eq(com.zhihuiqiao.entity.SysUser::getRoleType, "teacher").count();
        long enterpriseCount = sysUserService.lambdaQuery().eq(com.zhihuiqiao.entity.SysUser::getRoleType, "enterprise").count();

        // 科研撮合统计
        long projectCount = researchProjectService.count();
        long demandCount = enterpriseDemandService.count();
        long applicationCount = projectApplicationService.count();

        // 资源流转统计
        long resourceCount = idleResourceService.count();
        long bookingCount = resourceBookingService.count();

        // 教学辅助统计
        long learningResourceCount = learningResourceService.count();

        stats.put("userCount", userCount);
        stats.put("studentCount", studentCount);
        stats.put("teacherCount", teacherCount);
        stats.put("enterpriseCount", enterpriseCount);
        stats.put("projectCount", projectCount);
        stats.put("demandCount", demandCount);
        stats.put("applicationCount", applicationCount);
        stats.put("resourceCount", resourceCount);
        stats.put("bookingCount", bookingCount);
        stats.put("learningResourceCount", learningResourceCount);

        return Result.success(stats);
    }
}
