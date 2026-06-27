package com.zhihuiqiao.dto;

import lombok.Data;

import java.util.List;

/**
 * DeepSeek Chat Completions 响应 DTO
 * 兼容 OpenAI 格式
 */
@Data
public class DeepSeekChatResponse {

    /**
     * 响应 ID
     */
    private String id;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 创建时间戳
     */
    private Long created;

    /**
     * 选择列表
     */
    private List<Choice> choices;

    /**
     * 选择对象
     */
    @Data
    public static class Choice {

        /**
         * 索引
         */
        private Integer index;

        /**
         * 消息
         */
        private Message message;

        /**
         * 结束原因
         */
        private String finishReason;
    }

    /**
     * 消息对象
     */
    @Data
    public static class Message {

        /**
         * 角色
         */
        private String role;

        /**
         * 内容
         */
        private String content;
    }
}
