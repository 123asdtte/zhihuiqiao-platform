package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户违约记录实体类
 * 记录用户因资源超期归还、损坏赔偿等产生的违约行为及信用扣分明细
 */
@Data
@TableName("user_penalty_record")
public class UserPenaltyRecord {

    /**
     * 记录ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 违约用户ID
     */
    private Long userId;

    /**
     * 违约类型：overdue 超期 / damage 损坏赔偿
     */
    private String penaltyType;

    /**
     * 关联预约ID
     */
    private Long relatedBookingId;

    /**
     * 违约描述
     */
    private String description;

    /**
     * 扣除信用分
     */
    private Integer penaltyScore;

    /**
     * 状态：active/resolved
     */
    private String status;

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
