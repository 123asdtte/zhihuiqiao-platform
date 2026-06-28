package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.ProjectTask;
import com.zhihuiqiao.mapper.ProjectTaskMapper;
import com.zhihuiqiao.service.ProjectTaskService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目任务 Service 实现类
 */
@Service
public class ProjectTaskServiceImpl extends ServiceImpl<ProjectTaskMapper, ProjectTask> implements ProjectTaskService {

    @Override
    public List<ProjectTask> listByProjectId(Long projectId) {
        return baseMapper.listByProjectId(projectId);
    }
}
