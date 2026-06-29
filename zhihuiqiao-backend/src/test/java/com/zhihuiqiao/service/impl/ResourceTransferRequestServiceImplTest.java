package com.zhihuiqiao.service.impl;

import com.zhihuiqiao.entity.IdleResource;
import com.zhihuiqiao.entity.ResourceTransferLog;
import com.zhihuiqiao.entity.ResourceTransferRequest;
import com.zhihuiqiao.mapper.ResourceTransferRequestMapper;
import com.zhihuiqiao.service.IdleResourceService;
import com.zhihuiqiao.service.ResourceTransferLogService;
import com.zhihuiqiao.service.SysUserService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * 资源转让意向服务单元测试
 * 验证意向提交、卖家确认成交、拒绝、取消及评价等核心逻辑
 */
@ExtendWith(MockitoExtension.class)
class ResourceTransferRequestServiceImplTest {

    @Mock
    private ResourceTransferRequestMapper mapper;

    @Mock
    private IdleResourceService idleResourceService;

    @Mock
    private ResourceTransferLogService logService;

    @Mock
    private SysUserService sysUserService;

    private ResourceTransferRequestServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ResourceTransferRequestServiceImpl(idleResourceService, logService, sysUserService);
        // 通过反射注入 baseMapper，因为 ServiceImpl 依赖它执行数据库操作
        Field baseMapperField = service.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(service, mapper);
    }

    /**
     * 测试：非转让模式资源提交意向应失败
     */
    @Test
    void submitRequest_shouldFail_whenResourceNotTransferable() {
        IdleResource resource = new IdleResource();
        resource.setId(1L);
        resource.setTradeMode("borrow");
        when(idleResourceService.getById(1L)).thenReturn(resource);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.submitRequest(2L, 1L, "想要", "微信"));
        assertEquals("该资源不支持转让", exception.getMessage());
    }

    /**
     * 测试：转让模式资源但非可用状态提交意向应失败
     */
    @Test
    void submitRequest_shouldFail_whenResourceNotAvailable() {
        IdleResource resource = new IdleResource();
        resource.setId(1L);
        resource.setTradeMode("transfer");
        resource.setStatus("rented");
        when(idleResourceService.getById(1L)).thenReturn(resource);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.submitRequest(2L, 1L, "想要", "微信"));
        assertEquals("该资源当前不可转让", exception.getMessage());
    }

    /**
     * 测试：不能购买自己发布的资源
     */
    @Test
    void submitRequest_shouldFail_whenBuyerIsOwner() {
        IdleResource resource = new IdleResource();
        resource.setId(1L);
        resource.setTradeMode("transfer");
        resource.setStatus("available");
        resource.setOwnerId(2L);
        when(idleResourceService.getById(1L)).thenReturn(resource);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.submitRequest(2L, 1L, "想要", "微信"));
        assertEquals("不能购买自己发布的资源", exception.getMessage());
    }

    /**
     * 测试：成功提交转让意向
     */
    @Test
    void submitRequest_shouldSuccess_whenResourceTransferable() {
        IdleResource resource = new IdleResource();
        resource.setId(1L);
        resource.setTradeMode("transfer");
        resource.setStatus("available");
        resource.setOwnerId(3L);
        when(idleResourceService.getById(1L)).thenReturn(resource);
        when(mapper.insert(any(ResourceTransferRequest.class))).thenAnswer(invocation -> {
            ResourceTransferRequest request = invocation.getArgument(0);
            request.setId(100L);
            return 1;
        });

        Long requestId = service.submitRequest(2L, 1L, "想要", "微信");

        assertNotNull(requestId);
        assertEquals(100L, requestId);
        verify(mapper, times(1)).insert(argThat(req ->
                req.getResourceId().equals(1L)
                        && req.getBuyerId().equals(2L)
                        && req.getSellerId().equals(3L)
                        && "pending".equals(req.getStatus())
                        && "想要".equals(req.getMessage())
                        && "微信".equals(req.getContactInfo())));
    }

    /**
     * 测试：卖家确认成交后更新资源状态并写入流转日志
     */
    @Test
    void acceptRequest_shouldUpdateStatusAndLog() {
        ResourceTransferRequest request = new ResourceTransferRequest();
        request.setId(1L);
        request.setResourceId(10L);
        request.setBuyerId(2L);
        request.setSellerId(1L);
        request.setStatus("pending");
        when(mapper.selectById(1L)).thenReturn(request);

        IdleResource resource = new IdleResource();
        resource.setId(10L);
        resource.setStatus("available");
        resource.setOwnerId(1L);
        when(idleResourceService.getById(10L)).thenReturn(resource);
        when(mapper.update(any(), any())).thenReturn(1);
        when(idleResourceService.updateById(any(IdleResource.class))).thenReturn(true);
        when(logService.save(any(ResourceTransferLog.class))).thenReturn(true);

        boolean result = service.acceptRequest(1L, 1L);

        assertTrue(result);
        verify(idleResourceService).updateById(argThat(r -> "transferred".equals(r.getStatus())));
        verify(logService).save(argThat(log ->
                log.getResourceId().equals(10L)
                        && log.getFromUserId().equals(1L)
                        && log.getToUserId().equals(2L)
                        && "transfer".equals(log.getTransferType())
                        && "转让成交".equals(log.getRemark())));
    }

    /**
     * 测试：非卖家无权确认成交
     */
    @Test
    void acceptRequest_shouldFail_whenNotSeller() {
        ResourceTransferRequest request = new ResourceTransferRequest();
        request.setId(1L);
        request.setSellerId(1L);
        request.setStatus("pending");
        when(mapper.selectById(1L)).thenReturn(request);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.acceptRequest(1L, 2L));
        assertEquals("无权操作该意向", exception.getMessage());
    }

    /**
     * 测试：拒绝意向成功
     */
    @Test
    void rejectRequest_shouldSuccess_whenPending() {
        ResourceTransferRequest request = new ResourceTransferRequest();
        request.setId(1L);
        request.setSellerId(1L);
        request.setStatus("pending");
        when(mapper.selectById(1L)).thenReturn(request);
        when(mapper.updateById(any(ResourceTransferRequest.class))).thenReturn(1);

        boolean result = service.rejectRequest(1L, 1L);

        assertTrue(result);
        verify(mapper).updateById(argThat(req -> "rejected".equals(req.getStatus())));
    }

    /**
     * 测试：买家取消意向成功
     */
    @Test
    void cancelRequest_shouldSuccess_whenPending() {
        ResourceTransferRequest request = new ResourceTransferRequest();
        request.setId(1L);
        request.setBuyerId(2L);
        request.setStatus("pending");
        when(mapper.selectById(1L)).thenReturn(request);
        when(mapper.updateById(any(ResourceTransferRequest.class))).thenReturn(1);

        boolean result = service.cancelRequest(1L, 2L);

        assertTrue(result);
        verify(mapper).updateById(argThat(req -> "cancelled".equals(req.getStatus())));
    }

    /**
     * 测试：卖家对买家评价成功，差评时扣除买家信用分
     */
    @Test
    @SuppressWarnings("unchecked")
    void review_shouldSaveSellerRatingAndDeductCreditScore_whenBadReview() {
        ResourceTransferRequest request = new ResourceTransferRequest();
        request.setId(1L);
        request.setResourceId(10L);
        request.setBuyerId(2L);
        request.setSellerId(1L);
        request.setStatus("accepted");
        when(mapper.selectById(1L)).thenReturn(request);

        ResourceTransferLog log = new ResourceTransferLog();
        log.setId(100L);
        log.setResourceId(10L);
        log.setFromUserId(1L);
        log.setToUserId(2L);
        log.setTransferType("transfer");

        // mock 链式查询包装器，避免真实查询数据库
        LambdaQueryChainWrapper<ResourceTransferLog> lambdaQuery = mock(LambdaQueryChainWrapper.class);
        when(lambdaQuery.eq(any(), any())).thenReturn(lambdaQuery);
        when(lambdaQuery.one()).thenReturn(log);
        when(logService.lambdaQuery()).thenReturn(lambdaQuery);
        when(logService.updateById(any(ResourceTransferLog.class))).thenReturn(true);
        when(sysUserService.deductCreditScore(2L, 5)).thenReturn(true);

        boolean result = service.review(1L, 1L, 2L, 1, "物品与描述不符");

        assertTrue(result);
        verify(sysUserService).deductCreditScore(2L, 5);
        verify(logService).updateById(argThat(l ->
                l.getFromUserRating().equals(1)
                        && "物品与描述不符".equals(l.getFromUserComment())));
    }
}
