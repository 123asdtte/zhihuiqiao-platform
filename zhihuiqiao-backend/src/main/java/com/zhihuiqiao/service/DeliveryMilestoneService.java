package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.DeliveryMilestone;

import java.util.List;

/**
 * 交付里程碑 Service 接口
 */
public interface DeliveryMilestoneService extends IService<DeliveryMilestone> {

    /**
     * 查询合同下的全部里程碑
     */
    List<DeliveryMilestone> listByContractId(Long contractId);

    /**
     * 提交交付物
     */
    boolean submitDeliverable(Long milestoneId, String deliverableUrl);

    /**
     * 企业验收通过
     */
    boolean approveDeliverable(Long milestoneId, String remark);

    /**
     * 企业验收驳回
     */
    boolean rejectDeliverable(Long milestoneId, String remark);
}
