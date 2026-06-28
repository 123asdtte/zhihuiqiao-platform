package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.DeliveryMilestone;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 交付里程碑 Mapper 接口
 */
@Mapper
public interface DeliveryMilestoneMapper extends BaseMapper<DeliveryMilestone> {

    /**
     * 查询合同下的全部里程碑
     */
    @Select("SELECT m.*, c.contract_title " +
            "FROM delivery_milestone m " +
            "LEFT JOIN cooperation_contract c ON m.contract_id = c.id " +
            "WHERE m.contract_id = #{contractId} AND m.deleted = 0 " +
            "ORDER BY m.sort_order ASC, m.create_time ASC")
    List<DeliveryMilestone> listByContractId(@Param("contractId") Long contractId);
}
