package com.zhihuiqiao.algorithm.learningpath;

import com.zhihuiqiao.entity.KnowledgePoint;
import com.zhihuiqiao.entity.LearningRecord;
import com.zhihuiqiao.service.KnowledgePointService;
import com.zhihuiqiao.service.LearningRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 学习路径推荐服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class LearningPathServiceTest {

    @Mock
    private KnowledgePointService knowledgePointService;

    @Mock
    private LearningRecordService learningRecordService;

    @InjectMocks
    private LearningPathService learningPathService;

    private List<KnowledgePoint> mockPoints;
    private List<LearningRecord> mockRecords;

    @BeforeEach
    void setUp() {
        // A -> B -> C -> D 线性依赖
        mockPoints = new ArrayList<>();
        mockPoints.add(createPoint(1L, "基础知识", 1, null));
        mockPoints.add(createPoint(2L, "进阶知识", 2, "1"));
        mockPoints.add(createPoint(3L, "高级知识", 3, "2"));
        mockPoints.add(createPoint(4L, "专家知识", 4, "3"));

        // 学习记录：已完成基础知识
        mockRecords = new ArrayList<>();
        LearningRecord record = new LearningRecord();
        record.setId(1L);
        record.setUserId(100L);
        record.setResourceId(1L);
        record.setStatus("completed");
        record.setProgress(100);
        mockRecords.add(record);
    }

    @Test
    void testGeneratePath_withRecords() {
        when(knowledgePointService.getByCourseName("大数据"))
                .thenReturn(mockPoints);
        when(learningRecordService.listByUserAndResources(eq(100L), anyList()))
                .thenReturn(mockRecords);

        LearningPathService.LearningPathResult result =
                learningPathService.generatePath(100L, "大数据");

        assertNotNull(result);
        assertEquals("大数据", result.getCourseName());
        assertEquals(4, result.getTotalPoints());
        assertEquals(1, result.getCompletedPoints());
        assertFalse(result.isHasCycle());

        // 已完成基础知识，应推荐进阶知识 -> 高级知识 -> 专家知识
        List<LearningPathService.PathItem> path = result.getPath();
        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertEquals(2L, path.get(0).getPointId()); // 进阶知识
    }

    @Test
    void testGeneratePath_noRecords() {
        when(knowledgePointService.getByCourseName("大数据"))
                .thenReturn(mockPoints);
        when(learningRecordService.listByUserAndResources(eq(100L), anyList()))
                .thenReturn(Collections.emptyList());

        LearningPathService.LearningPathResult result =
                learningPathService.generatePath(100L, "大数据");

        assertNotNull(result);
        assertEquals(0, result.getCompletedPoints());
        assertFalse(result.getPath().isEmpty());
        assertEquals(1L, result.getPath().get(0).getPointId()); // 基础知识
    }

    @Test
    void testGeneratePath_noPoints() {
        when(knowledgePointService.getByCourseName("空课程"))
                .thenReturn(Collections.emptyList());

        LearningPathService.LearningPathResult result =
                learningPathService.generatePath(100L, "空课程");

        assertNotNull(result);
        assertTrue(result.getPath().isEmpty());
        assertEquals(0, result.getTotalPoints());
        assertFalse(result.isHasCycle());
    }

    @Test
    void testGetNextRecommended() {
        when(knowledgePointService.getByCourseName("大数据"))
                .thenReturn(mockPoints);
        when(learningRecordService.listByUserAndResources(eq(100L), anyList()))
                .thenReturn(mockRecords);

        LearningPathService.PathItem next =
                learningPathService.getNextRecommended(100L, "大数据");

        assertNotNull(next);
        assertEquals(2L, next.getPointId()); // 下一个应推荐进阶知识
        assertEquals("进阶知识", next.getPointName());
    }

    @Test
    void testGetNextRecommended_allCompleted() {
        // 所有知识点都已完成
        List<LearningRecord> allCompleted = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            LearningRecord r = new LearningRecord();
            r.setUserId(100L);
            r.setResourceId((long) i);
            r.setStatus("completed");
            r.setProgress(100);
            allCompleted.add(r);
        }

        when(knowledgePointService.getByCourseName("大数据"))
                .thenReturn(mockPoints);
        when(learningRecordService.listByUserAndResources(eq(100L), anyList()))
                .thenReturn(allCompleted);

        LearningPathService.PathItem next =
                learningPathService.getNextRecommended(100L, "大数据");

        assertNull(next);
    }

    @Test
    void testGetLeveledPath() {
        // 创建不同难度的知识点：简单(1) -> 进阶(2) -> 困难(5)
        // 让所有点形成一条链，使得空进度时所有节点都可达（因为拓扑排序会按顺序推荐）
        // 但generateLearningPath只返回前置已完成的节点
        // 空进度时只有无前置依赖的点(简单入门)可学
        KnowledgePoint easy1 = createPoint(1L, "简单入门", 1, null);
        KnowledgePoint hard1 = createPoint(3L, "高级算法", 5, "1");

        List<KnowledgePoint> variedPoints = Arrays.asList(easy1, hard1);

        when(knowledgePointService.getByCourseName("大数据"))
                .thenReturn(variedPoints);
        when(learningRecordService.listByUserAndResources(eq(100L), anyList()))
                .thenReturn(Collections.emptyList());

        Map<Integer, List<LearningPathService.PathItem>> leveled =
                learningPathService.getLeveledPath(100L, "大数据");

        assertNotNull(leveled);
        assertFalse(leveled.isEmpty());
        // 空进度时只有难度1的"简单入门"可学（无前置依赖）
        assertTrue(leveled.containsKey(1));
        // 所有点都已完成所有前置才会出现难度5
    }

    @Test
    void testGetLeveledPath_noPoints() {
        when(knowledgePointService.getByCourseName("空课程"))
                .thenReturn(Collections.emptyList());

        Map<Integer, List<LearningPathService.PathItem>> leveled =
                learningPathService.getLeveledPath(100L, "空课程");

        assertNotNull(leveled);
        assertTrue(leveled.isEmpty());
    }

    @Test
    void testGetProgress() {
        when(knowledgePointService.getByCourseName("大数据"))
                .thenReturn(mockPoints);
        when(learningRecordService.listByUserAndResources(eq(100L), anyList()))
                .thenReturn(mockRecords);

        int progress = learningPathService.getProgress(100L, "大数据");

        // 4个知识点中完成1个 = 25%
        assertEquals(25, progress);
    }

    @Test
    void testGetProgress_noPoints() {
        when(knowledgePointService.getByCourseName("空课程"))
                .thenReturn(Collections.emptyList());

        int progress = learningPathService.getProgress(100L, "空课程");

        assertEquals(0, progress);
    }

    @Test
    void testGetProgress_allCompleted() {
        List<LearningRecord> allCompleted = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            LearningRecord r = new LearningRecord();
            r.setUserId(100L);
            r.setResourceId((long) i);
            r.setStatus("completed");
            r.setProgress(100);
            allCompleted.add(r);
        }

        when(knowledgePointService.getByCourseName("大数据"))
                .thenReturn(mockPoints);
        when(learningRecordService.listByUserAndResources(eq(100L), anyList()))
                .thenReturn(allCompleted);

        int progress = learningPathService.getProgress(100L, "大数据");

        assertEquals(100, progress);
    }

    @Test
    void testPathItem_fields() {
        when(knowledgePointService.getByCourseName("大数据"))
                .thenReturn(mockPoints);
        when(learningRecordService.listByUserAndResources(eq(100L), anyList()))
                .thenReturn(mockRecords);

        LearningPathService.PathItem next =
                learningPathService.getNextRecommended(100L, "大数据");

        assertNotNull(next);
        assertTrue(next.getPointId() > 0);
        assertNotNull(next.getPointName());
        assertNotNull(next.getDescription());
        assertTrue(next.getDifficulty() >= 1);
        assertTrue(next.getEstimatedMinutes() >= 0);
    }

    /**
     * 创建测试用知识点
     */
    private KnowledgePoint createPoint(Long id, String name, int difficulty, String prerequisiteIds) {
        KnowledgePoint point = new KnowledgePoint();
        point.setId(id);
        point.setPointName(name);
        point.setDifficulty(difficulty);
        point.setPrerequisiteIds(prerequisiteIds);
        point.setEstimatedMinutes(30);
        point.setDescription(name + " description");
        point.setCourseName("大数据");
        return point;
    }
}
