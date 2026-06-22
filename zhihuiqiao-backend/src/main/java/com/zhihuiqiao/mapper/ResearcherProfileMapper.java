package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.ResearcherProfile;
import org.apache.ibatis.annotations.Mapper;

/**
 * 科研画像 Mapper 接口
 * 提供科研画像的基础 CRUD 操作
 */
@Mapper
public interface ResearcherProfileMapper extends BaseMapper<ResearcherProfile> {
}
