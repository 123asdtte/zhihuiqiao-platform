package com.zhihuiqiao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhihuiqiao.annotation.OperationLogAnnotation;
import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.entity.LearningRecord;
import com.zhihuiqiao.entity.LearningResource;
import com.zhihuiqiao.service.LearningRecordService;
import com.zhihuiqiao.service.LearningResourceService;
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
 * 教学辅助模块 Controller
 * 提供学习资源、学习记录、收藏相关的 RESTful API
 */
@Tag(name = "教学辅助模块", description = "学习资源、学习记录、收藏相关接口")
@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {

    private final LearningResourceService learningResourceService;
    private final LearningRecordService learningRecordService;

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
     * 获取当前登录用户角色类型
     */
    private String getCurrentRoleType() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "";
        }
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", "").toLowerCase())
                .orElse("");
    }

    // ==================== 学习资源 ====================

    /**
     * 发布学习资源
     */
    @OperationLogAnnotation(module = "教学辅助", operation = "发布学习资源")
    @Operation(summary = "发布学习资源")
    @PostMapping("/resource")
    public Result<Long> publishResource(@RequestBody @Valid LearningResource resource) {
        // 学习资源仅允许教师或管理员发布
        String roleType = getCurrentRoleType();
        if (!"teacher".equals(roleType) && !"admin".equals(roleType)) {
            return Result.error("无权发布学习资源");
        }

        // 从 SecurityContext 获取当前登录用户 ID 设置为发布人
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Long userId) {
            resource.setPublisherId(userId);
        }
        // 新发布的学习资源默认进入待审核状态（status=2），管理员审核通过（status=1）后才展示
        if (resource.getStatus() == null) {
            resource.setStatus(2);
        }
        if (resource.getViews() == null) {
            resource.setViews(0);
        }
        if (resource.getLikes() == null) {
            resource.setLikes(0);
        }
        learningResourceService.save(resource);
        return Result.success(resource.getId());
    }

    /**
     * 分页查询学习资源列表
     */
    @Operation(summary = "分页查询学习资源列表")
    @GetMapping("/resource/list")
    public Result<Page<LearningResource>> listResources(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String difficultyLevel) {

        Page<LearningResource> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LearningResource> wrapper = new LambdaQueryWrapper<>();
        // 默认只查询已审核通过（status=1）的学习资源，待审核（status=2）内容不在列表展示
        wrapper.eq(LearningResource::getStatus, 1)
                .eq(LearningResource::getDeleted, 0)
                .orderByDesc(LearningResource::getCreateTime);

        if (StringUtils.hasText(resourceType)) {
            wrapper.eq(LearningResource::getResourceType, resourceType);
        }
        if (StringUtils.hasText(subject)) {
            wrapper.eq(LearningResource::getSubject, subject);
        }
        if (StringUtils.hasText(difficultyLevel)) {
            wrapper.eq(LearningResource::getDifficultyLevel, difficultyLevel);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(LearningResource::getResourceName, keyword)
                    .or()
                    .like(LearningResource::getDescription, keyword));
        }

        return Result.success(learningResourceService.page(page, wrapper));
    }

    /**
     * 查询学习资源详情
     */
    @Operation(summary = "查询学习资源详情")
    @GetMapping("/resource/{id}")
    public Result<LearningResource> getResourceById(@PathVariable Long id) {
        LearningResource resource = learningResourceService.getById(id);
        if (resource != null) {
            // 未审核通过的资源只有管理员或发布者本人可以查看详情
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Long currentUserId = authentication != null && authentication.getCredentials() instanceof Long uid ? uid : null;
            String currentRole = authentication != null && authentication.isAuthenticated()
                    ? authentication.getAuthorities().stream()
                    .findFirst()
                    .map(auth -> auth.getAuthority().replace("ROLE_", "").toLowerCase())
                    .orElse("")
                    : "";
            if (resource.getStatus() != null && resource.getStatus() != 1
                    && !"admin".equals(currentRole) && !resource.getPublisherId().equals(currentUserId)) {
                return Result.error("该资源暂未通过审核");
            }
            resource.setViews(resource.getViews() + 1);
            learningResourceService.updateById(resource);
        }
        return Result.success(resource);
    }

    /**
     * 删除学习资源
     */
    @OperationLogAnnotation(module = "教学辅助", operation = "删除学习资源")
    @Operation(summary = "删除学习资源")
    @DeleteMapping("/resource/{id}")
    public Result<Boolean> deleteResource(@PathVariable Long id) {
        // 仅资源发布者或管理员可删除
        LearningResource resource = learningResourceService.getById(id);
        if (resource == null) {
            return Result.error("学习资源不存在");
        }
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (!"admin".equals(currentRole) && !resource.getPublisherId().equals(currentUserId)) {
            return Result.error("无权删除该学习资源");
        }
        return Result.success(learningResourceService.removeById(id));
    }

    // ==================== 学习记录 ====================

    /**
     * 开始学习资源或更新进度
     */
    @Operation(summary = "开始学习资源或更新进度")
    @PostMapping("/record")
    public Result<Long> saveOrUpdateRecord(@RequestBody @Valid LearningRecord record) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Long userId) {
            record.setUserId(userId);
        }

        // 查询是否已存在学习记录
        LambdaQueryWrapper<LearningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningRecord::getUserId, record.getUserId())
                .eq(LearningRecord::getResourceId, record.getResourceId());
        LearningRecord existing = learningRecordService.getOne(wrapper);

        if (existing != null) {
            record.setId(existing.getId());
            record.setCreateTime(existing.getCreateTime());
        } else {
            record.setCreateTime(LocalDateTime.now());
        }

        // 进度达到 100 时自动标记完成
        if (record.getProgress() != null && record.getProgress() >= 100) {
            record.setStatus("completed");
            record.setCompleteTime(LocalDateTime.now());
        } else if (!StringUtils.hasText(record.getStatus())) {
            record.setStatus("learning");
        }

        record.setUpdateTime(LocalDateTime.now());
        learningRecordService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    /**
     * 查询我的学习记录
     */
    @Operation(summary = "查询我的学习记录")
    @GetMapping("/record/my")
    public Result<List<LearningRecord>> listMyRecords(@RequestParam(required = false) String status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (authentication != null && authentication.getCredentials() instanceof Long uid) {
            userId = uid;
        }

        LambdaQueryWrapper<LearningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getDeleted, 0)
                .orderByDesc(LearningRecord::getUpdateTime);
        if (StringUtils.hasText(status)) {
            wrapper.eq(LearningRecord::getStatus, status);
        }

        List<LearningRecord> records = learningRecordService.list(wrapper);
        // 填充资源信息
        records.forEach(record -> {
            LearningResource resource = learningResourceService.getById(record.getResourceId());
            if (resource != null) {
                record.setResourceName(resource.getResourceName());
                record.setCoverUrl(resource.getCoverUrl());
                record.setResourceType(resource.getResourceType());
            }
        });
        return Result.success(records);
    }

    /**
     * 收藏/取消收藏学习资源
     */
    @Operation(summary = "收藏或取消收藏学习资源")
    @PostMapping("/record/favorite/{resourceId}")
    public Result<Boolean> toggleFavorite(@PathVariable Long resourceId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (authentication != null && authentication.getCredentials() instanceof Long uid) {
            userId = uid;
        }

        LambdaQueryWrapper<LearningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningRecord::getUserId, userId)
                .eq(LearningRecord::getResourceId, resourceId);
        LearningRecord record = learningRecordService.getOne(wrapper);

        if (record == null) {
            LearningRecord favorite = new LearningRecord();
            favorite.setUserId(userId);
            favorite.setResourceId(resourceId);
            favorite.setStatus("favorite");
            favorite.setProgress(0);
            favorite.setLastPosition(0);
            favorite.setCreateTime(LocalDateTime.now());
            favorite.setUpdateTime(LocalDateTime.now());
            learningRecordService.save(favorite);
            return Result.success(true);
        }

        // 若当前已是收藏，则取消收藏；否则切换为收藏
        if ("favorite".equals(record.getStatus())) {
            learningRecordService.removeById(record.getId());
        } else {
            record.setStatus("favorite");
            record.setUpdateTime(LocalDateTime.now());
            learningRecordService.updateById(record);
        }
        return Result.success(true);
    }

    /**
     * 删除学习记录
     */
    @Operation(summary = "删除学习记录")
    @DeleteMapping("/record/{id}")
    public Result<Boolean> deleteRecord(@PathVariable Long id) {
        // 仅记录所有者或管理员可删除
        LearningRecord record = learningRecordService.getById(id);
        if (record == null) {
            return Result.error("学习记录不存在");
        }
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (!"admin".equals(currentRole) && !record.getUserId().equals(currentUserId)) {
            return Result.error("无权删除该学习记录");
        }
        return Result.success(learningRecordService.removeById(id));
    }
}
