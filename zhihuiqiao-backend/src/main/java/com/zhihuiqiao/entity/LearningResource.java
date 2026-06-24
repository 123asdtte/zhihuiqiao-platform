package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习资源实体类
 * 记录教学辅助模块中的课程、视频、论文、图书、工具等学习资源信息
 */
@Data
@TableName("learning_resource")
public class LearningResource {

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
     * 资源类型：course/video/paper/book/tool
     */
    private String resourceType;

    /**
     * 学科领域
     */
    private String subject;

    /**
     * 资源描述
     */
    private String description;

    /**
     * 封面URL
     */
    private String coverUrl;

    /**
     * 内容链接
     */
    private String contentUrl;

    /**
     * 难度等级：初级/中级/高级
     */
    private String difficultyLevel;

    /**
     * 发布人ID
     */
    private Long publisherId;

    /**
     * 状态：0-下架 1-上架
     */
    private Integer status;

    /**
     * 浏览量
     */
    private Integer views;

    /**
     * 点赞数
     */
    private Integer likes;

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
