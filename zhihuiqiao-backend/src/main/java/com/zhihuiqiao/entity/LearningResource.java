package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习资源实体类
 */
@Data
@TableName("learning_resource")
public class LearningResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String resourceName;

    private String resourceType;

    private String subject;

    private String description;

    private String coverUrl;

    private String contentUrl;

    private String difficultyLevel;

    private Long publisherId;

    private Integer status;

    private Integer views;

    private Integer likes;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
