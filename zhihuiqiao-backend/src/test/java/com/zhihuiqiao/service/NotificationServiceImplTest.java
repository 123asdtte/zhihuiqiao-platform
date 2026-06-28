package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhihuiqiao.entity.Notification;
import com.zhihuiqiao.mapper.NotificationMapper;
import com.zhihuiqiao.service.impl.NotificationServiceImpl;
import com.zhihuiqiao.websocket.NotificationWebSocketHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 消息通知服务单元测试
 * 验证通知发送、已读标记、通知设置过滤等核心逻辑
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationWebSocketHandler webSocketHandler;

    @Mock
    private NotificationSettingService notificationSettingService;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() throws Exception {
        notificationService = new NotificationServiceImpl(webSocketHandler, notificationSettingService);
        // 通过反射注入 baseMapper，因为 ServiceImpl 依赖它执行数据库操作
        Field baseMapperField = notificationService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(notificationService, notificationMapper);
    }

    /**
     * 测试：用户开启通知时，通知正常入库并推送
     */
    @Test
    void sendNotification_shouldInsertAndPush_whenEnabled() {
        when(notificationSettingService.isEnabled(1L, "project")).thenReturn(true);
        when(notificationMapper.insert(any(Notification.class))).thenAnswer(invocation -> {
            Notification n = invocation.getArgument(0);
            n.setId(100L);
            return 1;
        });

        Long id = notificationService.sendNotification(1L, "测试标题", "测试内容", "project", 10L, "project");

        assertNotNull(id);
        assertEquals(100L, id);
        verify(notificationMapper, times(1)).insert(any(Notification.class));
        verify(webSocketHandler, times(1)).sendNotificationToUser(eq(1L), any());
    }

    /**
     * 测试：用户关闭通知时，不发送通知
     */
    @Test
    void sendNotification_shouldSkip_whenDisabled() {
        when(notificationSettingService.isEnabled(1L, "project")).thenReturn(false);

        Long id = notificationService.sendNotification(1L, "测试标题", "测试内容", "project", 10L, "project");

        assertNull(id);
        verify(notificationMapper, never()).insert(any(Notification.class));
        verify(webSocketHandler, never()).sendNotificationToUser(any(), any());
    }

    /**
     * 测试：标记通知为已读
     */
    @Test
    void markAsRead_shouldReturnTrue_whenUpdateSuccess() {
        when(notificationMapper.update(any(), any())).thenReturn(1);

        boolean result = notificationService.markAsRead(1L, 1L);

        assertTrue(result);
    }

    /**
     * 测试：查询用户通知列表时传入类型参数
     */
    @Test
    void listUserNotifications_shouldCallMapper_withTypeFilter() {
        Page<Notification> page = new Page<>(1, 10);

        notificationService.listUserNotifications(page, 1L, false, "application");

        verify(notificationMapper, times(1)).selectPage(eq(page), any());
    }
}
