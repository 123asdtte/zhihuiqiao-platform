package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.ResearchProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 科研项目 Mapper 接口
 * 提供科研项目的基础 CRUD 及自定义查询
 */
@Mapper
public interface ResearchProjectMapper extends BaseMapper<ResearchProject> {

    /**
     * 根据关键词进行全文检索项目名称和简介
     *
     * @param keyword 搜索关键词
     * @return 匹配的科研项目列表
     */
    @Select("SELECT * FROM research_project WHERE deleted = 0 AND MATCH(project_name, project_description) AGAINST(#{keyword} IN BOOLEAN MODE)")
    List<ResearchProject> searchByKeyword(@Param("keyword") String keyword);
}
