package com.zhihuiqiao.config;

import com.zhihuiqiao.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手认证拦截器
 * 在 WebSocket 握手阶段校验 JWT Token，并将用户信息存入 session 属性
 */
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    /**
     * JWT 工具类
     */
    private final JwtUtil jwtUtil;

    public WebSocketAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 握手前校验 Token
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 仅处理 Servlet 请求
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }

        HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
        String token = extractToken(httpServletRequest);

        // 没有 Token 或 Token 无效时拒绝握手
        if (!StringUtils.hasText(token) || !jwtUtil.validateToken(token)) {
            return false;
        }

        // 将用户信息放入 WebSocket Session 属性，供后续使用
        attributes.put("userId", jwtUtil.getUserIdFromToken(token));
        attributes.put("username", jwtUtil.getUsernameFromToken(token));
        attributes.put("roleType", jwtUtil.getRoleTypeFromToken(token));
        return true;
    }

    /**
     * 握手后回调，无需额外处理
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }

    /**
     * 从请求中提取 Token
     * 优先从 query 参数获取，其次从请求头获取
     */
    private String extractToken(HttpServletRequest request) {
        // 1. 从 query 参数获取，例如 /ws/notification?token=xxx
        String token = request.getParameter("token");
        if (StringUtils.hasText(token)) {
            return token;
        }

        // 2. 从请求头获取
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
