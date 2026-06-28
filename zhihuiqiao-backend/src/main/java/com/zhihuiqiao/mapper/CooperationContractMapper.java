package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.CooperationContract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 合作合同 Mapper 接口
 */
@Mapper
public interface CooperationContractMapper extends BaseMapper<CooperationContract> {

    /**
     * 查询需求关联的合同详情
     */
    @Select("SELECT c.*, ua.real_name as party_a_name, ub.real_name as party_b_name " +
            "FROM cooperation_contract c " +
            "LEFT JOIN sys_user ua ON c.party_a_id = ua.id " +
            "LEFT JOIN sys_user ub ON c.party_b_id = ub.id " +
            "WHERE c.demand_id = #{demandId} AND c.deleted = 0")
    CooperationContract getByDemandId(@Param("demandId") Long demandId);
}
