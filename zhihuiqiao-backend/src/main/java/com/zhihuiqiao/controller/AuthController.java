package com.zhihuiqiao.controller;

import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.dto.LoginRequest;
import com.zhihuiqiao.dto.RegisterRequest;
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
