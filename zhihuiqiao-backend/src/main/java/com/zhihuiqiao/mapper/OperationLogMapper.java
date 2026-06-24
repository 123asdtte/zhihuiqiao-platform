package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper 接口
 * 继承 MyBatis-Plus BaseMapper，提供基础的 CRUD 能力
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
