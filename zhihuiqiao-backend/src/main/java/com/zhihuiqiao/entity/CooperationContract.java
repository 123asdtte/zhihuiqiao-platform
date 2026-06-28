package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 合作合同实体类
 */
@Data
@TableName("cooperation_contract")
public class CooperationContract {

    /**
     * 合同ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联需求ID
     */
    private Long demandId;

    /**
     * 关联承接方案ID
     */
    private Long bidId;

    /**
     * 企业方用户ID（甲方）
     */
    private Long partyAId;

    /**
     * 承接方用户ID（乙方）
     */
    private Long partyBId;

    /**
     * 合同标题
     */
    private String contractTitle;

    /**
     * 合同内容
     */
    private String contractContent;

    /**
     * 合同金额
     */
    private BigDecimal amount;

    /**
     * 状态：unsigned/signed/completed/terminated
     */
    private String status;

    /**
     * 签订时间
     */
    private LocalDateTime signTime;

    /**
     * 合同附件URL
     */
    private String fileUrl;

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
     * 企业名称，非数据库字段
     */
    @TableField(exist = false)
    private String partyAName;

    /**
     * 承接方名称，非数据库字段
     */
    @TableField(exist = false)
    private String partyBName;
}
