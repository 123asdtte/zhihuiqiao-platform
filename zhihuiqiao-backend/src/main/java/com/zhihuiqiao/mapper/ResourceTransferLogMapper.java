package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.ResourceTransferLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资源流转记录 Mapper 接口
 * 提供资源流转记录的基础 CRUD 操作
 */
@Mapper
public interface ResourceTransferLogMapper extends BaseMapper<ResourceTransferLog> {
}
