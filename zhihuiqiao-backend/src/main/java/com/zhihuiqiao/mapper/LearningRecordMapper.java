package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.LearningRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学习记录 Mapper 接口
 */
@Mapper
public interface LearningRecordMapper extends BaseMapper<LearningRecord> {

    /**
     * 查询用户对指定资源列表的学习记录
     */
    @Select("<script>" +
            "SELECT * FROM learning_record WHERE deleted = 0 " +
            "AND user_id = #{userId} " +
            "AND resource_id IN " +
            "<foreach item='id' collection='resourceIds' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<LearningRecord> selectByUserAndResources(@Param("userId") Long userId, @Param("resourceIds") List<Long> resourceIds);
}
