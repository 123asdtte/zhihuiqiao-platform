package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 通知设置实体类
 * 记录用户对各类通知的接收偏好
 */
@Data
@TableName("sys_notification_setting")
public class NotificationSetting {

    /**
     * 设置ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 通知类型：system/project/application/resource/booking/learning
     */
    private String type;

    /**
     * 是否启用：0-关闭 1-开启
     */
    private Integer enabled;
}
