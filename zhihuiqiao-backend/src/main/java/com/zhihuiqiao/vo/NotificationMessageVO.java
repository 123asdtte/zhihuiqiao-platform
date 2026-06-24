package com.zhihuiqiao.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * WebSocket 实时通知消息视图对象
 * 用于服务端向客户端推送的通知内容
 */
@Data
public class NotificationMessageVO {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型
     */
    private String type;

    /**
     * 关联业务ID
     */
    private Long relatedId;

    /**
     * 关联业务类型
     */
    private String relatedType;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
