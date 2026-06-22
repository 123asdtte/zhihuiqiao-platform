package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.ProjectApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目申请 Mapper 接口
 * 提供项目申请的基础 CRUD 操作
 */
@Mapper
public interface ProjectApplicationMapper extends BaseMapper<ProjectApplication> {
}
