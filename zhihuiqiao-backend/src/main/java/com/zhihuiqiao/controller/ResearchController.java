package com.zhihuiqiao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.entity.*;
import com.zhihuiqiao.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    // ==================== 科研画像 ====================

    @Operation(summary = "创建或更新科研画像")
    @PostMapping("/profile")
    public Result<Long> saveOrUpdateProfile(@RequestBody @Valid ResearcherProfile profile) {
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
        LambdaQueryWrapper<ResearcherProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResearcherProfile::getUserId, userId);
        return Result.success(researcherProfileService.getOne(wrapper));
    }

    // ==================== 科研项目 ====================

    @Operation(summary = "发布科研项目")
    @PostMapping("/project")
    public Result<Long> publishProject(@RequestBody @Valid ResearchProject project) {
        project.setStatus("recruiting");
        project.setCurrentMembers(1);
        project.setViews(0);
        researchProjectService.save(project);
        return Result.success(project.getId());
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
            // 默认查询招募中或进行中的项目
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

        return Result.success(researchProjectService.page(page, wrapper));
    }

    @Operation(summary = "查询项目详情")
    @GetMapping("/project/{id}")
    public Result<ResearchProject> getProjectById(@PathVariable Long id) {
        ResearchProject project = researchProjectService.getById(id);
        if (project != null) {
            project.setViews(project.getViews() + 1);
            researchProjectService.updateById(project);
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

    // ==================== 项目申请 ====================

    @Operation(summary = "提交项目加入申请")
    @PostMapping("/application")
    public Result<Long> applyProject(@RequestBody @Valid ProjectApplication application) {
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
        return Result.success(application.getId());
    }

    @Operation(summary = "查询项目的申请列表")
    @GetMapping("/project/{projectId}/applications")
    public Result<List<ProjectApplication>> listApplicationsByProject(@PathVariable Long projectId) {
        LambdaQueryWrapper<ProjectApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectApplication::getProjectId, projectId)
                .orderByDesc(ProjectApplication::getCreateTime);
        return Result.success(projectApplicationService.list(wrapper));
    }

    @Operation(summary = "查询我的申请列表")
    @GetMapping("/application/my")
    public Result<List<ProjectApplication>> listMyApplications(@RequestParam Long applicantId) {
        LambdaQueryWrapper<ProjectApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectApplication::getApplicantId, applicantId)
                .orderByDesc(ProjectApplication::getCreateTime);
        return Result.success(projectApplicationService.list(wrapper));
    }

    @Operation(summary = "审核项目申请")
    @PutMapping("/application/{id}/audit")
    public Result<Boolean> auditApplication(@PathVariable Long id,
                                           @RequestParam String status,
                                           @RequestParam(required = false) String replyMessage) {
        ProjectApplication application = projectApplicationService.getById(id);
        if (application == null) {
            return Result.error("申请记录不存在");
        }

        application.setStatus(status);
        application.setReplyMessage(replyMessage);
        application.setHandleTime(LocalDateTime.now());
        boolean result = projectApplicationService.updateById(application);

        // 审核通过时，更新项目当前成员数
        if ("approved".equals(status)) {
            ResearchProject project = researchProjectService.getById(application.getProjectId());
            if (project != null) {
                project.setCurrentMembers(project.getCurrentMembers() + 1);
                if (project.getCurrentMembers() >= project.getMaxMembers()) {
                    project.setStatus("ongoing");
                }
                researchProjectService.updateById(project);
            }
        }

        return Result.success(result);
    }

    // ==================== 企业需求 ====================

    @Operation(summary = "发布企业需求")
    @PostMapping("/demand")
    public Result<Long> publishDemand(@RequestBody @Valid EnterpriseDemand demand) {
        demand.setStatus("open");
        demand.setViews(0);
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
        }
        return Result.success(demand);
    }
}
