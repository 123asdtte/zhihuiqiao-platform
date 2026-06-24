package com.zhihuiqiao.config;

import com.zhihuiqiao.security.JwtUtil;
import com.zhihuiqiao.websocket.NotificationWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket 配置类
 * 注册通知推送的 WebSocket 端点
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    /**
     * 通知 WebSocket 处理器
     */
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    /**
     * JWT 工具类，用于 WebSocket 握手时校验 Token
     */
    private final JwtUtil jwtUtil;

    public WebSocketConfig(NotificationWebSocketHandler notificationWebSocketHandler, JwtUtil jwtUtil) {
        this.notificationWebSocketHandler = notificationWebSocketHandler;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationWebSocketHandler, "/ws/notification")
                // 允许前端跨域访问
                .setAllowedOrigins("*")
                // 添加自定义握手拦截器，校验 WebSocket 连接中的 Token
                .addInterceptors(new WebSocketAuthInterceptor(jwtUtil));
    }
}
