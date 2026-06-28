package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.CooperationContract;

/**
 * 合作合同 Service 接口
 */
public interface CooperationContractService extends IService<CooperationContract> {

    /**
     * 根据需求ID查询合同
     */
    CooperationContract getByDemandId(Long demandId);

    /**
     * 签订合同
     */
    boolean signContract(Long contractId);

    /**
     * 完成合同
     */
    boolean completeContract(Long contractId);
}
