package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhihuiqiao.entity.ProjectApplication;
import com.zhihuiqiao.entity.ResearchProject;
import com.zhihuiqiao.mapper.ResearchProjectMapper;
import com.zhihuiqiao.service.ProjectApplicationService;
import com.zhihuiqiao.service.ResearchProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 科研项目 Service 实现类
 */
@Service
public class ResearchProjectServiceImpl extends ServiceImpl<ResearchProjectMapper, ResearchProject> implements ResearchProjectService {

    private final ProjectApplicationService projectApplicationService;

    public ResearchProjectServiceImpl(ProjectApplicationService projectApplicationService) {
        this.projectApplicationService = projectApplicationService;
    }

    @Override
    public List<ResearchProject> searchByKeyword(String keyword) {
        return baseMapper.searchByKeyword(keyword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProject(Long id, Long currentUserId, String currentRole) {
        // 1. 查询项目是否存在
        ResearchProject project = getById(id);
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }

        // 2. 权限校验：仅发布者本人或管理员可删除
        if (!"admin".equals(currentRole) && !project.getPublisherId().equals(currentUserId)) {
            throw new RuntimeException("无权删除该项目");
        }

        // 3. 状态校验：仅允许删除 draft / pending_audit / recruiting
        String status = project.getStatus();
        if (!StringUtils.hasText(status) ||
                !("draft".equals(status) || "pending_audit".equals(status) || "recruiting".equals(status))) {
            throw new RuntimeException("当前状态的项目不允许删除");
        }

        // 4. 关联数据校验：存在非 rejected 的申请记录时禁止删除
        LambdaQueryWrapper<ProjectApplication> applicationWrapper = new LambdaQueryWrapper<>();
        applicationWrapper.eq(ProjectApplication::getProjectId, id)
                .ne(ProjectApplication::getStatus, "rejected");
        long activeApplicationCount = projectApplicationService.count(applicationWrapper);
        if (activeApplicationCount > 0) {
            throw new RuntimeException("该项目已有申请记录，无法删除");
        }

        // 5. 级联删除该项目的所有申请记录（包含 rejected）
        LambdaQueryWrapper<ProjectApplication> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ProjectApplication::getProjectId, id);
        projectApplicationService.remove(deleteWrapper);

        // 6. 删除项目
        return removeById(id);
    }

    @Override
    public List<ResearchProject> listRecruitingProjects() {
        LambdaQueryWrapper<ResearchProject> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResearchProject::getStatus, "recruiting")
                .orderByDesc(ResearchProject::getCreateTime);
        return list(wrapper);
    }
}
