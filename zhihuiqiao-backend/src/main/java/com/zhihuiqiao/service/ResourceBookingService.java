package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.ResourceBooking;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源预约 Service 接口
 */
public interface ResourceBookingService extends IService<ResourceBooking> {

    /**
     * 查询指定资源在时间段内的冲突预约
     *
     * @param resourceId 资源ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 冲突预约列表
     */
    List<ResourceBooking> findConflictBookings(Long resourceId, LocalDateTime startTime, LocalDateTime endTime);
}
