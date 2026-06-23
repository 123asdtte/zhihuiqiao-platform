package com.zhihuiqiao.algorithm.learningpath;

import com.zhihuiqiao.entity.KnowledgePoint;
import com.zhihuiqiao.entity.LearningRecord;
import com.zhihuiqiao.service.KnowledgePointService;
import com.zhihuiqiao.service.LearningRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习路径推荐服务
 * 
 * 基于知识图谱和拓扑排序算法，为学生生成个性化学习路径。
 * 结合学生已完成的学习记录，动态推荐下一个最佳学习知识点。
 */
@Service
@RequiredArgsConstructor
public class LearningPathService {

    private final KnowledgePointService knowledgePointService;
    private final LearningRecordService learningRecordService;

    /**
     * 生成个性化学习路径（完整推荐）
     *
     * @param userId     学生用户ID
     * @param courseName 课程名称
     * @return 推荐学习路径（按最优顺序）
     */
    public LearningPathResult generatePath(Long userId, String courseName) {
        // 1. 获取该课程的所有知识点
        List<KnowledgePoint> points = knowledgePointService.getByCourseName(courseName);
        if (points.isEmpty()) {
            return new LearningPathResult(courseName, Collections.emptyList(), 0, 0, false);
        }

        // 2. 获取学生已完成的知识点
        List<Long> completedIds = getCompletedPointIds(userId, courseName, points);

        // 3. 构建知识图谱并执行拓扑排序
        KnowledgeGraph graph = new KnowledgeGraph(points);

        // 4. 检测环
        if (graph.hasCycle()) {
            return new LearningPathResult(courseName, Collections.emptyList(), 
                    points.size(), completedIds.size(), true);
        }

        // 5. 生成个性化学习路径
        List<KnowledgeGraph.Node> pathNodes = graph.generateLearningPath(completedIds);

        // 6. 获取完整拓扑排序（用于前端展示）
        List<KnowledgeGraph.Node> fullSorted = graph.getFullSortedNodes();

        // 7. 转换为 VO
        List<PathItem> pathItems = pathNodes.stream()
                .map(this::toPathItem)
                .collect(Collectors.toList());

        return new LearningPathResult(courseName, pathItems,
                points.size(), completedIds.size(), false);
    }

    /**
     * 获取下一个推荐学习的知识点
     *
     * @param userId     学生用户ID
     * @param courseName 课程名称
     * @return 下一个推荐的知识点
     */
    public PathItem getNextRecommended(Long userId, String courseName) {
        LearningPathResult result = generatePath(userId, courseName);
        if (result.getPath() == null || result.getPath().isEmpty()) {
            return null;
        }
        return result.getPath().get(0);
    }

    /**
     * 获取按难度分层的推荐路径
     *
     * @param userId     学生用户ID
     * @param courseName 课程名称
     * @return 难度 -> 知识点列表
     */
    public Map<Integer, List<PathItem>> getLeveledPath(Long userId, String courseName) {
        List<KnowledgePoint> points = knowledgePointService.getByCourseName(courseName);
        if (points.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> completedIds = getCompletedPointIds(userId, courseName, points);
        KnowledgeGraph graph = new KnowledgeGraph(points);

        Map<Integer, List<KnowledgeGraph.Node>> leveledNodes = graph.generateLeveledPath(completedIds);

        Map<Integer, List<PathItem>> result = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<KnowledgeGraph.Node>> entry : leveledNodes.entrySet()) {
            List<PathItem> items = entry.getValue().stream()
                    .map(this::toPathItem)
                    .collect(Collectors.toList());
            result.put(entry.getKey(), items);
        }

        return result;
    }

    /**
     * 统计学习进度
     *
     * @param userId     学生用户ID
     * @param courseName 课程名称
     * @return 进度百分比 (0-100)
     */
    public int getProgress(Long userId, String courseName) {
        List<KnowledgePoint> points = knowledgePointService.getByCourseName(courseName);
        if (points.isEmpty()) {
            return 0;
        }

        // 获取该课程所有知识点的学习记录
        List<Long> pointIds = points.stream().map(KnowledgePoint::getId).collect(Collectors.toList());
        List<LearningRecord> records = learningRecordService.listByUserAndResources(userId, pointIds);

        long completedCount = records.stream()
                .filter(r -> "completed".equals(r.getStatus()))
                .count();

        return (int) (completedCount * 100 / points.size());
    }

    /**
     * 获取学生已完成的知识点ID列表
     */
    private List<Long> getCompletedPointIds(Long userId, String courseName, List<KnowledgePoint> points) {
        List<Long> pointIds = points.stream().map(KnowledgePoint::getId).collect(Collectors.toList());
        List<LearningRecord> records = learningRecordService.listByUserAndResources(userId, pointIds);

        // 状态为 completed 或 progress >= 100 的视为已完成
        Set<Long> completedIds = records.stream()
                .filter(r -> "completed".equals(r.getStatus()) || 
                        (r.getProgress() != null && r.getProgress() >= 100))
                .map(LearningRecord::getResourceId)
                .collect(Collectors.toSet());

        return new ArrayList<>(completedIds);
    }

    private PathItem toPathItem(KnowledgeGraph.Node node) {
        return new PathItem(node.getId(), node.getPointName(), 
                node.getDescription(), node.getDifficulty(), 
                node.getEstimatedMinutes());
    }

    // ==================== Inner VO Classes ====================

    /**
     * 推荐路径中的单个知识点项
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class PathItem {
        private Long pointId;
        private String pointName;
        private String description;
        private int difficulty;
        private int estimatedMinutes;
    }

    /**
     * 学习路径推荐结果
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class LearningPathResult {
        private String courseName;
        private List<PathItem> path;
        private int totalPoints;
        private int completedPoints;
        private boolean hasCycle;
    }
}
