package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.ProjectOutcome;
import com.zhihuiqiao.mapper.ProjectOutcomeMapper;
import com.zhihuiqiao.service.ProjectOutcomeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目成果 Service 实现类
 */
@Service
public class ProjectOutcomeServiceImpl extends ServiceImpl<ProjectOutcomeMapper, ProjectOutcome> implements ProjectOutcomeService {

    @Override
    public List<ProjectOutcome> listByProjectId(Long projectId) {
        return baseMapper.listByProjectId(projectId);
    }
}
