package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源流转记录实体类
 * 记录闲置资源的借出、归还、转让等流转历史
 */
@Data
@TableName("resource_transfer_log")
public class ResourceTransferLog {

    /**
     * 记录ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 关联预约ID
     */
    private Long bookingId;

    /**
     * 转出方ID
     */
    private Long fromUserId;

    /**
     * 转入方ID
     */
    private Long toUserId;

    /**
     * 流转类型：borrow/return/transfer
     */
    private String transferType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
