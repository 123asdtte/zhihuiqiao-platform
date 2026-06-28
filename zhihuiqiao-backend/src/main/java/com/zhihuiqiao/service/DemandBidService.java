package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.DemandBid;

import java.util.List;

/**
 * 需求承接记录 Service 接口
 */
public interface DemandBidService extends IService<DemandBid> {

    /**
     * 提交承接方案
     */
    DemandBid submitBid(DemandBid bid);

    /**
     * 查询需求下的所有承接方案
     */
    List<DemandBid> listByDemandId(Long demandId);

    /**
     * 企业审核通过承接方案
     */
    boolean approveBid(Long bidId, String remark);

    /**
     * 企业驳回承接方案
     */
    boolean rejectBid(Long bidId, String remark);
}
