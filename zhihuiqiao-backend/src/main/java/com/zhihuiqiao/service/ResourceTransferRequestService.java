package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.ResourceTransferRequest;

import java.util.List;

/**
 * 资源转让意向 Service 接口
 */
public interface ResourceTransferRequestService extends IService<ResourceTransferRequest> {

    /**
     * 提交转让意向
     */
    Long submitRequest(Long buyerId, Long resourceId, String message, String contactInfo);

    /**
     * 卖家确认成交
     */
    boolean acceptRequest(Long requestId, Long sellerId);

    /**
     * 卖家拒绝意向
     */
    boolean rejectRequest(Long requestId, Long sellerId);

    /**
     * 买家取消意向
     */
    boolean cancelRequest(Long requestId, Long buyerId);

    /**
     * 查询买家发出的意向
     */
    List<ResourceTransferRequest> listSentRequests(Long buyerId);

    /**
     * 查询卖家收到的意向
     */
    List<ResourceTransferRequest> listReceivedRequests(Long sellerId);

    /**
     * 交易评价
     */
    boolean review(Long requestId, Long fromUserId, Long toUserId, Integer rating, String comment);
}
