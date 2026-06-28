package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.Notification;

/**
 * 消息通知 Service 接口
 */
public interface NotificationService extends IService<Notification> {

    /**
     * 发送通知给指定用户
     *
     * @param userId      接收用户ID
     * @param title       通知标题
     * @param content     通知内容
     * @param type        通知类型
     * @param relatedId   关联业务ID
     * @param relatedType 关联业务类型
     * @return 通知ID
     */
    Long sendNotification(Long userId, String title, String content, String type,
                          Long relatedId, String relatedType);

    /**
     * 分页查询用户的通知列表
     *
     * @param page       分页对象
     * @param userId     用户ID
     * @param onlyUnread 是否只查询未读
     * @param type       通知类型筛选
     * @return 分页结果
     */
    IPage<Notification> listUserNotifications(Page<Notification> page, Long userId, Boolean onlyUnread, String type);

    /**
     * 获取用户未读通知数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     *
     * @param id     通知ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAsRead(Long id, Long userId);

    /**
     * 标记用户所有通知为已读
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean markAllAsRead(Long userId);
}
