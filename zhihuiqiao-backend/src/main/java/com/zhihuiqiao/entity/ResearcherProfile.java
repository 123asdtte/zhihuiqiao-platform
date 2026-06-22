package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 科研画像实体类
 * 记录教师的科研方向、技能、成果等信息，用于项目智能匹配
 */
@Data
@TableName("researcher_profile")
public class ResearcherProfile {

    /**
     * 画像ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 研究方向，多个方向用逗号分隔
     */
    private String researchDirections;

    /**
     * 技能标签，多个标签用逗号分隔
     */
    private String skills;

    /**
     * 论文/专利/成果
     */
    private String publications;

    /**
     * 项目经历
     */
    private String projectExperience;

    /**
     * 科研兴趣
     */
    private String researchInterests;

    /**
     * 可投入时间：全职/兼职/课余
     */
    private String availability;

    /**
     * 合作意向
     */
    private String cooperationIntention;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
