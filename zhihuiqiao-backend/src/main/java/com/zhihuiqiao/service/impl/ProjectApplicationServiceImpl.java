package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.ProjectApplication;
import com.zhihuiqiao.mapper.ProjectApplicationMapper;
import com.zhihuiqiao.service.ProjectApplicationService;
import org.springframework.stereotype.Service;

/**
 * 项目申请 Service 实现类
 */
@Service
public class ProjectApplicationServiceImpl extends ServiceImpl<ProjectApplicationMapper, ProjectApplication> implements ProjectApplicationService {
}
