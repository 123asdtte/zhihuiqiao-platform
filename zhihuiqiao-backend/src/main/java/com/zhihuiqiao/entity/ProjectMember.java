package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目成员实体类
 * 记录科研项目的正式成员及其角色
 */
@Data
@TableName("project_member")
public class ProjectMember {

    /**
     * 成员ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 成员名称（非数据库字段，用于前端展示）
     */
    @TableField(exist = false)
    private String userName;

    /**
     * 角色：leader（负责人）/ member（成员）
     */
    private String role;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 状态：active（有效）/ inactive（已退出）
     */
    private String status;

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
