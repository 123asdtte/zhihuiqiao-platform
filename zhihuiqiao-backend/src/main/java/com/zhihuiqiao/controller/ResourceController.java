package com.zhihuiqiao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.entity.IdleResource;
import com.zhihuiqiao.entity.ResourceBooking;
import com.zhihuiqiao.entity.ResourceTransferLog;
import com.zhihuiqiao.service.IdleResourceService;
import com.zhihuiqiao.service.ResourceBookingService;
import com.zhihuiqiao.service.ResourceTransferLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源流转模块 Controller
 * 提供闲置资源、资源预约、资源流转记录的 RESTful API
 */
@Tag(name = "资源流转模块", description = "闲置资源、资源预约、流转记录相关接口")
@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
public class ResourceController {

    private final IdleResourceService idleResourceService;
    private final ResourceBookingService resourceBookingService;
    private final ResourceTransferLogService resourceTransferLogService;

    // ==================== 闲置资源 ====================

    @Operation(summary = "发布闲置资源")
    @PostMapping("/publish")
    public Result<Long> publishResource(@RequestBody @Valid IdleResource resource) {
        resource.setStatus("available");
        resource.setViews(0);
        idleResourceService.save(resource);
        return Result.success(resource.getId());
    }

    @Operation(summary = "分页查询闲置资源列表")
    @GetMapping("/list")
    public Result<Page<IdleResource>> listResources(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String status) {

        Page<IdleResource> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<IdleResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(IdleResource::getCreateTime);

        if (StringUtils.hasText(resourceType)) {
            wrapper.eq(IdleResource::getResourceType, resourceType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(IdleResource::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(IdleResource::getResourceName, keyword)
                    .or()
                    .like(IdleResource::getDescription, keyword));
        }

        return Result.success(idleResourceService.page(page, wrapper));
    }

    @Operation(summary = "查询资源详情")
    @GetMapping("/{id}")
    public Result<IdleResource> getResourceById(@PathVariable Long id) {
        IdleResource resource = idleResourceService.getById(id);
        if (resource != null) {
            resource.setViews(resource.getViews() + 1);
            idleResourceService.updateById(resource);
        }
        return Result.success(resource);
    }

    @Operation(summary = "更新资源状态")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateResourceStatus(@PathVariable Long id, @RequestParam String status) {
        IdleResource resource = new IdleResource();
        resource.setId(id);
        resource.setStatus(status);
        return Result.success(idleResourceService.updateById(resource));
    }

    // ==================== 资源预约 ====================

    @Operation(summary = "提交资源预约申请")
    @PostMapping("/booking")
    public Result<Long> submitBooking(@RequestBody @Valid ResourceBooking booking) {
        // 校验资源是否存在且可借用
        IdleResource resource = idleResourceService.getById(booking.getResourceId());
        if (resource == null) {
            return Result.error("资源不存在");
        }
        if (!"available".equals(resource.getStatus()) && !"rented".equals(resource.getStatus())) {
            return Result.error("当前资源不可预约");
        }

        // 校验时间是否冲突
        List<ResourceBooking> conflicts = resourceBookingService.findConflictBookings(
                booking.getResourceId(), booking.getStartTime(), booking.getEndTime());
        if (!conflicts.isEmpty()) {
            return Result.error("该时间段已被预约，请选择其他时间");
        }

        booking.setStatus("pending");
        resourceBookingService.save(booking);
        return Result.success(booking.getId());
    }

    @Operation(summary = "查询资源的预约列表")
    @GetMapping("/{resourceId}/bookings")
    public Result<List<ResourceBooking>> listBookingsByResource(@PathVariable Long resourceId) {
        LambdaQueryWrapper<ResourceBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceBooking::getResourceId, resourceId)
                .orderByDesc(ResourceBooking::getCreateTime);
        return Result.success(resourceBookingService.list(wrapper));
    }

    @Operation(summary = "查询我的预约列表")
    @GetMapping("/booking/my")
    public Result<List<ResourceBooking>> listMyBookings(@RequestParam Long borrowerId) {
        LambdaQueryWrapper<ResourceBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceBooking::getBorrowerId, borrowerId)
                .orderByDesc(ResourceBooking::getCreateTime);
        return Result.success(resourceBookingService.list(wrapper));
    }

    @Operation(summary = "审批资源预约")
    @PutMapping("/booking/{id}/audit")
    public Result<Boolean> auditBooking(@PathVariable Long id,
                                       @RequestParam String status,
                                       @RequestParam(required = false) String replyMessage) {
        ResourceBooking booking = resourceBookingService.getById(id);
        if (booking == null) {
            return Result.error("预约记录不存在");
        }

        booking.setStatus(status);
        booking.setReplyMessage(replyMessage);
        boolean result = resourceBookingService.updateById(booking);

        // 审批通过时，记录流转日志并更新资源状态
        if ("approved".equals(status)) {
            IdleResource resource = idleResourceService.getById(booking.getResourceId());
            if (resource != null) {
                resource.setStatus("rented");
                idleResourceService.updateById(resource);

                ResourceTransferLog log = new ResourceTransferLog();
                log.setResourceId(booking.getResourceId());
                log.setBookingId(booking.getId());
                log.setFromUserId(resource.getOwnerId());
                log.setToUserId(booking.getBorrowerId());
                log.setTransferType("borrow");
                log.setRemark("预约审批通过，资源借出");
                log.setCreateTime(LocalDateTime.now());
                resourceTransferLogService.save(log);
            }
        }

        return Result.success(result);
    }

    @Operation(summary = "归还资源")
    @PutMapping("/booking/{id}/return")
    public Result<Boolean> returnResource(@PathVariable Long id) {
        ResourceBooking booking = resourceBookingService.getById(id);
        if (booking == null) {
            return Result.error("预约记录不存在");
        }

        booking.setStatus("returned");
        booking.setReturnTime(LocalDateTime.now());
        boolean result = resourceBookingService.updateById(booking);

        IdleResource resource = idleResourceService.getById(booking.getResourceId());
        if (resource != null) {
            resource.setStatus("available");
            idleResourceService.updateById(resource);

            ResourceTransferLog log = new ResourceTransferLog();
            log.setResourceId(booking.getResourceId());
            log.setBookingId(booking.getId());
            log.setFromUserId(booking.getBorrowerId());
            log.setToUserId(resource.getOwnerId());
            log.setTransferType("return");
            log.setRemark("资源归还");
            log.setCreateTime(LocalDateTime.now());
            resourceTransferLogService.save(log);
        }

        return Result.success(result);
    }

    // ==================== 资源流转记录 ====================

    @Operation(summary = "查询资源的流转记录")
    @GetMapping("/{resourceId}/transfer-logs")
    public Result<List<ResourceTransferLog>> listTransferLogs(@PathVariable Long resourceId) {
        LambdaQueryWrapper<ResourceTransferLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceTransferLog::getResourceId, resourceId)
                .orderByDesc(ResourceTransferLog::getCreateTime);
        return Result.success(resourceTransferLogService.list(wrapper));
    }
}
