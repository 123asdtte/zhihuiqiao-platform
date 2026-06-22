package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.ResourceBooking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源预约 Mapper 接口
 * 提供资源预约的基础 CRUD 及自定义查询
 */
@Mapper
public interface ResourceBookingMapper extends BaseMapper<ResourceBooking> {

    /**
     * 查询指定资源在时间段内状态为 approved 或 ongoing 的预约记录
     * 用于判断是否存在时间冲突
     *
     * @param resourceId 资源ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 冲突的预约列表
     */
    @Select("SELECT * FROM resource_booking WHERE deleted = 0 AND resource_id = #{resourceId} " +
            "AND status IN ('approved', 'ongoing') " +
            "AND ((start_time <= #{startTime} AND end_time > #{startTime}) " +
            "OR (start_time < #{endTime} AND end_time >= #{endTime}) " +
            "OR (start_time >= #{startTime} AND end_time <= #{endTime}))")
    List<ResourceBooking> findConflictBookings(@Param("resourceId") Long resourceId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);
}
