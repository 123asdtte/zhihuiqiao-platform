package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.ProjectDynamic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 项目动态 Mapper 接口
 */
@Mapper
public interface ProjectDynamicMapper extends BaseMapper<ProjectDynamic> {

    /**
     * 查询项目的全部动态，按创建时间倒序，并关联发布人姓名
     */
    @Select("SELECT pd.*, u.real_name as publisher_name " +
            "FROM project_dynamic pd " +
            "LEFT JOIN sys_user u ON pd.publisher_id = u.id " +
            "WHERE pd.project_id = #{projectId} AND pd.deleted = 0 " +
            "ORDER BY pd.create_time DESC")
    List<ProjectDynamic> listByProjectId(@Param("projectId") Long projectId);
}
