package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 交付里程碑实体类
 */
@Data
@TableName("delivery_milestone")
public class DeliveryMilestone {

    /**
     * 里程碑ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联合同ID
     */
    private Long contractId;

    /**
     * 关联需求ID
     */
    private Long demandId;

    /**
     * 里程碑名称
     */
    private String milestoneName;

    /**
     * 交付内容描述
     */
    private String description;

    /**
     * 计划截止日期
     */
    private LocalDate dueDate;

    /**
     * 交付物附件URL
     */
    private String deliverableUrl;

    /**
     * 状态：pending/submitted/approved/rejected
     */
    private String status;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 验收时间
     */
    private LocalDateTime approveTime;

    /**
     * 企业验收备注
     */
    private String enterpriseRemark;

    /**
     * 排序
     */
    private Integer sortOrder;

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
     * 合同标题，非数据库字段
     */
    @TableField(exist = false)
    private String contractTitle;
}
