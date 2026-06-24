package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息通知 Mapper 接口
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
