package com.zhihuiqiao.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek Chat Completions 请求 DTO
 * 兼容 OpenAI 格式
 */
@Data
public class DeepSeekChatRequest {

    /**
     * 模型名称
     */
    private String model;

    /**
     * 消息列表
     */
    private List<Message> messages;

    /**
     * 最大返回 token 数
     */
    private Integer maxTokens;

    /**
     * 温度参数
     */
    private Double temperature;

    /**
     * DeepSeek 支持 JSON 格式返回
     */
    private Map<String, String> responseFormat;

    /**
     * 消息对象
     */
    @Data
    public static class Message {

        /**
         * 角色：system/user/assistant
         */
        private String role;

        /**
         * 内容
         */
        private String content;

        public Message() {
        }

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
