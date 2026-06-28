package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.ProjectMember;
import com.zhihuiqiao.entity.ResearchProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 项目成员 Mapper 接口
 */
@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {

    /**
     * 查询项目的全部有效成员，并关联用户姓名
     */
    @Select("SELECT pm.*, u.real_name as user_name " +
            "FROM project_member pm " +
            "LEFT JOIN sys_user u ON pm.user_id = u.id " +
            "WHERE pm.project_id = #{projectId} AND pm.deleted = 0 AND pm.status = 'active' " +
            "ORDER BY pm.role DESC, pm.join_time ASC")
    List<ProjectMember> listActiveMembersByProjectId(@Param("projectId") Long projectId);

    /**
     * 查询用户加入的所有项目ID
     */
    @Select("SELECT project_id FROM project_member WHERE user_id = #{userId} AND deleted = 0 AND status = 'active'")
    List<Long> listProjectIdsByUserId(@Param("userId") Long userId);
}
