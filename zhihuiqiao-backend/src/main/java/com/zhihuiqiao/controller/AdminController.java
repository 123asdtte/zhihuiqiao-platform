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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统管理 Controller
 * 提供用户管理、内容审核等后台管理接口
 */
@Tag(name = "系统管理", description = "用户管理、内容审核等后台接口")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SysUserService sysUserService;
    private final ResearchProjectService researchProjectService;
    private final EnterpriseDemandService enterpriseDemandService;
    private final IdleResourceService idleResourceService;
    private final LearningResourceService learningResourceService;
    private final PasswordEncoder passwordEncoder;

    // ==================== 用户管理 ====================

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户列表")
    @GetMapping("/users")
    public Result<Page<SysUser>> listUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String roleType) {

        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SysUser::getCreateTime);

        if (StringUtils.hasText(roleType)) {
            wrapper.eq(SysUser::getRoleType, roleType);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or()
                    .like(SysUser::getRealName, keyword)
                    .or()
                    .like(SysUser::getEmail, keyword));
        }

        // 不返回密码字段
        Page<SysUser> result = sysUserService.page(page, wrapper);
        result.getRecords().forEach(user -> user.setPassword(null));
        return Result.success(result);
    }

    /**
     * 更新用户信息
     */
    @OperationLogAnnotation(module = "用户管理", operation = "更新用户信息")
    @Operation(summary = "更新用户信息")
    @PutMapping("/users/{id}")
    public Result<Boolean> updateUser(@PathVariable Long id, @RequestBody @Valid SysUser user) {
        user.setId(id);
        // 管理员更新时不修改密码，密码走重置接口
        user.setPassword(null);
        return Result.success(sysUserService.updateById(user));
    }

    /**
     * 删除用户
     */
    @OperationLogAnnotation(module = "用户管理", operation = "删除用户")
    @Operation(summary = "删除用户")
    @DeleteMapping("/users/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id) {
        return Result.success(sysUserService.removeById(id));
    }

    /**
     * 重置用户密码
     */
    @OperationLogAnnotation(module = "用户管理", operation = "重置用户密码", saveParams = false)
    @Operation(summary = "重置用户密码")
    @PutMapping("/users/{id}/reset-password")
    public Result<Boolean> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        return Result.success(sysUserService.updateById(user));
    }

    // ==================== 内容审核 ====================

    /**
     * 查询待审核内容统计
     */
    @Operation(summary = "查询各类内容统计")
    @GetMapping("/audit/stats")
    public Result<Map<String, Object>> getAuditStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("projectCount", researchProjectService.count());
        stats.put("demandCount", enterpriseDemandService.count());
        stats.put("resourceCount", idleResourceService.count());
        stats.put("learningResourceCount", learningResourceService.count());
        return Result.success(stats);
    }

    /**
     * 分页查询科研项目列表（管理后台）
     */
    @Operation(summary = "分页查询科研项目列表")
    @GetMapping("/audit/projects")
    public Result<Page<ResearchProject>> listProjectsForAudit(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        Page<ResearchProject> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ResearchProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ResearchProject::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ResearchProject::getProjectName, keyword);
        }
        return Result.success(researchProjectService.page(page, wrapper));
    }

    /**
     * 分页查询企业需求列表（管理后台）
     */
    @Operation(summary = "分页查询企业需求列表")
    @GetMapping("/audit/demands")
    public Result<Page<EnterpriseDemand>> listDemandsForAudit(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        Page<EnterpriseDemand> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<EnterpriseDemand> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(EnterpriseDemand::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(EnterpriseDemand::getDemandTitle, keyword);
        }
        return Result.success(enterpriseDemandService.page(page, wrapper));
    }

    /**
     * 分页查询闲置资源列表（管理后台）
     */
    @Operation(summary = "分页查询闲置资源列表")
    @GetMapping("/audit/resources")
    public Result<Page<IdleResource>> listResourcesForAudit(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        Page<IdleResource> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<IdleResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(IdleResource::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(IdleResource::getResourceName, keyword);
        }
        return Result.success(idleResourceService.page(page, wrapper));
    }

    /**
     * 分页查询学习资源列表（管理后台）
     */
    @Operation(summary = "分页查询学习资源列表")
    @GetMapping("/audit/learning-resources")
    public Result<Page<LearningResource>> listLearningResourcesForAudit(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {

        Page<LearningResource> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LearningResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(LearningResource::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(LearningResource::getResourceName, keyword);
        }
        return Result.success(learningResourceService.page(page, wrapper));
    }

    /**
     * 更新科研项目状态
     */
    @OperationLogAnnotation(module = "内容审核", operation = "审核科研项目")
    @Operation(summary = "更新科研项目状态")
    @PutMapping("/audit/project/{id}/status")
    public Result<Boolean> updateProjectStatus(@PathVariable Long id, @RequestParam String status) {
        ResearchProject project = new ResearchProject();
        project.setId(id);
        project.setStatus(status);
        return Result.success(researchProjectService.updateById(project));
    }

    /**
     * 更新企业需求状态
     */
    @OperationLogAnnotation(module = "内容审核", operation = "审核企业需求")
    @Operation(summary = "更新企业需求状态")
    @PutMapping("/audit/demand/{id}/status")
    public Result<Boolean> updateDemandStatus(@PathVariable Long id, @RequestParam String status) {
        EnterpriseDemand demand = new EnterpriseDemand();
        demand.setId(id);
        demand.setStatus(status);
        return Result.success(enterpriseDemandService.updateById(demand));
    }

    /**
     * 更新闲置资源状态
     */
    @OperationLogAnnotation(module = "内容审核", operation = "审核闲置资源")
    @Operation(summary = "更新闲置资源状态")
    @PutMapping("/audit/resource/{id}/status")
    public Result<Boolean> updateResourceStatus(@PathVariable Long id, @RequestParam String status) {
        IdleResource resource = new IdleResource();
        resource.setId(id);
        resource.setStatus(status);
        return Result.success(idleResourceService.updateById(resource));
    }

    /**
     * 更新学习资源状态（上架/下架）
     */
    @OperationLogAnnotation(module = "内容审核", operation = "审核学习资源")
    @Operation(summary = "更新学习资源状态")
    @PutMapping("/audit/learning-resource/{id}/status")
    public Result<Boolean> updateLearningResourceStatus(@PathVariable Long id, @RequestParam Integer status) {
        LearningResource resource = new LearningResource();
        resource.setId(id);
        resource.setStatus(status);
        return Result.success(learningResourceService.updateById(resource));
    }
}
