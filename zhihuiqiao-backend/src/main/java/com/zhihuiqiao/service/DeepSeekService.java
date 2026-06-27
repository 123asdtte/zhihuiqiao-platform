package com.zhihuiqiao.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhihuiqiao.config.DeepSeekConfig;
import com.zhihuiqiao.dto.DeepSeekChatRequest;
import com.zhihuiqiao.dto.DeepSeekChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek AI 服务
 * 封装 DeepSeek API 调用，提供科研项目推荐能力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekService {

    private final DeepSeekConfig deepSeekConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调用 DeepSeek Chat Completions 接口
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return AI 返回的文本内容
     */
    public String chat(String systemPrompt, String userPrompt) {
        String url = deepSeekConfig.getBaseUrl() + "/v1/chat/completions";

        DeepSeekChatRequest request = new DeepSeekChatRequest();
        request.setModel(deepSeekConfig.getModel());
        request.setMaxTokens(deepSeekConfig.getMaxTokens());
        request.setTemperature(deepSeekConfig.getTemperature());
        request.setResponseFormat(Map.of("type", "json_object"));

        DeepSeekChatRequest.Message systemMessage = new DeepSeekChatRequest.Message("system", systemPrompt);
        DeepSeekChatRequest.Message userMessage = new DeepSeekChatRequest.Message("user", userPrompt);
        request.setMessages(List.of(systemMessage, userMessage));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(deepSeekConfig.getApiKey());

        HttpEntity<DeepSeekChatRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<DeepSeekChatResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    DeepSeekChatResponse.class
            );

            if (response.getBody() == null || response.getBody().getChoices() == null || response.getBody().getChoices().isEmpty()) {
                return null;
            }

            return response.getBody().getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            log.error("DeepSeek API 调用失败", e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage());
        }
    }
}
