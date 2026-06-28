package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.IdleResource;
import com.zhihuiqiao.entity.ResourceTransferLog;
import com.zhihuiqiao.entity.ResourceTransferRequest;
import com.zhihuiqiao.entity.SysUser;
import com.zhihuiqiao.mapper.ResourceTransferRequestMapper;
import com.zhihuiqiao.service.IdleResourceService;
import com.zhihuiqiao.service.ResourceTransferLogService;
import com.zhihuiqiao.service.ResourceTransferRequestService;
import com.zhihuiqiao.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源转让意向 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceTransferRequestServiceImpl extends ServiceImpl<ResourceTransferRequestMapper, ResourceTransferRequest>
        implements ResourceTransferRequestService {

    private final IdleResourceService idleResourceService;
    private final ResourceTransferLogService resourceTransferLogService;
    private final SysUserService sysUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitRequest(Long buyerId, Long resourceId, String message, String contactInfo) {
        IdleResource resource = idleResourceService.getById(resourceId);
        if (resource == null || !"transfer".equals(resource.getTradeMode())) {
            throw new IllegalArgumentException("该资源不支持转让");
        }
        if (!"available".equals(resource.getStatus())) {
            throw new IllegalArgumentException("该资源当前不可转让");
        }
        if (resource.getOwnerId().equals(buyerId)) {
            throw new IllegalArgumentException("不能购买自己发布的资源");
        }

        ResourceTransferRequest request = new ResourceTransferRequest();
        request.setResourceId(resourceId);
        request.setBuyerId(buyerId);
        request.setSellerId(resource.getOwnerId());
        request.setStatus("pending");
        request.setMessage(message);
        request.setContactInfo(contactInfo);
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(request);
        return request.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptRequest(Long requestId, Long sellerId) {
        ResourceTransferRequest request = getById(requestId);
        if (request == null || !request.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("无权操作该意向");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("该意向已处理");
        }

        IdleResource resource = idleResourceService.getById(request.getResourceId());
        if (resource == null || !"available".equals(resource.getStatus())) {
            throw new IllegalArgumentException("资源状态已变更");
        }

        request.setStatus("accepted");
        request.setUpdateTime(LocalDateTime.now());
        updateById(request);

        // 关闭同一资源的其他 pending 意向
        LambdaQueryWrapper<ResourceTransferRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceTransferRequest::getResourceId, request.getResourceId())
                .eq(ResourceTransferRequest::getStatus, "pending")
                .ne(ResourceTransferRequest::getId, requestId);
        ResourceTransferRequest rejected = new ResourceTransferRequest();
        rejected.setStatus("rejected");
        rejected.setUpdateTime(LocalDateTime.now());
        baseMapper.update(rejected, wrapper);

        // 更新资源状态为已转让
        resource.setStatus("transferred");
        resource.setUpdateTime(LocalDateTime.now());
        idleResourceService.updateById(resource);

        // 写入流转日志
        ResourceTransferLog transferLog = new ResourceTransferLog();
        transferLog.setResourceId(request.getResourceId());
        transferLog.setFromUserId(resource.getOwnerId());
        transferLog.setToUserId(request.getBuyerId());
        transferLog.setTransferType("transfer");
        transferLog.setRemark("转让成交");
        transferLog.setCreateTime(LocalDateTime.now());
        resourceTransferLogService.save(transferLog);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rejectRequest(Long requestId, Long sellerId) {
        ResourceTransferRequest request = getById(requestId);
        if (request == null || !request.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("无权操作该意向");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("该意向已处理");
        }
        request.setStatus("rejected");
        request.setUpdateTime(LocalDateTime.now());
        return updateById(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRequest(Long requestId, Long buyerId) {
        ResourceTransferRequest request = getById(requestId);
        if (request == null || !request.getBuyerId().equals(buyerId)) {
            throw new IllegalArgumentException("无权操作该意向");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new IllegalArgumentException("只能取消待处理的意向");
        }
        request.setStatus("cancelled");
        request.setUpdateTime(LocalDateTime.now());
        return updateById(request);
    }

    @Override
    public List<ResourceTransferRequest> listSentRequests(Long buyerId) {
        LambdaQueryWrapper<ResourceTransferRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceTransferRequest::getBuyerId, buyerId)
                .orderByDesc(ResourceTransferRequest::getCreateTime);
        return enrich(baseMapper.selectList(wrapper));
    }

    @Override
    public List<ResourceTransferRequest> listReceivedRequests(Long sellerId) {
        LambdaQueryWrapper<ResourceTransferRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceTransferRequest::getSellerId, sellerId)
                .orderByDesc(ResourceTransferRequest::getCreateTime);
        return enrich(baseMapper.selectList(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean review(Long requestId, Long fromUserId, Long toUserId, Integer rating, String comment) {
        ResourceTransferRequest request = getById(requestId);
        if (request == null || !"accepted".equals(request.getStatus())) {
            throw new IllegalArgumentException("只能评价已成交的交易");
        }
        if (!request.getBuyerId().equals(fromUserId) && !request.getSellerId().equals(fromUserId)) {
            throw new IllegalArgumentException("无权评价该交易");
        }
        if (!request.getBuyerId().equals(toUserId) && !request.getSellerId().equals(toUserId)) {
            throw new IllegalArgumentException("评价对象不正确");
        }

        ResourceTransferLog transferLog = resourceTransferLogService.lambdaQuery()
                .eq(ResourceTransferLog::getResourceId, request.getResourceId())
                .eq(ResourceTransferLog::getTransferType, "transfer")
                .eq(ResourceTransferLog::getFromUserId, request.getSellerId())
                .eq(ResourceTransferLog::getToUserId, request.getBuyerId())
                .one();
        if (transferLog == null) {
            throw new IllegalArgumentException("未找到成交记录");
        }

        boolean isSeller = request.getSellerId().equals(fromUserId);
        if (isSeller) {
            transferLog.setFromUserRating(rating);
            transferLog.setFromUserComment(comment);
        } else {
            transferLog.setToUserRating(rating);
            transferLog.setToUserComment(comment);
        }

        // 差评扣分
        if (rating != null && rating <= 2) {
            sysUserService.deductCreditScore(toUserId, 5);
        }

        return resourceTransferLogService.updateById(transferLog);
    }

    private List<ResourceTransferRequest> enrich(List<ResourceTransferRequest> list) {
        list.forEach(item -> {
            IdleResource resource = idleResourceService.getById(item.getResourceId());
            if (resource != null) {
                item.setResourceName(resource.getResourceName());
            }
            SysUser buyer = sysUserService.getById(item.getBuyerId());
            if (buyer != null) {
                item.setBuyerName(buyer.getUsername());
            }
            SysUser seller = sysUserService.getById(item.getSellerId());
            if (seller != null) {
                item.setSellerName(seller.getUsername());
            }
        });
        return list;
    }
}
