package com.zhihuiqiao.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知 WebSocket 处理器
 * 维护用户 ID 与 WebSocket Session 的映射关系，支持向指定用户实时推送通知
 */
@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    /**
     * 用户会话映射表
     * key: 用户ID，value: WebSocketSession
     */
    private static final Map<Long, WebSocketSession> USER_SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * JSON 序列化工具
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 连接建立成功后，将用户 session 加入映射表
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            USER_SESSION_MAP.put(userId, session);
            log.info("WebSocket 连接已建立，用户ID: {}", userId);
        }
    }

    /**
     * 连接关闭后，从映射表中移除用户 session
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            USER_SESSION_MAP.remove(userId);
            log.info("WebSocket 连接已关闭，用户ID: {}", userId);
        }
    }

    /**
     * 处理客户端发送的文本消息（本系统主要做服务端推送，客户端消息可忽略或记录）
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("收到客户端消息: {}", message.getPayload());
    }

    /**
     * 向指定用户发送通知消息
     *
     * @param userId  接收用户ID
     * @param message 通知消息对象
     */
    public void sendNotificationToUser(Long userId, Object message) {
        WebSocketSession session = USER_SESSION_MAP.get(userId);
        if (session == null || !session.isOpen()) {
            log.debug("用户 {} 当前不在线，无法实时推送", userId);
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(payload));
            log.info("已向用户 {} 实时推送通知", userId);
        } catch (IOException e) {
            log.error("向用户 {} 推送通知失败", userId, e);
        }
    }

    /**
     * 向所有在线用户广播通知
     *
     * @param message 通知消息对象
     */
    public void broadcastNotification(Object message) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (IOException e) {
            log.error("通知序列化失败", e);
            return;
        }

        USER_SESSION_MAP.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(payload));
                } catch (IOException e) {
                    log.error("广播通知失败", e);
                }
            }
        });
    }

    /**
     * 从 WebSocket Session 属性中获取用户ID
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }
}
