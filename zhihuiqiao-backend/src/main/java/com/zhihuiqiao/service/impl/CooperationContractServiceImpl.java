package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.CooperationContract;
import com.zhihuiqiao.mapper.CooperationContractMapper;
import com.zhihuiqiao.service.CooperationContractService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 合作合同 Service 实现类
 */
@Service
public class CooperationContractServiceImpl extends ServiceImpl<CooperationContractMapper, CooperationContract> implements CooperationContractService {

    @Override
    public CooperationContract getByDemandId(Long demandId) {
        return baseMapper.getByDemandId(demandId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean signContract(Long contractId) {
        CooperationContract contract = getById(contractId);
        if (contract == null || contract.getDeleted() == 1) {
            throw new RuntimeException("合同不存在");
        }
        if (!"unsigned".equals(contract.getStatus())) {
            throw new RuntimeException("合同当前状态不可签订");
        }
        contract.setStatus("signed");
        contract.setSignTime(LocalDateTime.now());
        return updateById(contract);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeContract(Long contractId) {
        CooperationContract contract = getById(contractId);
        if (contract == null || contract.getDeleted() == 1) {
            throw new RuntimeException("合同不存在");
        }
        contract.setStatus("completed");
        return updateById(contract);
    }
}
