package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.ProjectTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 项目任务 Mapper 接口
 */
@Mapper
public interface ProjectTaskMapper extends BaseMapper<ProjectTask> {

    /**
     * 查询项目的全部任务，并关联负责人姓名
     */
    @Select("SELECT pt.*, u.real_name as assignee_name " +
            "FROM project_task pt " +
            "LEFT JOIN sys_user u ON pt.assignee_id = u.id " +
            "WHERE pt.project_id = #{projectId} AND pt.deleted = 0 " +
            "ORDER BY pt.sort_order ASC, pt.create_time DESC")
    List<ProjectTask> listByProjectId(@Param("projectId") Long projectId);
}
