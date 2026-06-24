package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.LearningResource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习资源 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供基础 CRUD 能力
 */
@Mapper
public interface LearningResourceMapper extends BaseMapper<LearningResource> {
}
