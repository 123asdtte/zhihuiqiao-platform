package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.ProjectMember;
import com.zhihuiqiao.mapper.ProjectMemberMapper;
import com.zhihuiqiao.service.ProjectMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目成员 Service 实现类
 */
@Service
public class ProjectMemberServiceImpl extends ServiceImpl<ProjectMemberMapper, ProjectMember> implements ProjectMemberService {

    @Override
    public List<ProjectMember> listActiveMembersByProjectId(Long projectId) {
        return baseMapper.listActiveMembersByProjectId(projectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinProject(Long projectId, Long userId, String role) {
        LambdaQueryWrapper<ProjectMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId);
        ProjectMember exist = getOne(wrapper);
        if (exist != null) {
            // 已存在则恢复为有效状态
            exist.setDeleted(0);
            exist.setStatus("active");
            exist.setRole(role);
            exist.setJoinTime(LocalDateTime.now());
            return updateById(exist);
        }
        ProjectMember member = new ProjectMember();
        member.setProjectId(projectId);
        member.setUserId(userId);
        member.setRole(role);
        member.setStatus("active");
        member.setJoinTime(LocalDateTime.now());
        return save(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeMember(Long projectId, Long userId) {
        LambdaUpdateWrapper<ProjectMember> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId)
                .set(ProjectMember::getStatus, "inactive")
                .set(ProjectMember::getDeleted, 1);
        return update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMemberRole(Long projectId, Long userId, String role) {
        LambdaUpdateWrapper<ProjectMember> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ProjectMember::getProjectId, projectId)
                .eq(ProjectMember::getUserId, userId)
                .set(ProjectMember::getRole, role);
        return update(wrapper);
    }

    @Override
    public List<Long> listProjectIdsByUserId(Long userId) {
        return baseMapper.listProjectIdsByUserId(userId);
    }
}
