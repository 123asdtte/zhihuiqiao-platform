package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.ResourceBooking;
import com.zhihuiqiao.mapper.ResourceBookingMapper;
import com.zhihuiqiao.service.ResourceBookingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源预约 Service 实现类
 */
@Service
public class ResourceBookingServiceImpl extends ServiceImpl<ResourceBookingMapper, ResourceBooking> implements ResourceBookingService {

    @Override
    public List<ResourceBooking> findConflictBookings(Long resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.findConflictBookings(resourceId, startTime, endTime);
    }
}
