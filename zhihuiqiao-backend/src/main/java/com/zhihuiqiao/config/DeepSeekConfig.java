package com.zhihuiqiao.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DeepSeek AI 配置类
 * 用于读取 application.yml 中的 deepseek 配置项
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "deepseek")
public class DeepSeekConfig {

    /**
     * DeepSeek API 密钥
     */
    private String apiKey;

    /**
     * DeepSeek API 基础地址，默认兼容 OpenAI 格式
     */
    private String baseUrl = "https://api.deepseek.com";

    /**
     * 使用的模型名称
     */
    private String model = "deepseek-chat";

    /**
     * 推荐返回的最大 token 数
     */
    private Integer maxTokens = 2000;

    /**
     * 温度参数，控制推荐结果的创造性
     */
    private Double temperature = 0.7;
}
