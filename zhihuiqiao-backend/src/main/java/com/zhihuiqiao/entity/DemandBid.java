package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 需求承接记录实体类（揭榜）
 */
@Data
@TableName("demand_bid")
public class DemandBid {

    /**
     * 承接ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联企业需求ID
     */
    private Long demandId;

    /**
     * 承接人ID
     */
    private Long bidderId;

    /**
     * 承接人类型：student/teacher
     */
    private String bidderType;

    /**
     * 承接方案
     */
    private String bidContent;

    /**
     * 预计周期
     */
    private String expectedDuration;

    /**
     * 预计报价
     */
    private String expectedBudget;

    /**
     * 状态：pending/approved/rejected/cancelled
     */
    private String status;

    /**
     * 企业备注
     */
    private String enterpriseRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    private Integer deleted;

    /**
     * 承接人姓名，非数据库字段，用于前端展示
     */
    @TableField(exist = false)
    private String bidderName;

    /**
     * 关联需求标题，非数据库字段
     */
    @TableField(exist = false)
    private String demandTitle;
}
