package com.zhihuiqiao.algorithm.learningpath;

import com.zhihuiqiao.entity.KnowledgePoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 知识图谱算法单元测试
 */
class KnowledgeGraphTest {

    private List<KnowledgePoint> linearPoints;
    private List<KnowledgePoint> parallelPoints;
    private List<KnowledgePoint> cyclicPoints;

    @BeforeEach
    void setUp() {
        // 线性依赖: A -> B -> C -> D (A是B的前置，B是C的前置，C是D的前置)
        linearPoints = new ArrayList<>();
        linearPoints.add(createPoint(1L, "A", 1, null));
        linearPoints.add(createPoint(2L, "B", 2, "1"));
        linearPoints.add(createPoint(3L, "C", 3, "2"));
        linearPoints.add(createPoint(4L, "D", 4, "3"));

        // 并行依赖: A -> B, A -> C, B -> D, C -> D
        // A是B和C的前置，B和C是D的前置
        parallelPoints = new ArrayList<>();
        parallelPoints.add(createPoint(1L, "A", 1, null));
        parallelPoints.add(createPoint(2L, "B", 2, "1"));
        parallelPoints.add(createPoint(3L, "C", 3, "1"));
        parallelPoints.add(createPoint(4L, "D", 4, "2,3"));

        // 环形依赖: A -> B -> C -> A
        cyclicPoints = new ArrayList<>();
        cyclicPoints.add(createPoint(1L, "A", 1, "3"));  // A依赖C
        cyclicPoints.add(createPoint(2L, "B", 2, "1"));  // B依赖A
        cyclicPoints.add(createPoint(3L, "C", 3, "2"));  // C依赖B
    }

    @Test
    void testTopologicalSort_linearDependencies() {
        KnowledgeGraph graph = new KnowledgeGraph(linearPoints);
        KnowledgeGraph.TopologicalResult result = graph.topologicalSort();

        assertFalse(result.isHasCycle());
        assertEquals(4, result.getSortedNodes().size());

        // 验证拓扑顺序: A必须在B之前，B在C之前，C在D之前
        List<Long> sortedIds = result.getSortedNodes().stream()
                .map(KnowledgeGraph.Node::getId)
                .toList();

        assertTrue(sortedIds.indexOf(1L) < sortedIds.indexOf(2L));
        assertTrue(sortedIds.indexOf(2L) < sortedIds.indexOf(3L));
        assertTrue(sortedIds.indexOf(3L) < sortedIds.indexOf(4L));
    }

    @Test
    void testTopologicalSort_parallelDependencies() {
        KnowledgeGraph graph = new KnowledgeGraph(parallelPoints);
        KnowledgeGraph.TopologicalResult result = graph.topologicalSort();

        assertFalse(result.isHasCycle());
        assertEquals(4, result.getSortedNodes().size());

        // A在B和C之前，B和C在D之前
        List<Long> sortedIds = result.getSortedNodes().stream()
                .map(KnowledgeGraph.Node::getId)
                .toList();

        assertTrue(sortedIds.indexOf(1L) < sortedIds.indexOf(2L)); // A < B
        assertTrue(sortedIds.indexOf(1L) < sortedIds.indexOf(3L)); // A < C
        assertTrue(sortedIds.indexOf(2L) < sortedIds.indexOf(4L)); // B < D
        assertTrue(sortedIds.indexOf(3L) < sortedIds.indexOf(4L)); // C < D
    }

    @Test
    void testTopologicalSort_cycleDetection() {
        KnowledgeGraph graph = new KnowledgeGraph(cyclicPoints);
        KnowledgeGraph.TopologicalResult result = graph.topologicalSort();

        assertTrue(result.isHasCycle());
        // 环中节点数应为3
        assertEquals(3, result.getCycleNodeIds().size());
        assertTrue(result.getSortedNodes().isEmpty());
    }

    @Test
    void testHasCycle_noCycle() {
        KnowledgeGraph graph = new KnowledgeGraph(linearPoints);
        assertFalse(graph.hasCycle());
    }

    @Test
    void testHasCycle_withCycle() {
        KnowledgeGraph graph = new KnowledgeGraph(cyclicPoints);
        assertTrue(graph.hasCycle());
    }

    @Test
    void testGenerateLearningPath_withCompletedPoints() {
        KnowledgeGraph graph = new KnowledgeGraph(linearPoints);

        // 已完成A和B：B的前置A已完成，C的前置B已完成，但D的前置C未完成
        // 所以只有C是当前可学的
        List<KnowledgeGraph.Node> path = graph.generateLearningPath(Arrays.asList(1L, 2L));

        assertEquals(1, path.size());
        assertEquals(3L, path.get(0).getId()); // C
    }

