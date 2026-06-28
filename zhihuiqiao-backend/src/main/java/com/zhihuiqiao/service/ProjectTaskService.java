package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.ProjectTask;

import java.util.List;

/**
 * 项目任务 Service 接口
 */
public interface ProjectTaskService extends IService<ProjectTask> {

    /**
     * 查询项目的全部任务
     */
    List<ProjectTask> listByProjectId(Long projectId);
}
