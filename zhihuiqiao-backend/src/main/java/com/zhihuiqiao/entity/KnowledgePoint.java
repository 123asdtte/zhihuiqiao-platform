package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识点实体类
 * 用于构建课程知识图谱，支持学习路径规划算法（拓扑排序）
 */
@Data
@TableName("knowledge_point")
public class KnowledgePoint {

    /**
     * 知识点ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属课程名称
     */
    private String courseName;

    /**
     * 知识点名称
     */
    private String pointName;

    /**
     * 知识点描述
     */
    private String description;

    /**
     * 知识点内容
     */
    private String content;

    /**
     * 难度等级：1-5
     */
    private Integer difficulty;

    /**
     * 前置知识点ID列表，多个用逗号分隔，如"1,3,5"
     */
    private String prerequisiteIds;

    /**
     * 预计学习时长（分钟）
     */
    private Integer estimatedMinutes;

    /**
     * 状态：0-下架 1-上架
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
