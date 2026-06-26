package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 科研项目实体类
 * 记录科研项目的基本信息、发布人、状态、成员要求等
 */
@Data
@TableName("research_project")
public class ResearchProject {

    /**
     * 项目ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目编号
     */
    private String projectCode;

    /**
     * 发布人ID
     */
    private Long publisherId;

    /**
     * 发布人类型：teacher/enterprise
     */
    private String publisherType;

    /**
     * 发布人姓名，冗余字段用于列表/详情展示
     */
    private String publisherName;

    /**
     * 项目类型：基础研究/应用研究/技术开发/创新创业
     */
    private String projectType;

    /**
     * 研究领域
     */
    private String researchFields;

    /**
     * 项目简介
     */
    private String projectDescription;

    /**
     * 成员要求
     */
    private String requirements;

    /**
     * 预期成果
     */
    private String expectedOutcomes;

    /**
     * 状态：recruiting/ongoing/completed/closed
     */
    private String status;

    /**
     * 最大成员数
     */
    private Integer maxMembers;

    /**
     * 当前成员数
     */
    private Integer currentMembers;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 浏览量
     */
    private Integer views;

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
