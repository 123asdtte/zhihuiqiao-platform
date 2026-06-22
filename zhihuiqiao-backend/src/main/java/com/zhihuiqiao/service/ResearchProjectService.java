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
}
