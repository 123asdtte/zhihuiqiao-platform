package com.zhihuiqiao.aspect;

import com.zhihuiqiao.annotation.OperationLogAnnotation;
import com.zhihuiqiao.entity.OperationLog;
import com.zhihuiqiao.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面
 * 拦截带有 @OperationLogAnnotation 注解的 Controller 方法，
 * 自动记录操作人、操作模块、操作描述、请求信息、执行结果等。
 */
@Aspect
@Component
public class OperationLogAspect {

    /**
     * 操作日志 Service
     */
    private final OperationLogService operationLogService;

    public OperationLogAspect(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /**
     * 定义切入点：所有标注了 @OperationLogAnnotation 的方法
     */
    @Pointcut("@annotation(com.zhihuiqiao.annotation.OperationLogAnnotation)")
    public void operationLogPointCut() {
    }

    /**
     * 环绕通知：在目标方法执行前后记录操作日志
     *
     * @param joinPoint 连接点
     * @return 目标方法返回值
     * @throws Throwable 方法执行异常
     */
    @Around("operationLogPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录方法开始时间
        long startTime = System.currentTimeMillis();

        // 获取当前请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 获取方法上的注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLogAnnotation annotation = method.getAnnotation(OperationLogAnnotation.class);

        // 构建操作日志对象
        OperationLog operationLog = new OperationLog();
        operationLog.setModule(annotation.module());
        operationLog.setOperation(annotation.operation());
        operationLog.setMethod(request != null ? request.getMethod() : null);
        operationLog.setRequestUrl(request != null ? request.getRequestURI() : null);
        operationLog.setIpAddress(getClientIp(request));
        operationLog.setUserAgent(request != null ? request.getHeader("User-Agent") : null);
        operationLog.setCreateTime(LocalDateTime.now());

        // 填充当前登录用户信息
        fillUserInfo(operationLog);

        // 是否记录请求参数
        if (annotation.saveParams() && request != null) {
            String queryString = request.getQueryString();
            operationLog.setRequestParams(queryString);
        }

        Object result;
        try {
            // 执行目标方法
            result = joinPoint.proceed();

            // 记录成功状态
            operationLog.setStatus(1);
            operationLog.setResponseCode(200);
        } catch (Throwable throwable) {
            // 记录失败状态和错误信息
            operationLog.setStatus(0);
            operationLog.setErrorMsg(throwable.getMessage());
            throw throwable;
        } finally {
            // 计算执行耗时
            long endTime = System.currentTimeMillis();
            operationLog.setExecutionTime((int) (endTime - startTime));

            // 异步保存操作日志，避免影响主业务流程
            operationLogService.saveAsync(operationLog);
        }

        return result;
    }

    /**
     * 从 Spring Security 上下文中获取当前登录用户信息
     *
     * @param operationLog 操作日志对象
     */
    private void fillUserInfo(OperationLog operationLog) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            operationLog.setUsername(userDetails.getUsername());
        }

        // userId 存储在 credentials 中，详见 JwtAuthenticationFilter
        Object credentials = authentication.getCredentials();
        if (credentials instanceof Long userId) {
            operationLog.setUserId(userId);
        }

        // 从 authorities 中解析角色类型
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            String authority = authentication.getAuthorities().iterator().next().getAuthority();
            if (authority != null && authority.startsWith("ROLE_")) {
                operationLog.setRoleType(authority.substring(5).toLowerCase());
            }
        }
    }

    /**
     * 获取客户端真实 IP 地址
     *
     * @param request HTTP 请求对象
     * @return 客户端 IP
     */
    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果存在多个 IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}
