package com.zhihuiqiao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhihuiqiao.algorithm.learningpath.LearningPathService;
import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.entity.KnowledgePoint;
import com.zhihuiqiao.entity.LearningRecord;
import com.zhihuiqiao.entity.LearningResource;
import com.zhihuiqiao.service.KnowledgePointService;
import com.zhihuiqiao.service.LearningRecordService;
import com.zhihuiqiao.service.LearningResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 学习辅助模块 Controller
 * 提供学习资源管理、学习记录、知识图谱和学习路径推荐 API
 */
@Tag(name = "学习辅助模块", description = "学习资源、学习记录、知识图谱、学习路径推荐")
@RestController
@RequestMapping("/learning")
@RequiredArgsConstructor
public class LearningController {

    private final KnowledgePointService knowledgePointService;
    private final LearningRecordService learningRecordService;
    private final LearningResourceService learningResourceService;
    private final LearningPathService learningPathService;

    // ==================== 知识点管理（算法：知识图谱） ====================

    @Operation(summary = "创建知识点")
    @PostMapping("/knowledge-point")
    public Result<Long> createKnowledgePoint(@RequestBody @Valid KnowledgePoint point) {
        knowledgePointService.save(point);
        return Result.success(point.getId());
    }

    @Operation(summary = "更新知识点")
    @PutMapping("/knowledge-point/{id}")
    public Result<Boolean> updateKnowledgePoint(@PathVariable Long id, @RequestBody KnowledgePoint point) {
        point.setId(id);
        return Result.success(knowledgePointService.updateById(point));
    }

    @Operation(summary = "根据课程名称查询知识点列表")
    @GetMapping("/knowledge-point/list")
    public Result<List<KnowledgePoint>> listKnowledgePoints(
            @RequestParam(required = false) String courseName) {
        if (StringUtils.hasText(courseName)) {
            return Result.success(knowledgePointService.getByCourseName(courseName));
        }
        return Result.success(knowledgePointService.list());
    }

    @Operation(summary = "查询知识点详情")
    @GetMapping("/knowledge-point/{id}")
    public Result<KnowledgePoint> getKnowledgePointById(@PathVariable Long id) {
        return Result.success(knowledgePointService.getById(id));
    }

    @Operation(summary = "删除知识点")
    @DeleteMapping("/knowledge-point/{id}")
    public Result<Boolean> deleteKnowledgePoint(@PathVariable Long id) {
        return Result.success(knowledgePointService.removeById(id));
    }

    // ==================== 学习路径推荐（算法：拓扑排序） ====================

    @Operation(summary = "【算法】生成个性化学习路径（拓扑排序）")
    @GetMapping("/path/recommend")
    public Result<LearningPathService.LearningPathResult> recommendLearningPath(
            @RequestParam Long userId,
            @RequestParam String courseName) {
        return Result.success(learningPathService.generatePath(userId, courseName));
    }

    @Operation(summary = "【算法】获取下一个推荐学习的知识点")
    @GetMapping("/path/next")
    public Result<LearningPathService.PathItem> getNextRecommended(
            @RequestParam Long userId,
            @RequestParam String courseName) {
        return Result.success(learningPathService.getNextRecommended(userId, courseName));
    }

    @Operation(summary = "【算法】获取按难度分层的推荐路径")
    @GetMapping("/path/leveled")
    public Result<Map<Integer, List<LearningPathService.PathItem>>> getLeveledPath(
            @RequestParam Long userId,
            @RequestParam String courseName) {
        return Result.success(learningPathService.getLeveledPath(userId, courseName));
    }

    @Operation(summary = "【算法】获取课程学习进度百分比")
    @GetMapping("/progress")
    public Result<Integer> getProgress(
            @RequestParam Long userId,
            @RequestParam String courseName) {
        return Result.success(learningPathService.getProgress(userId, courseName));
    }

    // ==================== 学习资源 ====================

    @Operation(summary = "分页查询学习资源")
    @GetMapping("/resource/list")
    public Result<Page<LearningResource>> listResources(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String resourceType) {
        Page<LearningResource> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<LearningResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(LearningResource::getCreateTime);
        if (StringUtils.hasText(subject)) {
            wrapper.eq(LearningResource::getSubject, subject);
        }
        if (StringUtils.hasText(resourceType)) {
            wrapper.eq(LearningResource::getResourceType, resourceType);
        }
        return Result.success(learningResourceService.page(page, wrapper));
    }

    // ==================== 学习记录 ====================

    @Operation(summary = "创建/更新学习记录")
    @PostMapping("/record")
    public Result<Long> saveLearningRecord(@RequestBody @Valid LearningRecord record) {
        // 检查是否已存在学习记录，存在则更新
        LambdaQueryWrapper<LearningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningRecord::getUserId, record.getUserId())
                .eq(LearningRecord::getResourceId, record.getResourceId());
        LearningRecord existing = learningRecordService.getOne(wrapper);
        if (existing != null) {
            record.setId(existing.getId());
            if ("completed".equals(record.getStatus()) && existing.getCompleteTime() == null) {
                record.setCompleteTime(LocalDateTime.now());
            }
        } else if ("completed".equals(record.getStatus())) {
            record.setCompleteTime(LocalDateTime.now());
        }
        learningRecordService.saveOrUpdate(record);
        return Result.success(record.getId());
    }

    @Operation(summary = "查询用户的学习记录列表")
    @GetMapping("/record/my")
    public Result<List<LearningRecord>> listMyRecords(@RequestParam Long userId) {
        LambdaQueryWrapper<LearningRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningRecord::getUserId, userId);
        wrapper.orderByDesc(LearningRecord::getUpdateTime);
        return Result.success(learningRecordService.list(wrapper));
    }
}
