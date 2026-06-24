package com.zhihuiqiao.controller;

import com.zhihuiqiao.annotation.OperationLogAnnotation;
import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.dto.LoginRequest;
import com.zhihuiqiao.dto.RegisterRequest;
import com.zhihuiqiao.entity.SysUser;
import com.zhihuiqiao.security.JwtUtil;
import com.zhihuiqiao.service.SysUserService;
import com.zhihuiqiao.vo.LoginVO;
import com.zhihuiqiao.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SysUserService sysUserService;
    private final JwtUtil jwtUtil;

    public AuthController(SysUserService sysUserService, JwtUtil jwtUtil) {
        this.sysUserService = sysUserService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(sysUserService.login(request));
    }

    @OperationLogAnnotation(module = "认证管理", operation = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        sysUserService.register(request);
        return Result.success();
    }

    @GetMapping("/info")
    public Result<UserVO> getCurrentUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getCredentials();
        return Result.success(sysUserService.getCurrentUserInfo(userId));
    }

    /**
     * 更新当前登录用户信息
     */
    @OperationLogAnnotation(module = "个人中心", operation = "更新个人资料")
    @PutMapping("/profile")
    public Result<Boolean> updateCurrentUserInfo(@RequestBody SysUser user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getCredentials();
        user.setId(userId);
        // 不允许通过个人信息接口修改关键字段
        user.setPassword(null);
        user.setUsername(null);
        user.setRoleType(null);
        return Result.success(sysUserService.updateById(user));
    }

    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        String username = jwtUtil.getUsernameFromToken(token);
        String roleType = jwtUtil.getRoleTypeFromToken(token);

        String newToken = jwtUtil.generateToken(userId, username, roleType);
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(newToken);
        loginVO.setUserInfo(sysUserService.getCurrentUserInfo(userId));
        return Result.success(loginVO);
    }
}
