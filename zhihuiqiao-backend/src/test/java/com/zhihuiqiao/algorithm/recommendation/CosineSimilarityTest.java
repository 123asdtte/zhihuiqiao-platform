package com.zhihuiqiao.algorithm.recommendation;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 余弦相似度计算器单元测试
 */
class CosineSimilarityTest {

    @Test
    void testCompute_identicalVectors() {
        Map<String, Double> v1 = new HashMap<>();
        v1.put("人工智能", 0.5);
        v1.put("机器学习", 0.3);
        v1.put("数据挖掘", 0.2);

        Map<String, Double> v2 = new HashMap<>();
        v2.put("人工智能", 0.5);
        v2.put("机器学习", 0.3);
        v2.put("数据挖掘", 0.2);

        double similarity = CosineSimilarity.compute(v1, v2);
        assertEquals(1.0, similarity, 1e-10);
    }

    @Test
    void testCompute_orthogonalVectors() {
        Map<String, Double> v1 = new HashMap<>();
        v1.put("人工智能", 1.0);

        Map<String, Double> v2 = new HashMap<>();
        v2.put("大数据", 1.0);

        double similarity = CosineSimilarity.compute(v1, v2);
        assertEquals(0.0, similarity, 1e-10);
    }

    @Test
    void testCompute_partiallyOverlapping() {
        Map<String, Double> v1 = new HashMap<>();
        v1.put("人工智能", 1.0);
        v1.put("机器学习", 1.0);

        Map<String, Double> v2 = new HashMap<>();
        v2.put("人工智能", 1.0);
        v2.put("数据挖掘", 1.0);

        double similarity = CosineSimilarity.compute(v1, v2);
        // cos = (1*1) / (sqrt(2) * sqrt(2)) = 1/2 = 0.5
        assertEquals(0.5, similarity, 1e-10);
    }

    @Test
    void testCompute_differentMagnitudes() {
        Map<String, Double> v1 = new HashMap<>();
        v1.put("人工智能", 2.0);
        v1.put("机器学习", 1.0);

        Map<String, Double> v2 = new HashMap<>();
        v2.put("人工智能", 4.0);
        v2.put("机器学习", 2.0);

        double similarity = CosineSimilarity.compute(v1, v2);
        // cos = (2*4 + 1*2) / (sqrt(4+1) * sqrt(16+4)) = 10 / (sqrt(5)*sqrt(20)) = 10/10 = 1.0
        assertEquals(1.0, similarity, 1e-10);
    }

    @Test
    void testCompute_nullVectorA() {
        Map<String, Double> v = new HashMap<>();
        v.put("人工智能", 1.0);

        assertEquals(-1.0, CosineSimilarity.compute(null, v));
    }

    @Test
    void testCompute_nullVectorB() {
        Map<String, Double> v = new HashMap<>();
        v.put("人工智能", 1.0);

        assertEquals(-1.0, CosineSimilarity.compute(v, null));
    }

    @Test
    void testCompute_emptyVectorA() {
        Map<String, Double> v = new HashMap<>();
        v.put("人工智能", 1.0);

        assertEquals(-1.0, CosineSimilarity.compute(new HashMap<>(), v));
    }

    @Test
    void testCompute_emptyVectorB() {
        Map<String, Double> v = new HashMap<>();
        v.put("人工智能", 1.0);

        assertEquals(-1.0, CosineSimilarity.compute(v, new HashMap<>()));
    }

    @Test
    void testCompute_zeroVector() {
        Map<String, Double> v1 = new HashMap<>();
        v1.put("人工智能", 0.0);

        Map<String, Double> v2 = new HashMap<>();
        v2.put("人工智能", 1.0);

        assertEquals(0.0, CosineSimilarity.compute(v1, v2), 1e-10);
    }

    @Test
    void testWeightedCompute_withWeights() {
        Map<String, Double> v1 = new HashMap<>();
        v1.put("人工智能", 1.0);
        v1.put("机器学习", 1.0);

        Map<String, Double> v2 = new HashMap<>();
        v2.put("人工智能", 1.0);
        v2.put("机器学习", 0.0);

        Map<String, Double> weights = new HashMap<>();
        weights.put("人工智能", 2.0);
        weights.put("机器学习", 1.0);

        double similarity = CosineSimilarity.weightedCompute(v1, v2, weights);
        // v1_weighted: 人工智能=2.0, 机器学习=1.0
        // v2_weighted: 人工智能=2.0, 机器学习=0.0
        // dot = 2*2 + 1*0 = 4
        // normA = sqrt(4+1) = sqrt(5)
        // normB = sqrt(4+0) = 2
        // similarity = 4 / (sqrt(5) * 2) = 4 / 4.4721 = 0.8944
        assertTrue(similarity > 0.8 && similarity < 0.9);
    }

    @Test
    void testWeightedCompute_nullWeights() {
        Map<String, Double> v1 = new HashMap<>();
        v1.put("人工智能", 1.0);

        Map<String, Double> v2 = new HashMap<>();
        v2.put("人工智能", 1.0);

        // weights中没有的term默认weight=1.0
        double similarity = CosineSimilarity.weightedCompute(v1, v2, new HashMap<>());
        assertEquals(1.0, similarity, 1e-10);
    }

    @Test
    void testWeightedCompute_nullVector() {
        Map<String, Double> v = new HashMap<>();
        v.put("人工智能", 1.0);

        assertEquals(-1.0, CosineSimilarity.weightedCompute(null, v, new HashMap<>()));
        assertEquals(-1.0, CosineSimilarity.weightedCompute(v, null, new HashMap<>()));
    }
}
