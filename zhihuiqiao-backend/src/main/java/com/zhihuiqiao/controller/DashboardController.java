package com.zhihuiqiao.controller;

import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
     * 获取全平台统计数据（后台管理专用，仅管理员可查看）
     */
    @Operation(summary = "获取全平台统计数据")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        // 全平台统计数据属于后台管理范畴，仅管理员可查看
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentRole = authentication != null && authentication.isAuthenticated()
                ? authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", "").toLowerCase())
                .orElse("")
                : "";
        if (!"admin".equals(currentRole)) {
            return Result.error("无权查看全平台统计数据");
        }

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

    /**
     * 获取公开统计数据（首页展示，所有登录用户均可查看）
     * 仅返回聚合后的平台运营数据，不包含敏感信息
     */
    @Operation(summary = "获取公开统计数据")
    @GetMapping("/public-stats")
    public Result<Map<String, Object>> getPublicStats() {
        Map<String, Object> stats = new HashMap<>();

        // 平台用户总数
        long userCount = sysUserService.lambdaQuery()
                .eq(com.zhihuiqiao.entity.SysUser::getStatus, 1)
                .eq(com.zhihuiqiao.entity.SysUser::getDeleted, 0)
                .count();

        // 科研项目数（已审核通过的状态）
        long projectCount = researchProjectService.lambdaQuery()
                .ne(com.zhihuiqiao.entity.ResearchProject::getStatus, "pending_audit")
                .ne(com.zhihuiqiao.entity.ResearchProject::getStatus, "draft")
                .count();

        // 企业需求数（进行中或已完成）
        long demandCount = enterpriseDemandService.lambdaQuery()
                .ne(com.zhihuiqiao.entity.EnterpriseDemand::getStatus, "closed")
                .count();

        // 闲置资源数（可借用状态）
        long resourceCount = idleResourceService.lambdaQuery()
                .eq(com.zhihuiqiao.entity.IdleResource::getStatus, "available")
                .count();

        // 学习资源数（已启用）
        long learningResourceCount = learningResourceService.lambdaQuery()
                .eq(com.zhihuiqiao.entity.LearningResource::getStatus, 1)
                .count();

        // 资源预约数（已通过的预约）
        long bookingCount = resourceBookingService.lambdaQuery()
                .eq(com.zhihuiqiao.entity.ResourceBooking::getStatus, "approved")
                .count();

        stats.put("userCount", userCount);
        stats.put("projectCount", projectCount);
        stats.put("demandCount", demandCount);
        stats.put("resourceCount", resourceCount);
        stats.put("learningResourceCount", learningResourceCount);
        stats.put("bookingCount", bookingCount);

        return Result.success(stats);
    }
}
