package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.CooperationContract;
import com.zhihuiqiao.entity.DeliveryMilestone;
import com.zhihuiqiao.entity.EnterpriseDemand;
import com.zhihuiqiao.mapper.DeliveryMilestoneMapper;
import com.zhihuiqiao.service.CooperationContractService;
import com.zhihuiqiao.service.DeliveryMilestoneService;
import com.zhihuiqiao.service.EnterpriseDemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交付里程碑 Service 实现类
 */
@Service
public class DeliveryMilestoneServiceImpl extends ServiceImpl<DeliveryMilestoneMapper, DeliveryMilestone> implements DeliveryMilestoneService {

    @Autowired
    private CooperationContractService contractService;

    @Autowired
    private EnterpriseDemandService enterpriseDemandService;

    @Override
    public List<DeliveryMilestone> listByContractId(Long contractId) {
        return baseMapper.listByContractId(contractId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitDeliverable(Long milestoneId, String deliverableUrl) {
        DeliveryMilestone milestone = getById(milestoneId);
        if (milestone == null || milestone.getDeleted() == 1) {
            throw new RuntimeException("里程碑不存在");
        }
        milestone.setDeliverableUrl(deliverableUrl);
        milestone.setStatus("submitted");
        milestone.setSubmitTime(LocalDateTime.now());
        return updateById(milestone);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveDeliverable(Long milestoneId, String remark) {
        DeliveryMilestone milestone = getById(milestoneId);
        if (milestone == null || milestone.getDeleted() == 1) {
            throw new RuntimeException("里程碑不存在");
        }
        milestone.setStatus("approved");
        milestone.setEnterpriseRemark(remark);
        milestone.setApproveTime(LocalDateTime.now());
        updateById(milestone);

        // 如果全部里程碑都已验收，则完成合同和需求
        checkAndCompleteContract(milestone.getContractId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectDeliverable(Long milestoneId, String remark) {
        DeliveryMilestone milestone = getById(milestoneId);
        if (milestone == null || milestone.getDeleted() == 1) {
            throw new RuntimeException("里程碑不存在");
        }
        milestone.setStatus("rejected");
        milestone.setEnterpriseRemark(remark);
        return updateById(milestone);
    }

    private void checkAndCompleteContract(Long contractId) {
        List<DeliveryMilestone> list = listByContractId(contractId);
        boolean allApproved = list.stream().allMatch(m -> "approved".equals(m.getStatus()));
        if (allApproved) {
            CooperationContract contract = contractService.getById(contractId);
            if (contract != null) {
                contractService.completeContract(contract.getId());
                EnterpriseDemand demand = enterpriseDemandService.getById(contract.getDemandId());
                if (demand != null) {
                    demand.setStatus("completed");
                    enterpriseDemandService.updateById(demand);
                }
            }
        }
    }
}
