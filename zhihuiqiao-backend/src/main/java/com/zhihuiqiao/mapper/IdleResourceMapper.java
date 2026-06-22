package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.IdleResource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 闲置资源 Mapper 接口
 * 提供闲置资源的基础 CRUD 操作
 */
@Mapper
public interface IdleResourceMapper extends BaseMapper<IdleResource> {
}
