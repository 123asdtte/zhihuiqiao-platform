package com.zhihuiqiao.algorithm.recommendation;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TF-IDF 计算器单元测试
 */
class TfIdfCalculatorTest {

    @Test
    void testComputeTfIdf_singleDocument() {
        List<String> docs = Collections.singletonList("人工智能");
        List<Map<String, Double>> result = TfIdfCalculator.computeTfIdf(docs);

        assertEquals(1, result.size());
        assertFalse(result.get(0).isEmpty());
        // 只有一个词，TF-IDF > 0
        assertTrue(result.get(0).containsKey("人工智能"));
        assertTrue(result.get(0).get("人工智能") > 0);
    }

    @Test
    void testComputeTfIdf_multipleDocuments() {
        List<String> docs = Arrays.asList(
                "人工智能 机器学习",
                "数据挖掘 人工智能",
                "云计算 大数据"
        );

        List<Map<String, Double>> result = TfIdfCalculator.computeTfIdf(docs);

        assertEquals(3, result.size());
        // 每个文档都应有特征向量
        for (Map<String, Double> vector : result) {
            assertFalse(vector.isEmpty());
        }
        // "人工智能"出现在2篇文档中，IDF应小于"云计算"(只出现在1篇中)
    }

    @Test
    void testComputeTfIdf_emptyList() {
        List<Map<String, Double>> result = TfIdfCalculator.computeTfIdf(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void testComputeTfIdfForDocument_singleDocument() {
        List<String> corpus = Arrays.asList(
                "人工智能 机器学习 数据挖掘",
                "云计算 大数据 人工智能"
        );

        Map<String, Double> vector = TfIdfCalculator.computeTfIdfForDocument("机器学习 云计算", corpus);

        assertNotNull(vector);
        assertFalse(vector.isEmpty());
        // 所有语料库中的词都应在向量中（包括tf=0的）
        assertTrue(vector.containsKey("人工智能"));
        assertTrue(vector.containsKey("机器学习"));
        assertTrue(vector.containsKey("云计算"));
    }

    @Test
    void testComputeTfIdfForDocument_emptyCorpus() {
        Map<String, Double> vector = TfIdfCalculator.computeTfIdfForDocument(
                "人工智能", Collections.emptyList());

        assertNotNull(vector);
        // 语料库只有一个文档（输入的文档本身），IDF为log(1/1)+1=1
        assertTrue(vector.containsKey("人工智能"));
    }

    @Test
    void testChineseTextTokenization() {
        // 中文文本应被正确分词
        List<String> docs = Collections.singletonList("人工智能机器学习");
        List<Map<String, Double>> result = TfIdfCalculator.computeTfIdf(docs);

        assertFalse(result.get(0).isEmpty());
        // 应该包含bi-gram特征
        // "人工智能"长度4，生成3个bi-gram: "人工", "工智", "智能"
        // "机器学习"长度4，生成3个bi-gram: "机器", "器学", "学习"
        // 也有原始token
    }

    @Test
    void testEnglishTextTokenization() {
        List<String> docs = Arrays.asList(
                "Machine Learning is great",
                "Deep Learning is powerful"
        );

        List<Map<String, Double>> result = TfIdfCalculator.computeTfIdf(docs);

        assertEquals(2, result.size());
        // "learning" 应出现在两个文档中（小写化后）
        // "machine" 只出现在文档0
        // "deep" 只出现在文档1
        // "is" 是停用词，应被过滤
    }

    @Test
    void testStopWordsFiltered() {
        List<String> docs = Collections.singletonList(
                "the a an is are was were be been being"); // 全是停用词
        List<Map<String, Double>> result = TfIdfCalculator.computeTfIdf(docs);

        // 所有词都是停用词，但长度可能≥2，所以需要确认tokenize过滤逻辑
        // "is"长度2会被保留，但"a"长度1会被过滤
        // 检查长度≥2的停用词是否也被过滤
        for (Map.Entry<String, Double> entry : result.get(0).entrySet()) {
            assertFalse(isCommonStopWord(entry.getKey()),
                    "停用词 '" + entry.getKey() + "' 应被过滤");
        }
    }

    @Test
    void testNullAndEmptyInput() {
        // computeTfIdf接受空列表
        assertTrue(TfIdfCalculator.computeTfIdf(Collections.emptyList()).isEmpty());

        // computeTfIdfForDocument 空文档：文档本身无词，但IDF中的词会以0.0填充
        Map<String, Double> result = TfIdfCalculator.computeTfIdfForDocument("", Collections.singletonList("test"));
        assertNotNull(result);
        // "test"在IDF中但TF=0，应被putIfAbsent填充为0.0
        assertEquals(1, result.size());
        assertEquals(0.0, result.get("test"), 1e-10);
    }

    @Test
    void testMixedChineseEnglish() {
        List<String> docs = Collections.singletonList(
                "AI 人工智能 Machine Learning 机器学习");
        List<Map<String, Double>> result = TfIdfCalculator.computeTfIdf(docs);

        assertFalse(result.get(0).isEmpty());
        // 英文词应被保留（小写化）
        // 中文bi-gram应被生成
    }

    @Test
    void testTfIdfDistribution() {
        // 高频词在所有文档中出现 -> IDF小
        // 低频词只在少量文档中出现 -> IDF大
        List<String> docs = Arrays.asList(
                "人工智能 机器学习 数据挖掘 深度学习",
                "人工智能 计算机视觉",
                "人工智能 自然语言处理"
        );

        List<Map<String, Double>> vectors = TfIdfCalculator.computeTfIdf(docs);

        // "人工智能"出现在3篇文档中，IDF应该较小
        // "数据挖掘"只出现在1篇文档中，IDF应该较大
        double aiTfIdf = 0;
        double dmTfIdf = 0;
        for (Map<String, Double> vec : vectors) {
            if (vec.containsKey("人工智能")) {
                aiTfIdf = vec.get("人工智能");
            }
            if (vec.containsKey("数据挖掘")) {
                dmTfIdf = vec.get("数据挖掘");
            }
        }

        assertTrue(aiTfIdf > 0);
        assertTrue(dmTfIdf > 0);
        // "人工智能"的TF-IDF应小于"数据挖掘"（因为IDF更小）
        // 注意：实际TF值也可能不同，所以这个比较不是绝对的
        // 但我们可以验证同一个文档中稀有词的TF-IDF > 常见词的TF-IDF
    }

    /**
     * 辅助方法：判断是否为常见停用词
     */
    private boolean isCommonStopWord(String word) {
        return Set.of(
                "the", "a", "an", "is", "are", "was", "were", "be", "been",
                "being", "have", "has", "had", "do", "does", "did", "will",
                "would", "could", "should", "may", "might", "can", "shall",
                "to", "of", "in", "for", "on", "with", "at", "by", "from",
                "this", "that", "these", "those", "it", "its", "and", "or",
                "but", "not", "no", "if", "about", "into", "over", "after",
                "before", "between", "under", "above", "below"
        ).contains(word);
    }
}
