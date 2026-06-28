package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.DemandBid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 需求承接记录 Mapper 接口
 */
@Mapper
public interface DemandBidMapper extends BaseMapper<DemandBid> {

    /**
     * 查询需求的全部承接方案，包含承接人姓名和需求标题
     */
    @Select("SELECT b.*, u.real_name as bidder_name, d.demand_title " +
            "FROM demand_bid b " +
            "LEFT JOIN sys_user u ON b.bidder_id = u.id " +
            "LEFT JOIN enterprise_demand d ON b.demand_id = d.id " +
            "WHERE b.demand_id = #{demandId} AND b.deleted = 0 " +
            "ORDER BY b.create_time DESC")
    List<DemandBid> listByDemandId(@Param("demandId") Long demandId);
}
