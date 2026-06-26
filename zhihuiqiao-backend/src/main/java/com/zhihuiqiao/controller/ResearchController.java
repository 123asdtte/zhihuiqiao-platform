package com.zhihuiqiao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhihuiqiao.annotation.OperationLogAnnotation;
import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.entity.*;
import com.zhihuiqiao.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 科研撮合模块 Controller
 * 提供科研画像、科研项目、项目申请、企业需求的 RESTful API
 */
@Tag(name = "科研撮合模块", description = "科研项目、企业需求、项目申请相关接口")
@RestController
@RequestMapping("/api/research")
@RequiredArgsConstructor
public class ResearchController {

    private final ResearcherProfileService researcherProfileService;
    private final ResearchProjectService researchProjectService;
    private final ProjectApplicationService projectApplicationService;
    private final EnterpriseDemandService enterpriseDemandService;
    private final SysUserService sysUserService;
    private final NotificationService notificationService;

    // ==================== 科研画像 ====================

    @Operation(summary = "创建或更新科研画像")
    @PostMapping("/profile")
    public Result<Long> saveOrUpdateProfile(@RequestBody @Valid ResearcherProfile profile) {
        // 科研画像仅面向学生与教师，企业账号无需维护科研画像
        String roleType = getCurrentRoleType();
        if (!"student".equals(roleType) && !"teacher".equals(roleType) && !"admin".equals(roleType)) {
            return Result.error("当前角色无法维护科研画像");
        }

        // 根据 userId 判断是新增还是更新
        LambdaQueryWrapper<ResearcherProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResearcherProfile::getUserId, profile.getUserId());
        ResearcherProfile existing = researcherProfileService.getOne(wrapper);
        if (existing != null) {
            profile.setId(existing.getId());
        }
        researcherProfileService.saveOrUpdate(profile);
        return Result.success(profile.getId());
    }

    @Operation(summary = "根据用户ID查询科研画像")
    @GetMapping("/profile/{userId}")
    public Result<ResearcherProfile> getProfileByUserId(@PathVariable Long userId) {
        // 非管理员只能查看自己的科研画像
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (!"admin".equals(currentRole) && !userId.equals(currentUserId)) {
            return Result.error("无权查看他人科研画像");
        }

        LambdaQueryWrapper<ResearcherProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResearcherProfile::getUserId, userId);
        return Result.success(researcherProfileService.getOne(wrapper));
    }

    // ==================== 科研项目 ====================

    @OperationLogAnnotation(module = "科研撮合", operation = "发布科研项目")
    @Operation(summary = "发布科研项目")
    @PostMapping("/project")
    public Result<Long> publishProject(@RequestBody @Valid ResearchProject project) {
        String roleType = getCurrentRoleType();
        // 科研项目允许教师、学生或管理员发布，发布后需经管理员审核
        if (!"teacher".equals(roleType) && !"student".equals(roleType) && !"admin".equals(roleType)) {
            return Result.error("无权发布科研项目");
        }

        // 从 SecurityContext 获取当前登录用户，设置发布者信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Long userId) {
            project.setPublisherId(userId);
            // 同步填充发布人姓名，避免详情/列表展示为"未知"
            SysUser publisher = sysUserService.getById(userId);
            if (publisher != null) {
                project.setPublisherName(publisher.getUsername());
            }
        }
        project.setPublisherType(roleType);

        // 设置默认值：未指定状态时默认为待审核，管理员审核通过后才变为招募中
        if (!StringUtils.hasText(project.getStatus())) {
            project.setStatus("pending_audit");
        }
        if (project.getCurrentMembers() == null) {
            project.setCurrentMembers(1);
        }
        if (project.getViews() == null) {
            project.setViews(0);
        }
        researchProjectService.save(project);
        return Result.success(project.getId());
    }

    /**
     * 获取当前登录用户角色类型
     */
    private String getCurrentRoleType() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "teacher";
        }
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", "").toLowerCase())
                .orElse("teacher");
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Long userId) {
            return userId;
        }
        return null;
    }

    /**
     * 补充项目发布人姓名，兼容历史数据 publisher_name 为空的情况
     */
    private void fillPublisherName(ResearchProject project) {
        if (project == null) {
            return;
        }
        if (!StringUtils.hasText(project.getPublisherName()) && project.getPublisherId() != null) {
            SysUser publisher = sysUserService.getById(project.getPublisherId());
            if (publisher != null) {
                project.setPublisherName(publisher.getUsername());
            }
        }
    }

    @Operation(summary = "分页查询科研项目列表")
    @GetMapping("/project/list")
    public Result<Page<ResearchProject>> listProjects(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String projectType,
            @RequestParam(required = false) String status) {

        Page<ResearchProject> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ResearchProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ResearchProject::getCreateTime);

        if (StringUtils.hasText(projectType)) {
            wrapper.eq(ResearchProject::getProjectType, projectType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(ResearchProject::getStatus, status);
        } else {
            // 默认只查询已审核通过的项目（招募中/进行中），待审核内容不在列表展示
            wrapper.in(ResearchProject::getStatus, List.of("recruiting", "ongoing"));
        }

        if (StringUtils.hasText(keyword)) {
            // 使用 MySQL 全文检索
            List<ResearchProject> searchResult = researchProjectService.searchByKeyword(keyword);
            if (!searchResult.isEmpty()) {
                List<Long> ids = searchResult.stream().map(ResearchProject::getId).toList();
                wrapper.in(ResearchProject::getId, ids);
            } else {
                // 没有全文检索结果时，使用模糊查询兜底
                wrapper.and(w -> w.like(ResearchProject::getProjectName, keyword)
                        .or()
                        .like(ResearchProject::getProjectDescription, keyword));
            }
        }

        Page<ResearchProject> result = researchProjectService.page(page, wrapper);
        result.getRecords().forEach(this::fillPublisherName);
        return Result.success(result);
    }

    @Operation(summary = "查询项目详情")
    @GetMapping("/project/{id}")
    public Result<ResearchProject> getProjectById(@PathVariable Long id) {
        ResearchProject project = researchProjectService.getById(id);
        if (project != null) {
            // 浏览量字段可能为空，需做防 null 处理
            project.setViews((project.getViews() == null ? 0 : project.getViews()) + 1);
            researchProjectService.updateById(project);
            fillPublisherName(project);
        }
        return Result.success(project);
    }

    @Operation(summary = "更新项目状态")
    @PutMapping("/project/{id}/status")
    public Result<Boolean> updateProjectStatus(@PathVariable Long id, @RequestParam String status) {
        ResearchProject project = new ResearchProject();
        project.setId(id);
        project.setStatus(status);
        return Result.success(researchProjectService.updateById(project));
    }

    @OperationLogAnnotation(module = "科研撮合", operation = "删除科研项目")
    @Operation(summary = "删除科研项目")
    @DeleteMapping("/project/{id}")
    public Result<Boolean> deleteProject(@PathVariable Long id) {
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        try {
            boolean success = researchProjectService.deleteProject(id, currentUserId, currentRole);
            return Result.success(success);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "查询我发布的科研项目")
    @GetMapping("/project/my")
    public Result<Page<ResearchProject>> listMyProjects(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {

        Long currentUserId = getCurrentUserId();
        Page<ResearchProject> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ResearchProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResearchProject::getPublisherId, currentUserId)
                .orderByDesc(ResearchProject::getCreateTime);

        if (StringUtils.hasText(status)) {
            wrapper.eq(ResearchProject::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ResearchProject::getProjectName, keyword);
        }

        Page<ResearchProject> result = researchProjectService.page(page, wrapper);
        result.getRecords().forEach(this::fillPublisherName);
        return Result.success(result);
    }

    // ==================== 项目申请 ====================

    @Operation(summary = "提交项目加入申请")
    @PostMapping("/application")
    public Result<Long> applyProject(@RequestBody @Valid ProjectApplication application) {
        String roleType = getCurrentRoleType();
        // 科研项目申请仅允许学生或管理员提交
        if (!"student".equals(roleType) && !"admin".equals(roleType)) {
            return Result.error("当前角色无法申请科研项目");
        }

        // 从当前登录用户设置申请人ID，防止前端伪造
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            application.setApplicantId(currentUserId);
        }

        // 防止重复申请
        LambdaQueryWrapper<ProjectApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectApplication::getProjectId, application.getProjectId())
                .eq(ProjectApplication::getApplicantId, application.getApplicantId())
                .ne(ProjectApplication::getStatus, "rejected");
        if (projectApplicationService.count(wrapper) > 0) {
            return Result.error("您已提交过申请，请勿重复申请");
        }

        application.setStatus("pending");
        projectApplicationService.save(application);

        // 发送通知给项目发布人
        ResearchProject project = researchProjectService.getById(application.getProjectId());
        if (project != null) {
            notificationService.sendNotification(
                    project.getPublisherId(),
                    "新的项目加入申请",
                    "您的项目「" + project.getProjectName() + "」收到了新的加入申请，请及时处理。",
                    "application",
                    application.getId(),
                    "project_application"
            );
        }

        return Result.success(application.getId());
    }

    @Operation(summary = "查询项目的申请列表")
    @GetMapping("/project/{projectId}/applications")
    public Result<List<ProjectApplication>> listApplicationsByProject(@PathVariable Long projectId) {
        // 校验当前用户是否为项目发布者或管理员，防止越权查看他人项目的申请
        ResearchProject project = researchProjectService.getById(projectId);
        if (project == null) {
            return Result.success(List.of());
        }
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (!"admin".equals(currentRole) && !project.getPublisherId().equals(currentUserId)) {
            return Result.error("无权查看该项目的申请列表");
        }

        LambdaQueryWrapper<ProjectApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectApplication::getProjectId, projectId)
                .orderByDesc(ProjectApplication::getCreateTime);
        return Result.success(projectApplicationService.list(wrapper));
    }

    @Operation(summary = "查询我的申请列表（管理员可查看全部申请）")
    @GetMapping("/application/my")
    public Result<List<ProjectApplication>> listMyApplications(@RequestParam(required = false) Long applicantId) {
        // 从 SecurityContext 获取当前登录用户ID与角色
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = authentication != null && authentication.getCredentials() instanceof Long id ? id : null;
        String currentRole = getCurrentRoleType();

        LambdaQueryWrapper<ProjectApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ProjectApplication::getCreateTime);

        if ("admin".equals(currentRole)) {
            // 管理员：可查看全部申请；如果传了 applicantId 则按申请人筛选
            if (applicantId != null) {
                wrapper.eq(ProjectApplication::getApplicantId, applicantId);
            }
        } else {
            // 非管理员：只能查看自己的申请，忽略传入的 applicantId
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            wrapper.eq(ProjectApplication::getApplicantId, currentUserId);
        }

        List<ProjectApplication> applications = projectApplicationService.list(wrapper);

        // 批量查询项目名称并填充到申请记录中
        if (!applications.isEmpty()) {
            List<Long> projectIds = applications.stream()
                    .map(ProjectApplication::getProjectId)
                    .distinct()
                    .toList();
            List<ResearchProject> projects = researchProjectService.listByIds(projectIds);
            java.util.Map<Long, String> projectNameMap = projects.stream()
                    .collect(java.util.stream.Collectors.toMap(ResearchProject::getId, ResearchProject::getProjectName));

            // 管理员查看全部申请时，批量查询申请人信息
            List<Long> applicantIds = applications.stream()
                    .map(ProjectApplication::getApplicantId)
                    .distinct()
                    .toList();
            java.util.Map<Long, String> applicantNameMap = new java.util.HashMap<>();
            if (!applicantIds.isEmpty()) {
                List<SysUser> users = sysUserService.listByIds(applicantIds);
                applicantNameMap = users.stream()
                        .collect(java.util.stream.Collectors.toMap(SysUser::getId,
                                u -> StringUtils.hasText(u.getRealName()) ? u.getRealName() : u.getUsername()));
            }

            for (ProjectApplication application : applications) {
                application.setProjectName(projectNameMap.getOrDefault(application.getProjectId(), "未知项目"));
                application.setApplicantName(applicantNameMap.getOrDefault(application.getApplicantId(), "未知用户"));
            }
        }

        return Result.success(applications);
    }

    @OperationLogAnnotation(module = "科研撮合", operation = "审核项目申请")
    @Operation(summary = "审核项目申请")
    @PutMapping("/application/{id}/audit")
    public Result<Boolean> auditApplication(@PathVariable Long id,
                                           @RequestParam String status,
                                           @RequestParam(required = false) String replyMessage) {
        ProjectApplication application = projectApplicationService.getById(id);
        if (application == null) {
            return Result.error("申请记录不存在");
        }

        // 仅项目发布者或管理员可审批该项目的加入申请
        ResearchProject project = researchProjectService.getById(application.getProjectId());
        if (project == null) {
            return Result.error("项目不存在");
        }
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (!"admin".equals(currentRole) && !project.getPublisherId().equals(currentUserId)) {
            return Result.error("无权审批该项目的申请");
        }

        application.setStatus(status);
        application.setReplyMessage(replyMessage);
        application.setHandleTime(LocalDateTime.now());
        boolean result = projectApplicationService.updateById(application);

        // 审核通过时，更新项目当前成员数
        if ("approved".equals(status) && project != null) {
            project.setCurrentMembers(project.getCurrentMembers() + 1);
            if (project.getCurrentMembers() >= project.getMaxMembers()) {
                project.setStatus("ongoing");
            }
            researchProjectService.updateById(project);
        }

        // 发送审核结果通知给申请人
        if (project != null) {
            String auditResult = "approved".equals(status) ? "已通过" : "未通过";
            notificationService.sendNotification(
                    application.getApplicantId(),
                    "项目申请" + auditResult,
                    "您对项目「" + project.getProjectName() + "」的加入申请" + auditResult +
                            (StringUtils.hasText(replyMessage) ? "，回复：" + replyMessage : "。"),
                    "application",
                    application.getId(),
                    "project_application"
            );
        }

        return Result.success(result);
    }

    // ==================== 企业需求 ====================

    @OperationLogAnnotation(module = "科研撮合", operation = "发布企业需求")
    @Operation(summary = "发布企业需求")
    @PostMapping("/demand")
    public Result<Long> publishDemand(@RequestBody @Valid EnterpriseDemand demand) {
        String roleType = getCurrentRoleType();
        // 企业需求仅允许企业或管理员发布
        if (!"enterprise".equals(roleType) && !"admin".equals(roleType)) {
            return Result.error("无权发布企业需求");
        }

        // 从 SecurityContext 获取当前登录用户，设置发布企业ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Long userId) {
            demand.setEnterpriseId(userId);
        }
        // 新发布的企业需求默认进入待审核状态，管理员审核通过后才对外开放
        demand.setStatus("pending_audit");
        if (demand.getViews() == null) {
            demand.setViews(0);
        }
        enterpriseDemandService.save(demand);
        return Result.success(demand.getId());
    }

    @Operation(summary = "分页查询企业需求列表")
    @GetMapping("/demand/list")
    public Result<Page<EnterpriseDemand>> listDemands(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String demandType,
            @RequestParam(required = false) String status) {

        Page<EnterpriseDemand> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<EnterpriseDemand> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(EnterpriseDemand::getCreateTime);

        if (StringUtils.hasText(demandType)) {
            wrapper.eq(EnterpriseDemand::getDemandType, demandType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(EnterpriseDemand::getStatus, status);
        } else {
            // 默认只查询已审核通过（进行中）的企业需求，待审核内容不在列表展示
            wrapper.eq(EnterpriseDemand::getStatus, "open");
        }

        if (StringUtils.hasText(keyword)) {
            List<EnterpriseDemand> searchResult = enterpriseDemandService.searchByKeyword(keyword);
            if (!searchResult.isEmpty()) {
                List<Long> ids = searchResult.stream().map(EnterpriseDemand::getId).toList();
                wrapper.in(EnterpriseDemand::getId, ids);
            } else {
                wrapper.and(w -> w.like(EnterpriseDemand::getDemandTitle, keyword)
                        .or()
                        .like(EnterpriseDemand::getDemandDescription, keyword));
            }
        }

        return Result.success(enterpriseDemandService.page(page, wrapper));
    }

    @Operation(summary = "查询企业需求详情")
    @GetMapping("/demand/{id}")
    public Result<EnterpriseDemand> getDemandById(@PathVariable Long id) {
        EnterpriseDemand demand = enterpriseDemandService.getById(id);
        if (demand != null) {
            demand.setViews(demand.getViews() + 1);
            enterpriseDemandService.updateById(demand);
            // 填充发布企业名称，便于前端展示
            SysUser enterprise = sysUserService.getById(demand.getEnterpriseId());
            if (enterprise != null) {
                demand.setPublisherName(enterprise.getUsername());
            }
        }
        return Result.success(demand);
    }
}
