package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业需求实体类
 * 记录企业发布的技术需求、合作意向等信息
 */
@Data
@TableName("enterprise_demand")
public class EnterpriseDemand {

    /**
     * 需求ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 企业用户ID
     */
    private Long enterpriseId;

    /**
     * 需求标题
     */
    private String demandTitle;

    /**
     * 需求类型：技术攻关/成果转化/人才招聘/联合研发
     */
    private String demandType;

    /**
     * 行业领域
     */
    private String industryField;

    /**
     * 需求描述
     */
    private String demandDescription;

    /**
     * 技术要求
     */
    private String technicalRequirements;

    /**
     * 预算范围
     */
    private String budgetRange;

    /**
     * 合作方式
     */
    private String cooperationMode;

    /**
     * 联系方式
     */
    private String contactInfo;

    /**
     * 状态：open/closed/completed
     */
    private String status;

    /**
     * 浏览量
     */
    private Integer views;

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
     * 发布企业名称，非数据库字段，用于前端展示
     */
    @TableField(exist = false)
    private String publisherName;
}
