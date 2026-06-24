package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.Notification;
import com.zhihuiqiao.mapper.NotificationMapper;
import com.zhihuiqiao.service.NotificationService;
import com.zhihuiqiao.vo.NotificationMessageVO;
import com.zhihuiqiao.websocket.NotificationWebSocketHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 消息通知 Service 实现类
 * 支持数据库存储 + WebSocket 实时推送
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    /**
     * WebSocket 通知处理器，用于实时推送
     */
    private final NotificationWebSocketHandler webSocketHandler;

    public NotificationServiceImpl(NotificationWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public Long sendNotification(Long userId, String title, String content, String type,
                                 Long relatedId, String relatedType) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(0);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setDeleted(0);
        baseMapper.insert(notification);

        // 通过 WebSocket 向用户实时推送通知
        pushNotificationToUser(notification);

        return notification.getId();
    }

    /**
     * 将通知转换为 WebSocket 消息并推送给指定用户
     *
     * @param notification 通知实体
     */
    private void pushNotificationToUser(Notification notification) {
        if (webSocketHandler == null) {
            return;
        }
        NotificationMessageVO messageVO = new NotificationMessageVO();
        BeanUtils.copyProperties(notification, messageVO);
        webSocketHandler.sendNotificationToUser(notification.getUserId(), messageVO);
    }

    @Override
    public IPage<Notification> listUserNotifications(Page<Notification> page, Long userId, Boolean onlyUnread) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getDeleted, 0)
                .orderByDesc(Notification::getCreateTime);
        if (Boolean.TRUE.equals(onlyUnread)) {
            wrapper.eq(Notification::getIsRead, 0);
        }
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .eq(Notification::getDeleted, 0);
        return baseMapper.selectCount(wrapper);
    }

    @Override
    public boolean markAsRead(Long id, Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getId, id)
                .eq(Notification::getUserId, userId)
                .set(Notification::getIsRead, 1);
        return baseMapper.update(null, wrapper) > 0;
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1);
        return baseMapper.update(null, wrapper) > 0;
    }
}
