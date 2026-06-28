package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.DemandBid;
import com.zhihuiqiao.entity.EnterpriseDemand;
import com.zhihuiqiao.mapper.DemandBidMapper;
import com.zhihuiqiao.service.DemandBidService;
import com.zhihuiqiao.service.EnterpriseDemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 需求承接记录 Service 实现类
 */
@Service
public class DemandBidServiceImpl extends ServiceImpl<DemandBidMapper, DemandBid> implements DemandBidService {

    @Autowired
    private DemandBidMapper demandBidMapper;

    @Autowired
    private EnterpriseDemandService enterpriseDemandService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DemandBid submitBid(DemandBid bid) {
        // 同一个用户对同一个需求只能提交一次承接方案
        LambdaQueryWrapper<DemandBid> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DemandBid::getDemandId, bid.getDemandId());
        wrapper.eq(DemandBid::getBidderId, bid.getBidderId());
        wrapper.eq(DemandBid::getDeleted, 0);
        DemandBid exist = demandBidMapper.selectOne(wrapper);
        if (exist != null) {
            throw new RuntimeException("您已经提交过该需求的承接方案");
        }
        bid.setStatus("pending");
        demandBidMapper.insert(bid);
        return bid;
    }

    @Override
    public List<DemandBid> listByDemandId(Long demandId) {
        return demandBidMapper.listByDemandId(demandId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveBid(Long bidId, String remark) {
        DemandBid bid = demandBidMapper.selectById(bidId);
        if (bid == null || bid.getDeleted() == 1) {
            throw new RuntimeException("承接方案不存在");
        }
        bid.setStatus("approved");
        bid.setEnterpriseRemark(remark);
        demandBidMapper.updateById(bid);

        // 更新需求状态为 ongoing
        EnterpriseDemand demand = enterpriseDemandService.getById(bid.getDemandId());
        if (demand != null) {
            demand.setStatus("ongoing");
            enterpriseDemandService.updateById(demand);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectBid(Long bidId, String remark) {
        DemandBid bid = demandBidMapper.selectById(bidId);
        if (bid == null || bid.getDeleted() == 1) {
            throw new RuntimeException("承接方案不存在");
        }
        bid.setStatus("rejected");
        bid.setEnterpriseRemark(remark);
        return demandBidMapper.updateById(bid) > 0;
    }
}
