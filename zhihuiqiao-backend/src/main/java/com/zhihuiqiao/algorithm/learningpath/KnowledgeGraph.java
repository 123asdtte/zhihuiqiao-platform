package com.zhihuiqiao.algorithm.learningpath;

import com.zhihuiqiao.entity.KnowledgePoint;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 知识图谱构建器
 * 
 * 将知识点及其前置依赖关系构建为有向无环图(DAG)
 * 使用 Kahn 算法进行拓扑排序，生成最优学习路径
 * 
 * 算法原理：
 * 1. 每个知识点作为图的一个节点
 * 2. 前置依赖关系作为有向边 A→B（A是B的前置知识点）
 * 3. 拓扑排序保证：学习B之前，所有前置知识A都已完成
 * 4. 复杂度：O(V + E)，其中V为知识点数，E为依赖关系数
 */
public class KnowledgeGraph {

    /**
     * 知识点节点
     */
    @Getter
    public static class Node {
        private final Long id;
        private final String pointName;
        private final String description;
        private final int difficulty;
        private final int estimatedMinutes;
        private final List<Long> prerequisiteIds;
        
        /** 入度（有多少条边指向该节点） */
        private int inDegree = 0;
        
        /** 出边列表（该节点指向的其他节点ID） */
        private final List<Long> outEdges = new ArrayList<>();

        public Node(KnowledgePoint point) {
            this.id = point.getId();
            this.pointName = point.getPointName();
            this.description = point.getDescription();
            this.difficulty = point.getDifficulty() != null ? point.getDifficulty() : 1;
            this.estimatedMinutes = point.getEstimatedMinutes() != null ? point.getEstimatedMinutes() : 30;
            this.prerequisiteIds = parsePrerequisiteIds(point.getPrerequisiteIds());
        }

        private List<Long> parsePrerequisiteIds(String ids) {
            if (ids == null || ids.trim().isEmpty()) {
                return Collections.emptyList();
            }
            return Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
    }

    /**
     * 拓扑排序结果
     */
    @Getter
    public static class TopologicalResult {
        /** 排序后的知识点列表（学习顺序） */
        private final List<Node> sortedNodes;
        
        /** 是否存在环 */
        private final boolean hasCycle;
        
        /** 如果存在环，给出环中的节点ID */
        private final List<Long> cycleNodeIds;

        public TopologicalResult(List<Node> sortedNodes, boolean hasCycle, List<Long> cycleNodeIds) {
            this.sortedNodes = sortedNodes;
            this.hasCycle = hasCycle;
            this.cycleNodeIds = cycleNodeIds;
        }
    }

    /** 节点映射：id -> Node */
    private final Map<Long, Node> nodeMap = new HashMap<>();

    /** 所有节点列表 */
    private final List<Node> allNodes;

    /**
     * 从知识点列表构建知识图谱
     *
     * @param knowledgePoints 同课程下的所有知识点
     */
    public KnowledgeGraph(List<KnowledgePoint> knowledgePoints) {
        this.allNodes = knowledgePoints.stream()
                .map(Node::new)
                .collect(Collectors.toList());
        
        // 构建 id -> Node 映射
        for (Node node : allNodes) {
            nodeMap.put(node.getId(), node);
        }

        // 构建邻接表和入度
        for (Node node : allNodes) {
            for (Long prereqId : node.getPrerequisiteIds()) {
                Node prereqNode = nodeMap.get(prereqId);
                if (prereqNode != null) {
                    // 前置知识点 → 当前知识点（B的前置是A，A→B）
                    prereqNode.getOutEdges().add(node.getId());
                    node.inDegree++;
                }
            }
        }
    }

    /**
     * 执行拓扑排序（Kahn 算法）
     * 
     * Kahn 算法步骤：
     * 1. 将所有入度为0的节点加入队列
     * 2. 从队列取出一个节点，加入结果列表
     * 3. 将该节点的所有邻接节点入度减1
     * 4. 如果邻接节点入度变为0，加入队列
     * 5. 重复2-4直到队列为空
     * 6. 如果结果列表长度不等于节点总数，说明存在环
     *
     * @return 拓扑排序结果
     */
    public TopologicalResult topologicalSort() {
        List<Node> sorted = new ArrayList<>();
        Queue<Node> queue = new LinkedList<>();

        // 1. 所有入度为0的节点入队
        for (Node node : allNodes) {
            if (node.getInDegree() == 0) {
                queue.offer(node);
            }
        }

        // 2. BFS 拓扑排序
        while (!queue.isEmpty()) {
            Node current = queue.poll();
            sorted.add(current);

            // 将所有邻接节点入度减1
            for (Long neighborId : current.getOutEdges()) {
                Node neighbor = nodeMap.get(neighborId);
                if (neighbor != null) {
                    neighbor.inDegree--;
                    if (neighbor.inDegree == 0) {
                        queue.offer(neighbor);
                    }
                }
            }
        }

        // 3. 检测环
        boolean hasCycle = sorted.size() < allNodes.size();
        List<Long> cycleNodes = new ArrayList<>();

        if (hasCycle) {
            // 找出仍在环中的节点（入度 > 0 的节点）
            for (Node node : allNodes) {
                if (node.getInDegree() > 0) {
                    cycleNodes.add(node.getId());
                }
            }
        }

        return new TopologicalResult(sorted, hasCycle, cycleNodes);
    }

    /**
     * 根据已学知识点生成个性化学习路径
     *
     * @param completedPointIds 已学完的知识点ID列表
     * @return 推荐的学习序列（按最优顺序）
     */
    public List<Node> generateLearningPath(List<Long> completedPointIds) {
        Set<Long> completed = new HashSet<>(completedPointIds);
        TopologicalResult result = topologicalSort();

        if (result.isHasCycle()) {
            // 存在环，无法生成完整路径
            return Collections.emptyList();
        }

        // 过滤：只返回未学且前置知识已学完的节点
        List<Node> path = new ArrayList<>();
        for (Node node : result.getSortedNodes()) {
            if (completed.contains(node.getId())) {
                continue; // 已学，跳过
            }
            // 检查前置知识是否全部完成
            boolean allPrerequisitesMet = node.getPrerequisiteIds().stream()
                    .allMatch(completed::contains);
            if (allPrerequisitesMet) {
                path.add(node);
            }
        }

        return path;
    }

    /**
     * 按难度分层的学习路径
     * 先学简单的前置知识，再学高级知识
     *
     * @param completedPointIds 已学完的知识点ID列表
     * @return 分层推荐路径
     */
    public Map<Integer, List<Node>> generateLeveledPath(List<Long> completedPointIds) {
        List<Node> path = generateLearningPath(completedPointIds);
        if (path.isEmpty()) {
            return Collections.emptyMap();
        }

        // 按难度分组
        return path.stream()
                .collect(Collectors.groupingBy(
                        Node::getDifficulty,
                        TreeMap::new,  // 按难度升序排列
                        Collectors.toList()
                ));
    }

    /**
     * 获取全量拓扑排序的结果（用于前端展示知识图谱）
     */
    public List<Node> getFullSortedNodes() {
        TopologicalResult result = topologicalSort();
        return result.getSortedNodes();
    }

    /**
     * 检测依赖关系是否存在环
     */
    public boolean hasCycle() {
        return topologicalSort().isHasCycle();
    }
}
