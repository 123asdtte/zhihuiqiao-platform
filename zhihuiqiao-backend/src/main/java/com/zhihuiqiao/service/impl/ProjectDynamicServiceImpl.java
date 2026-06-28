package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.ProjectDynamic;
import com.zhihuiqiao.mapper.ProjectDynamicMapper;
import com.zhihuiqiao.service.ProjectDynamicService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目动态 Service 实现类
 */
@Service
public class ProjectDynamicServiceImpl extends ServiceImpl<ProjectDynamicMapper, ProjectDynamic> implements ProjectDynamicService {

    @Override
    public List<ProjectDynamic> listByProjectId(Long projectId) {
        return baseMapper.listByProjectId(projectId);
    }
}
