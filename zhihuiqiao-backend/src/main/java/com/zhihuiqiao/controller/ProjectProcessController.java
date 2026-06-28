package com.zhihuiqiao.controller;

import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.entity.*;
import com.zhihuiqiao.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目过程管理 Controller
 * 提供项目动态、任务、成果归档、状态流转等接口
 */
@Tag(name = "项目过程管理", description = "项目动态、任务、成果、状态流转相关接口")
@RestController
@RequestMapping("/api/research/project")
@RequiredArgsConstructor
public class ProjectProcessController {

    private final ResearchProjectService researchProjectService;
    private final ProjectMemberService projectMemberService;
    private final ProjectDynamicService projectDynamicService;
    private final ProjectTaskService projectTaskService;
    private final ProjectOutcomeService projectOutcomeService;

    // ==================== 项目动态 ====================

    @Operation(summary = "发布项目动态")
    @PostMapping("/{projectId}/dynamic")
    public Result<Long> createDynamic(@PathVariable Long projectId, @RequestBody ProjectDynamic dynamic) {
        if (!StringUtils.hasText(dynamic.getContent())) {
            return Result.error("动态内容不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (!canManageProject(projectId, currentUserId)) {
            return Result.error("无权发布该项目动态");
        }
        dynamic.setProjectId(projectId);
        dynamic.setPublisherId(currentUserId);
        if (!StringUtils.hasText(dynamic.getDynamicType())) {
            dynamic.setDynamicType("announcement");
        }
        projectDynamicService.save(dynamic);
        return Result.success(dynamic.getId());
    }

    @Operation(summary = "查询项目动态列表")
    @GetMapping("/{projectId}/dynamics")
    public Result<List<ProjectDynamic>> listDynamics(@PathVariable Long projectId) {
        return Result.success(projectDynamicService.listByProjectId(projectId));
    }

    @Operation(summary = "删除项目动态")
    @DeleteMapping("/dynamic/{dynamicId}")
    public Result<Boolean> deleteDynamic(@PathVariable Long dynamicId) {
        ProjectDynamic dynamic = projectDynamicService.getById(dynamicId);
        if (dynamic == null) {
            return Result.error("动态不存在");
        }
        Long currentUserId = getCurrentUserId();
        if (!canManageProject(dynamic.getProjectId(), currentUserId) && !dynamic.getPublisherId().equals(currentUserId)) {
            return Result.error("无权删除该动态");
        }
        return Result.success(projectDynamicService.removeById(dynamicId));
    }

    // ==================== 项目任务 ====================

    @Operation(summary = "创建项目任务")
    @PostMapping("/{projectId}/task")
    public Result<Long> createTask(@PathVariable Long projectId, @RequestBody ProjectTask task) {
        if (!StringUtils.hasText(task.getTaskName())) {
            return Result.error("任务名称不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (!canManageProject(projectId, currentUserId)) {
            return Result.error("无权管理该项目任务");
        }
        task.setProjectId(projectId);
        if (!StringUtils.hasText(task.getStatus())) {
            task.setStatus("pending");
        }
        projectTaskService.save(task);
        return Result.success(task.getId());
    }

    @Operation(summary = "查询项目任务列表")
    @GetMapping("/{projectId}/tasks")
    public Result<List<ProjectTask>> listTasks(@PathVariable Long projectId) {
        return Result.success(projectTaskService.listByProjectId(projectId));
    }

    @Operation(summary = "更新任务状态")
    @PutMapping("/task/{taskId}/status")
    public Result<Boolean> updateTaskStatus(@PathVariable Long taskId, @RequestParam String status) {
        if (!List.of("pending", "in_progress", "completed").contains(status)) {
            return Result.error("无效的任务状态");
        }
        ProjectTask task = projectTaskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        Long currentUserId = getCurrentUserId();
        if (!canManageProject(task.getProjectId(), currentUserId) && !currentUserId.equals(task.getAssigneeId())) {
            return Result.error("无权更新该任务状态");
        }
        task.setStatus(status);
        return Result.success(projectTaskService.updateById(task));
    }

    @Operation(summary = "删除项目任务")
    @DeleteMapping("/task/{taskId}")
    public Result<Boolean> deleteTask(@PathVariable Long taskId) {
        ProjectTask task = projectTaskService.getById(taskId);
        if (task == null) {
            return Result.error("任务不存在");
        }
        Long currentUserId = getCurrentUserId();
        if (!canManageProject(task.getProjectId(), currentUserId)) {
            return Result.error("无权删除该任务");
        }
        return Result.success(projectTaskService.removeById(taskId));
    }

    // ==================== 项目成果 ====================

    @Operation(summary = "上传项目成果")
    @PostMapping("/{projectId}/outcome")
    public Result<Long> createOutcome(@PathVariable Long projectId, @RequestBody ProjectOutcome outcome) {
        if (!StringUtils.hasText(outcome.getOutcomeName())) {
            return Result.error("成果名称不能为空");
        }
        Long currentUserId = getCurrentUserId();
        if (!canManageProject(projectId, currentUserId)) {
            return Result.error("无权上传该项目成果");
        }
        outcome.setProjectId(projectId);
        if (!StringUtils.hasText(outcome.getOutcomeType())) {
            outcome.setOutcomeType("other");
        }
        projectOutcomeService.save(outcome);
        return Result.success(outcome.getId());
    }

    @Operation(summary = "查询项目成果列表")
    @GetMapping("/{projectId}/outcomes")
    public Result<List<ProjectOutcome>> listOutcomes(@PathVariable Long projectId) {
        return Result.success(projectOutcomeService.listByProjectId(projectId));
    }

    @Operation(summary = "删除项目成果")
    @DeleteMapping("/outcome/{outcomeId}")
    public Result<Boolean> deleteOutcome(@PathVariable Long outcomeId) {
        ProjectOutcome outcome = projectOutcomeService.getById(outcomeId);
        if (outcome == null) {
            return Result.error("成果不存在");
        }
        Long currentUserId = getCurrentUserId();
        if (!canManageProject(outcome.getProjectId(), currentUserId)) {
            return Result.error("无权删除该成果");
        }
        return Result.success(projectOutcomeService.removeById(outcomeId));
    }

    // ==================== 项目状态流转 ====================

    @Operation(summary = "开始项目：recruiting -> in_progress")
    @PutMapping("/{projectId}/start")
    public Result<Boolean> startProject(@PathVariable Long projectId) {
        ResearchProject project = researchProjectService.getById(projectId);
        if (project == null) {
            return Result.error("项目不存在");
        }
        Long currentUserId = getCurrentUserId();
        if (!canManageProject(projectId, currentUserId)) {
            return Result.error("无权操作该项目");
        }
        if (!"recruiting".equals(project.getStatus())) {
            return Result.error("当前项目状态无法开始");
        }
        project.setStatus("in_progress");
        project.setUpdateTime(LocalDateTime.now());
        return Result.success(researchProjectService.updateById(project));
    }

    @Operation(summary = "结项项目：in_progress -> completed")
    @PutMapping("/{projectId}/complete")
    public Result<Boolean> completeProject(@PathVariable Long projectId) {
        ResearchProject project = researchProjectService.getById(projectId);
        if (project == null) {
            return Result.error("项目不存在");
        }
        Long currentUserId = getCurrentUserId();
        if (!canManageProject(projectId, currentUserId)) {
            return Result.error("无权操作该项目");
        }
        if (!"in_progress".equals(project.getStatus())) {
            return Result.error("当前项目状态无法结项");
        }
        project.setStatus("completed");
        project.setUpdateTime(LocalDateTime.now());
        return Result.success(researchProjectService.updateById(project));
    }

    /**
     * 判断当前用户是否可以管理项目（管理员或项目负责人）
     */
    private boolean canManageProject(Long projectId, Long currentUserId) {
        if ("admin".equals(getCurrentRoleType())) {
            return true;
        }
        ResearchProject project = researchProjectService.getById(projectId);
        return project != null && project.getPublisherId().equals(currentUserId);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Long userId) {
            return userId;
        }
        return null;
    }

    private String getCurrentRoleType() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "student";
        }
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", "").toLowerCase())
                .orElse("student");
    }
}
