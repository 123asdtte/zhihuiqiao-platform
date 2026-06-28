package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.ProjectOutcome;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 项目成果 Mapper 接口
 */
@Mapper
public interface ProjectOutcomeMapper extends BaseMapper<ProjectOutcome> {

    /**
     * 查询项目的全部成果，按创建时间倒序
     */
    @Select("SELECT * FROM project_outcome WHERE project_id = #{projectId} AND deleted = 0 ORDER BY create_time DESC")
    List<ProjectOutcome> listByProjectId(@Param("projectId") Long projectId);
}
