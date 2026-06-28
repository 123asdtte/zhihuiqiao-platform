package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目任务实体类
 * 用于项目内部的任务拆解与跟踪
 */
@Data
@TableName("project_task")
public class ProjectTask {

    /**
     * 任务ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 负责人ID
     */
    private Long assigneeId;

    /**
     * 负责人姓名（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private String assigneeName;

    /**
     * 截止日期
     */
    private LocalDate deadline;

    /**
     * 状态：pending 待处理 / in_progress 进行中 / completed 已完成
     */
    private String status;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    private Integer deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
