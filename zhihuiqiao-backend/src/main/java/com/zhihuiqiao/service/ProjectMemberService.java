package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.ProjectMember;

import java.util.List;

/**
 * 项目成员 Service 接口
 */
public interface ProjectMemberService extends IService<ProjectMember> {

    /**
     * 查询项目的全部有效成员
     */
    List<ProjectMember> listActiveMembersByProjectId(Long projectId);

    /**
     * 将用户加入项目成员
     */
    boolean joinProject(Long projectId, Long userId, String role);

    /**
     * 移除项目成员
     */
    boolean removeMember(Long projectId, Long userId);

    /**
     * 更新成员角色
     */
    boolean updateMemberRole(Long projectId, Long userId, String role);

    /**
     * 查询用户加入的所有项目ID
     */
    List<Long> listProjectIdsByUserId(Long userId);
}
