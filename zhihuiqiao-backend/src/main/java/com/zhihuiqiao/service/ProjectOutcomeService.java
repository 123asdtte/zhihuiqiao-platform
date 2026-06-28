package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.ProjectOutcome;

import java.util.List;

/**
 * 项目成果 Service 接口
 */
public interface ProjectOutcomeService extends IService<ProjectOutcome> {

    /**
     * 查询项目的全部成果
     */
    List<ProjectOutcome> listByProjectId(Long projectId);
}
