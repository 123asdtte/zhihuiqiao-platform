package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资源损坏赔偿记录实体类
 * 记录资源归还时的损坏情况及赔偿处理
 */
@Data
@TableName("resource_damage_record")
public class ResourceDamageRecord {

    /**
     * 记录ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联预约ID
     */
    private Long bookingId;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 上报人ID（通常为资源所有者）
     */
    private Long reporterId;

    /**
     * 损坏描述
     */
    private String damageDescription;

    /**
     * 损坏照片URL，多个以逗号分隔
     */
    private String damageImages;

    /**
     * 赔偿金额
     */
    private BigDecimal compensationAmount;

    /**
     * 状态：pending/resolved
     */
    private String status;

    /**
     * 处理备注
     */
    private String resolveRemark;

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
