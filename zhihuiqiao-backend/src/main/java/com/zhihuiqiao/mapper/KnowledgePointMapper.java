package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.KnowledgePoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 知识点 Mapper 接口
 */
@Mapper
public interface KnowledgePointMapper extends BaseMapper<KnowledgePoint> {

    /**
     * 根据课程名称查询所有知识点
     */
    @Select("SELECT * FROM knowledge_point WHERE deleted = 0 AND course_name = #{courseName} ORDER BY sort_order ASC")
    List<KnowledgePoint> selectByCourseName(@Param("courseName") String courseName);
}
