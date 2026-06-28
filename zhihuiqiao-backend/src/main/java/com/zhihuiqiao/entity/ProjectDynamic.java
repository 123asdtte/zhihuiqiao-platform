package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目动态实体类
 * 用于记录项目公告、进展更新、团队讨论等内容
 */
@Data
@TableName("project_dynamic")
public class ProjectDynamic {

    /**
     * 动态ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 发布人ID
     */
    private Long publisherId;

    /**
     * 发布人姓名（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private String publisherName;

    /**
     * 动态内容
     */
    private String content;

    /**
     * 动态类型：announcement 公告 / progress 进展 / discussion 讨论
     */
    private String dynamicType;

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
