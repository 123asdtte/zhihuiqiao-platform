package com.zhihuiqiao.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-window:1800000}")
    private Long refreshWindow;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String username, String roleType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("roleType", roleType)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            return false;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            // 密钥变更或 Token 签名不匹配，视为无效 Token
            return false;
        } catch (io.jsonwebtoken.security.WeakKeyException e) {
            // 密钥长度不足导致校验失败，视为无效 Token
            return false;
        } catch (JwtException e) {
            // 捕获其他所有 JWT 相关异常
            return false;
        }
    }

    /**
     * 判断 Token 是否已过期
     */
    public boolean isTokenExpired(String token) {
        try {
            parseToken(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断 Token 是否在可刷新窗口内
     * 
     * 安全策略：
     * 1. 未过期的 Token：仅在过期前 refreshWindow 毫秒内允许刷新
     * 2. 已过期的 Token：仅在过期后 1 小时内允许刷新（防止无限续期）
     * 3. 超过 1 小时的过期 Token：必须重新登录
     *
     * @param token JWT Token
     * @return true 如果 Token 可以刷新
     */
    public boolean isTokenRefreshable(String token) {
        // 已过期 Token 的最大刷新窗口：1 小时
        final long MAX_EXPIRED_REFRESH_WINDOW = 3600000;
        
        try {
            Claims claims = parseToken(token);
            Date expirationDate = claims.getExpiration();
            Date now = new Date();
            long nowTime = now.getTime();
            long expiryTime = expirationDate.getTime();
            
            // 未过期：检查是否在刷新窗口内（过期前 refreshWindow 毫秒内）
            if (nowTime < expiryTime) {
                long refreshWindowStart = expiryTime - refreshWindow;
                return nowTime >= refreshWindowStart;
            }
            
            // 已过期：检查是否在最大刷新窗口内（过期后 1 小时内）
            long expiredDuration = nowTime - expiryTime;
            return expiredDuration <= MAX_EXPIRED_REFRESH_WINDOW;
            
        } catch (ExpiredJwtException e) {
            // parseToken 抛出过期异常，说明 Token 已过期，返回 false 需重新登录
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取 Token 剩余有效时间（毫秒）
     * 已过期返回负数
     */
    public long getTokenRemainingTime(String token) {
        try {
            Claims claims = parseToken(token);
            Date expirationDate = claims.getExpiration();
            return expirationDate.getTime() - System.currentTimeMillis();
        } catch (ExpiredJwtException e) {
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.getSubject());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    public String getRoleTypeFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("roleType", String.class);
    }
}
