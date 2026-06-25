package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目申请实体类
 * 记录学生申请加入科研项目的信息及审核状态
 */
@Data
@TableName("project_application")
public class ProjectApplication {

    /**
     * 申请ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private String projectName;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人名称（非数据库字段，管理员查看全部申请时用于前端展示）
     */
    @TableField(exist = false)
    private String applicantName;

    /**
     * 申请理由
     */
    private String applyReason;

    /**
     * 状态：pending/approved/rejected
     */
    private String status;

    /**
     * 回复消息
     */
    private String replyMessage;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

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
