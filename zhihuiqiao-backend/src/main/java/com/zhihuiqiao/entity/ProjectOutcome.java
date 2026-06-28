package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目成果实体类
 * 用于项目结项时的成果归档
 */
@Data
@TableName("project_outcome")
public class ProjectOutcome {

    /**
     * 成果ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 成果名称
     */
    private String outcomeName;

    /**
     * 成果类型：paper 论文 / patent 专利 / code 代码 / video 视频 / report 报告 / other 其他
     */
    private String outcomeType;

    /**
     * 成果描述
     */
    private String description;

    /**
     * 成果附件URL
     */
    private String fileUrl;

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
