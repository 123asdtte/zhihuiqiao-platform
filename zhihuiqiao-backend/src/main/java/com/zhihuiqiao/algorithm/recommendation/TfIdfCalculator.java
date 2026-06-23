package com.zhihuiqiao.algorithm.recommendation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TF-IDF 计算器
 * 将文本转换为 TF-IDF 特征向量，用于文本相似度计算
 * 
 * 算法原理：
 * TF(词频) = 词条在文档中出现的次数 / 文档总词条数
 * IDF(逆文档频率) = log(总文档数 / 包含该词条的文档数 + 1)
 * TF-IDF = TF * IDF
 */
public class TfIdfCalculator {

    /**
     * 计算语料库中所有文档的 TF-IDF 向量
     *
     * @param documents 文档列表，每个文档是一个字符串
     * @return 每个文档的 TF-IDF 向量（Map: 词条 -> TF-IDF值）
     */
    public static List<Map<String, Double>> computeTfIdf(List<String> documents) {
        // 1. 对所有文档进行分词
        List<List<String>> tokenizedDocs = documents.stream()
                .map(TfIdfCalculator::tokenize)
                .collect(Collectors.toList());

        // 2. 计算每个文档的 TF
        List<Map<String, Double>> tfResults = tokenizedDocs.stream()
                .map(TfIdfCalculator::computeTf)
                .collect(Collectors.toList());

        // 3. 计算 IDF
        Map<String, Double> idf = computeIdf(tokenizedDocs);

        // 4. 计算 TF-IDF
        List<Map<String, Double>> tfidfResults = new ArrayList<>();
        for (Map<String, Double> tf : tfResults) {
            Map<String, Double> tfidf = new HashMap<>();
            for (Map.Entry<String, Double> entry : tf.entrySet()) {
                String term = entry.getKey();
                double idfValue = idf.getOrDefault(term, 0.0);
                // TF-IDF = TF * IDF
                tfidf.put(term, entry.getValue() * idfValue);
            }
            tfidfResults.add(tfidf);
        }

        return tfidfResults;
    }

    /**
     * 计算单个文档和语料库之间的 TF-IDF 向量
     * 用于新文档与已有语料库的匹配场景
     *
     * @param document  待匹配文档
     * @param documents 已有语料库
     * @return 该文档的 TF-IDF 向量
     */
    public static Map<String, Double> computeTfIdfForDocument(String document, List<String> documents) {
        // 构建包含新文档的完整语料库
        List<String> allDocs = new ArrayList<>(documents);
        allDocs.add(document);
        List<List<String>> tokenizedAll = allDocs.stream()
                .map(TfIdfCalculator::tokenize)
                .collect(Collectors.toList());

        // 新文档的分词结果
        List<String> docTokens = tokenize(document);
        Map<String, Double> tf = computeTf(docTokens);

        // 使用完整语料库计算 IDF
        Map<String, Double> idf = computeIdf(tokenizedAll);

        // 计算 TF-IDF
        Map<String, Double> tfidf = new HashMap<>();
        for (Map.Entry<String, Double> entry : tf.entrySet()) {
            String term = entry.getKey();
            double idfValue = idf.getOrDefault(term, 0.0);
            tfidf.put(term, entry.getValue() * idfValue);
        }
        // 补充 IDF 中存在但 TF 中为 0 的词条
        for (String term : idf.keySet()) {
            tfidf.putIfAbsent(term, 0.0);
        }

        return tfidf;
    }

    /**
     * 中文/英文混合分词
     * 英文按空格和标点分割，中文按字符分割后合并连续中文字符
     * 过滤停用词（常见的无意义词）
     */
    private static List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> tokens = new ArrayList<>();
        // 统一转为小写
        String normalized = text.toLowerCase();
        
        // 按非字母数字字符分割
        StringBuilder currentToken = new StringBuilder();
        for (char c : normalized.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                currentToken.append(c);
            } else {
                if (currentToken.length() > 0) {
                    String token = currentToken.toString().trim();
                    if (token.length() >= 2 && !isStopWord(token)) {
                        tokens.add(token);
                    }
                    currentToken = new StringBuilder();
                }
            }
        }
        // 处理最后一个 token
        if (currentToken.length() > 0) {
            String token = currentToken.toString().trim();
            if (token.length() >= 2 && !isStopWord(token)) {
                tokens.add(token);
            }
        }

        // 对中文文本，按单个汉字作为特征（对于中文，bi-gram效果更好）
        List<String> extendedTokens = new ArrayList<>(tokens);
        for (String token : tokens) {
            if (containsChinese(token)) {
                // 对中文生成二元分词 (bi-gram)
                for (int i = 0; i < token.length() - 1; i++) {
                    String bigram = token.substring(i, i + 2);
                    if (!isStopWord(bigram)) {
                        extendedTokens.add(bigram);
                    }
                }
            }
        }

        return extendedTokens;
    }

    /**
     * 判断字符串是否包含中文
     */
    private static boolean containsChinese(String str) {
        for (char c : str.toCharArray()) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }

    /**
     * 计算词频 (Term Frequency)
     * TF(t) = 词条t在文档中出现的次数 / 文档总词条数
     */
    private static Map<String, Double> computeTf(List<String> tokens) {
        if (tokens.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Double> tf = new HashMap<>();
        double totalTokens = tokens.size();

        for (String token : tokens) {
            tf.merge(token, 1.0 / totalTokens, Double::sum);
        }
        return tf;
    }

    /**
     * 计算逆文档频率 (Inverse Document Frequency)
     * IDF(t) = log(总文档数 / 包含词条t的文档数 + 1)
     */
    private static Map<String, Double> computeIdf(List<List<String>> tokenizedDocs) {
        Map<String, Double> idf = new HashMap<>();
        int totalDocs = tokenizedDocs.size();

        // 统计每个词条出现在多少篇文档中
        for (List<String> tokens : tokenizedDocs) {
            Set<String> uniqueTokens = new HashSet<>(tokens);
            for (String token : uniqueTokens) {
                idf.merge(token, 1.0, Double::sum);
            }
        }

        // 计算 IDF
        for (Map.Entry<String, Double> entry : idf.entrySet()) {
            double df = entry.getValue();
            // IDF = ln(总文档数 / (包含词条的文档数 + 1)) + 1
            // 使用平滑处理，避免分母为0
            entry.setValue(Math.log(totalDocs / (df + 1)) + 1);
        }

        return idf;
    }

    /**
     * 简单的停用词过滤
     */
    private static boolean isStopWord(String word) {
        Set<String> stopWords = Set.of(
                "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一",
                "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着",
                "没有", "看", "好", "自己", "这", "他", "她", "它", "们", "那", "些",
                "什么", "怎么", "如何", "可以", "进行", "使用", "基于", "通过",
                "以及", "及其", "与", "或", "等", "包括", "提供", "需要",
                "the", "a", "an", "is", "are", "was", "were", "be", "been",
                "being", "have", "has", "had", "do", "does", "did", "will",
                "would", "could", "should", "may", "might", "can", "shall",
                "to", "of", "in", "for", "on", "with", "at", "by", "from",
                "this", "that", "these", "those", "it", "its", "and", "or",
                "but", "not", "no", "if", "about", "into", "over", "after",
                "before", "between", "under", "above", "below"
        );
        return stopWords.contains(word);
    }
}
