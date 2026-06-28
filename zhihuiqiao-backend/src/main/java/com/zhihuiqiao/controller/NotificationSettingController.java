package com.zhihuiqiao.controller;

import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.entity.NotificationSetting;
import com.zhihuiqiao.service.NotificationSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知设置 Controller
 * 提供用户通知偏好查询与修改接口
 */
@Tag(name = "通知设置", description = "用户通知偏好配置")
@RestController
@RequestMapping("/api/notifications/settings")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getCredentials();
    }

    @Operation(summary = "获取当前用户的通知设置")
    @GetMapping
    public Result<List<NotificationSetting>> getSettings() {
        return Result.success(notificationSettingService.getSettings(getCurrentUserId()));
    }

    @Operation(summary = "更新当前用户的通知设置")
    @PutMapping
    public Result<Boolean> updateSettings(@RequestBody List<NotificationSetting> settings) {
        return Result.success(notificationSettingService.updateSettings(getCurrentUserId(), settings));
    }
}
