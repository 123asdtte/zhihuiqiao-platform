package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息通知实体类
 * 记录系统向用户发送的各类通知消息
 */
@Data
@TableName("sys_notification")
public class Notification {

    /**
     * 通知ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 通知类型：system/project/application/resource/booking/learning
     */
    private String type;

    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;

    /**
     * 关联业务ID
     */
    private Long relatedId;

    /**
     * 关联业务类型
     */
    private String relatedType;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    private Integer deleted;
}
