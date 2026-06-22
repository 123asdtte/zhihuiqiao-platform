package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.EnterpriseDemand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 企业需求 Mapper 接口
 * 提供企业需求的基础 CRUD 及自定义查询
 */
@Mapper
public interface EnterpriseDemandMapper extends BaseMapper<EnterpriseDemand> {

    /**
     * 根据关键词进行全文检索需求标题和描述
     *
     * @param keyword 搜索关键词
     * @return 匹配的企业需求列表
     */
    @Select("SELECT * FROM enterprise_demand WHERE deleted = 0 AND MATCH(demand_title, demand_description) AGAINST(#{keyword} IN BOOLEAN MODE)")
    List<EnterpriseDemand> searchByKeyword(@Param("keyword") String keyword);
}
