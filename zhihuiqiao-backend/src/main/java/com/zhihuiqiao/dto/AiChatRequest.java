package com.zhihuiqiao.dto;

import lombok.Data;

/**
 * AI 通用对话请求 DTO
 */
@Data
public class AiChatRequest {

    /**
     * 用户输入的问题
     */
    private String question;
}
