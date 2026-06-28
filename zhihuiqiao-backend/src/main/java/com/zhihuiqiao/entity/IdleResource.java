package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 闲置资源实体类
 * 记录校园闲置设备、图书、场地等资源信息
 */
@Data
@TableName("idle_resource")
public class IdleResource {

    /**
     * 资源ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 资源类型：实验设备/图书资料/办公用品/电子数码/场地空间/其他
     */
    private String resourceType;

    /**
     * 交易模式：borrow-借用 transfer-转让
     */
    private String tradeMode;

    /**
     * 所有者ID
     */
    private Long ownerId;

    /**
     * 资源描述
     */
    private String description;

    /**
     * 图片URL，多个用逗号分隔
     */
    private String images;

    /**
     * 存放位置
     */
    private String location;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 租赁价格/天
     */
    private BigDecimal rentalPrice;

    /**
     * 转让期望价格，0 表示免费
     */
    private BigDecimal expectPrice;

    /**
     * 联系方式（微信/手机号/交易地点）
     */
    private String contactInfo;

    /**
     * 状态：available/rented/unavailable/transferred
     */
    private String status;

    /**
     * 借用规则
     */
    private String borrowRules;

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
}
