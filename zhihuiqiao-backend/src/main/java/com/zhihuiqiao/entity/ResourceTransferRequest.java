package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源转让意向实体类
 * 记录买家对转让资源的购买意向
 */
@Data
@TableName("resource_transfer_request")
public class ResourceTransferRequest {

    /**
     * 意向ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 状态：pending/accepted/rejected/cancelled
     */
    private String status;

    /**
     * 买家留言
     */
    private String message;

    /**
     * 买家联系方式
     */
    private String contactInfo;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 资源名称（非数据库字段）
     */
    @TableField(exist = false)
    private String resourceName;

    /**
     * 买家用户名（非数据库字段）
     */
    @TableField(exist = false)
    private String buyerName;

    /**
     * 卖家用户名（非数据库字段）
     */
    @TableField(exist = false)
    private String sellerName;

    /**
     * 买家是否已评价（非数据库字段）
     */
    @TableField(exist = false)
    private Boolean buyerReviewed;

    /**
     * 卖家是否已评价（非数据库字段）
     */
    @TableField(exist = false)
    private Boolean sellerReviewed;
}
