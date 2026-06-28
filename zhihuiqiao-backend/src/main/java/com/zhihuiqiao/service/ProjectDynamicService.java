package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.ProjectDynamic;

import java.util.List;

/**
 * 项目动态 Service 接口
 */
public interface ProjectDynamicService extends IService<ProjectDynamic> {

    /**
     * 查询项目的全部动态
     */
    List<ProjectDynamic> listByProjectId(Long projectId);
}
