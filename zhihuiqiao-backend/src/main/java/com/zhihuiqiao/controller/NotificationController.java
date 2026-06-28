package com.zhihuiqiao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.entity.Notification;
import com.zhihuiqiao.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 消息通知模块 Controller
 * 提供通知查询、标记已读、未读数量统计等接口
 */
@Tag(name = "消息通知", description = "系统通知、业务通知相关接口")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getCredentials();
    }

    @Operation(summary = "分页查询当前用户的通知列表")
    @GetMapping
    public Result<IPage<Notification>> listNotifications(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Boolean onlyUnread,
            @RequestParam(required = false) String type) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        return Result.success(notificationService.listUserNotifications(page, getCurrentUserId(), onlyUnread, type));
    }

    @Operation(summary = "获取当前用户未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount() {
        return Result.success(notificationService.getUnreadCount(getCurrentUserId()));
    }

    @Operation(summary = "标记指定通知为已读")
    @PutMapping("/{id}/read")
    public Result<Boolean> markAsRead(@PathVariable Long id) {
        return Result.success(notificationService.markAsRead(id, getCurrentUserId()));
    }

    @Operation(summary = "标记当前用户所有通知为已读")
    @PutMapping("/read-all")
    public Result<Boolean> markAllAsRead() {
        return Result.success(notificationService.markAllAsRead(getCurrentUserId()));
    }
}