    @Test
    void testGenerateLearningPath_noneCompleted() {
        KnowledgeGraph graph = new KnowledgeGraph(linearPoints);

        // 未完成任何知识点，应推荐A（唯一入度为0的节点）
        List<KnowledgeGraph.Node> path = graph.generateLearningPath(Collections.emptyList());

        assertEquals(1, path.size());
        assertEquals(1L, path.get(0).getId()); // A
    }

    @Test
    void testGenerateLearningPath_allCompleted() {
        KnowledgeGraph graph = new KnowledgeGraph(linearPoints);

        List<KnowledgeGraph.Node> path = graph.generateLearningPath(Arrays.asList(1L, 2L, 3L, 4L));

        assertTrue(path.isEmpty());
    }

    @Test
    void testGenerateLearningPath_withCycle() {
        KnowledgeGraph graph = new KnowledgeGraph(cyclicPoints);

        List<KnowledgeGraph.Node> path = graph.generateLearningPath(Collections.emptyList());

        assertTrue(path.isEmpty());
    }

    @Test
    void testGenerateLearningPath_prerequisitesNotMet() {
        KnowledgeGraph graph = new KnowledgeGraph(linearPoints);

        // 只完成了A，B的前置是A（已满足），但C的前置是B（未满足），D的前置是C（未满足）
        List<KnowledgeGraph.Node> path = graph.generateLearningPath(Arrays.asList(1L));

        assertEquals(1, path.size());
        assertEquals(2L, path.get(0).getId()); // B
    }

    @Test
    void testGenerateLeveledPath() {
        // 重新创建不同难度的知识点
        List<KnowledgePoint> points = new ArrayList<>();
        points.add(createPoint(1L, "简单知识", 1, null));
        points.add(createPoint(2L, "中等知识", 3, "1"));
        points.add(createPoint(3L, "困难知识", 5, "2"));

        KnowledgeGraph graph = new KnowledgeGraph(points);

        Map<Integer, List<KnowledgeGraph.Node>> leveledPath = graph.generateLeveledPath(Collections.emptyList());

        assertFalse(leveledPath.isEmpty());
        // 第一层难度应为1（简单知识）
        assertTrue(leveledPath.containsKey(1));
        // 应为TreeMap，按难度升序
        List<Integer> levels = new ArrayList<>(leveledPath.keySet());
        assertEquals(1, levels.get(0).intValue());
    }

    @Test
    void testGetFullSortedNodes() {
        KnowledgeGraph graph = new KnowledgeGraph(linearPoints);
        List<KnowledgeGraph.Node> sorted = graph.getFullSortedNodes();

        assertEquals(4, sorted.size());
        // 验证ID顺序
        assertEquals(1L, sorted.get(0).getId());
        assertEquals(2L, sorted.get(1).getId());
    }

    @Test
    void testSingleNodeGraph() {
        List<KnowledgePoint> points = Collections.singletonList(
                createPoint(1L, "Only", 1, null));
        KnowledgeGraph graph = new KnowledgeGraph(points);

        assertFalse(graph.hasCycle());
        assertEquals(1, graph.topologicalSort().getSortedNodes().size());
        assertEquals(1, graph.generateLearningPath(Collections.emptyList()).size());
    }

    @Test
    void testEmptyNodeGraph() {
        KnowledgeGraph graph = new KnowledgeGraph(Collections.emptyList());

        assertFalse(graph.hasCycle());
        assertTrue(graph.topologicalSort().getSortedNodes().isEmpty());
    }

    @Test
    void testNodePrerequisiteIds() {
        List<KnowledgePoint> points = new ArrayList<>();
        points.add(createPoint(1L, "A", 1, null));
        points.add(createPoint(2L, "B", 2, "1"));

        KnowledgeGraph graph = new KnowledgeGraph(points);
        List<KnowledgeGraph.Node> sorted = graph.getFullSortedNodes();

        KnowledgeGraph.Node nodeB = sorted.stream()
                .filter(n -> n.getId() == 2L)
                .findFirst()
                .orElseThrow();

        assertEquals(1, nodeB.getPrerequisiteIds().size());
        assertEquals(1L, nodeB.getPrerequisiteIds().get(0));
        assertEquals(2, nodeB.getDifficulty());
    }

    /**
     * 创建测试用的知识点
     */
    private KnowledgePoint createPoint(Long id, String name, int difficulty, String prerequisiteIds) {
        KnowledgePoint point = new KnowledgePoint();
        point.setId(id);
        point.setPointName(name);
        point.setDifficulty(difficulty);
        point.setPrerequisiteIds(prerequisiteIds);
        point.setEstimatedMinutes(30);
        point.setDescription(name + " description");
        point.setCourseName("测试课程");
        return point;
    }
}
