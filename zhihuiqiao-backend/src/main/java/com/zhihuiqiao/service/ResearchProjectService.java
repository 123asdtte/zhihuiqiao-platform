package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.ResearchProject;

import java.util.List;

/**
 * 科研项目 Service 接口
 */
public interface ResearchProjectService extends IService<ResearchProject> {

    /**
     * 根据关键词全文检索项目
     *
     * @param keyword 关键词
     * @return 项目列表
     */
    List<ResearchProject> searchByKeyword(String keyword);

    /**
     * 删除科研项目
     *
     * @param id            项目ID
     * @param currentUserId 当前登录用户ID
     * @param currentRole   当前登录用户角色
     * @return 删除是否成功
     */
    boolean deleteProject(Long id, Long currentUserId, String currentRole);

    /**
     * 查询所有招募中的科研项目
     *
     * @return 项目列表
     */
    List<ResearchProject> listRecruitingProjects();
}
