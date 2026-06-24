package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源预约实体类
 * 记录用户对闲置资源的预约申请及审批状态
 */
@Data
@TableName("resource_booking")
public class ResourceBooking {

    /**
     * 预约ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 借用人ID
     */
    private Long borrowerId;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 用途说明
     */
    private String purpose;

    /**
     * 状态：pending/approved/rejected/ongoing/returned/cancelled
     */
    private String status;

    /**
     * 实际归还时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime returnTime;

    /**
     * 回复消息
     */
    private String replyMessage;

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

    /**
     * 资源名称（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private String resourceName;
}
