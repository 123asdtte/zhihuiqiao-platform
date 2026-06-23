package com.zhihuiqiao.algorithm.recommendation;

import java.util.Map;
import java.util.Set;

/**
 * 余弦相似度计算器
 * 
 * 计算两个向量之间的余弦夹角，值域为 [0, 1]
 * 值越接近 1 表示两个向量越相似
 * 
 * 公式: cos(θ) = (A·B) / (||A|| × ||B||)
 */
public class CosineSimilarity {

    /**
     * 计算两个 TF-IDF 向量之间的余弦相似度
     *
     * @param vectorA 向量 A（如：用户画像的 TF-IDF）
     * @param vectorB 向量 B（如：项目描述的 TF-IDF）
     * @return 余弦相似度 [0, 1]，-1 表示向量为空
     */
    public static double compute(Map<String, Double> vectorA, Map<String, Double> vectorB) {
        if (vectorA == null || vectorB == null || vectorA.isEmpty() || vectorB.isEmpty()) {
            return -1.0;
        }

        // 取两个向量词条的并集
        Set<String> allTerms = vectorA.keySet();
        allTerms = allTerms.size() > vectorB.keySet().size() ? allTerms : vectorB.keySet();

        double dotProduct = 0.0;   // 点积 A·B
        double normA = 0.0;        // 向量 A 的模 ||A||
        double normB = 0.0;        // 向量 B 的模 ||B||

        for (String term : vectorA.keySet()) {
            double valueA = vectorA.getOrDefault(term, 0.0);
            double valueB = vectorB.getOrDefault(term, 0.0);
            dotProduct += valueA * valueB;
            normA += valueA * valueA;
        }

        for (double value : vectorB.values()) {
            normB += value * value;
        }

        // 防止除零
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 计算两个向量的加权余弦相似度
     * 可以为不同特征维度赋予不同权重
     *
     * @param vectorA 向量 A
     * @param vectorB 向量 B
     * @param weights 特征权重
     * @return 加权余弦相似度
     */
    public static double weightedCompute(Map<String, Double> vectorA,
                                          Map<String, Double> vectorB,
                                          Map<String, Double> weights) {
        if (vectorA == null || vectorB == null || vectorA.isEmpty() || vectorB.isEmpty()) {
            return -1.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (String term : vectorA.keySet()) {
            double weight = weights.getOrDefault(term, 1.0);
            double valueA = vectorA.getOrDefault(term, 0.0) * weight;
            double valueB = vectorB.getOrDefault(term, 0.0) * weight;
            dotProduct += valueA * valueB;
            normA += valueA * valueA;
        }

        for (String term : vectorB.keySet()) {
            double weight = weights.getOrDefault(term, 1.0);
            double valueB = vectorB.get(term) * weight;
            normB += valueB * valueB;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
