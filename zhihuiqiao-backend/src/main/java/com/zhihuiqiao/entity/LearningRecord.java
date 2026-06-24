package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习记录实体类
 * 记录用户与学习资源之间的学习进度、收藏、完成状态等
 */
@Data
@TableName("learning_record")
public class LearningRecord {

    /**
     * 记录ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 学习资源ID
     */
    private Long resourceId;

    /**
     * 学习进度：0-100
     */
    private Integer progress;

    /**
     * 状态：learning/completed/favorite
     */
    private String status;

    /**
     * 上次学习位置（秒/页码）
     */
    private Integer lastPosition;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    private Integer deleted;

    /**
     * 资源名称（非数据库字段，仅用于前端展示）
     */
    @TableField(exist = false)
    private String resourceName;

    /**
     * 资源封面（非数据库字段，仅用于前端展示）
     */
    @TableField(exist = false)
    private String coverUrl;

    /**
     * 资源类型（非数据库字段，仅用于前端展示）
     */
    @TableField(exist = false)
    private String resourceType;
}
