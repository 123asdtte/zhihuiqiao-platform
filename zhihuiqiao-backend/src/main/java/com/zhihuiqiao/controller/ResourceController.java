package com.zhihuiqiao.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhihuiqiao.annotation.OperationLogAnnotation;
import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.entity.IdleResource;
import com.zhihuiqiao.entity.ResourceBooking;
import com.zhihuiqiao.entity.ResourceDamageRecord;
import com.zhihuiqiao.entity.ResourceTransferLog;
import com.zhihuiqiao.entity.ResourceTransferRequest;
import com.zhihuiqiao.entity.SysUser;
import com.zhihuiqiao.entity.UserPenaltyRecord;
import com.zhihuiqiao.service.IdleResourceService;
import com.zhihuiqiao.service.NotificationService;
import com.zhihuiqiao.service.ResourceBookingService;
import com.zhihuiqiao.service.ResourceDamageRecordService;
import com.zhihuiqiao.service.ResourceTransferLogService;
import com.zhihuiqiao.service.ResourceTransferRequestService;
import com.zhihuiqiao.service.SysUserService;
import com.zhihuiqiao.service.UserPenaltyRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    private final ResourceDamageRecordService resourceDamageRecordService;
    private final UserPenaltyRecordService userPenaltyRecordService;
    private final SysUserService sysUserService;
    private final NotificationService notificationService;
    private final ResourceTransferRequestService transferRequestService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Long userId) {
            return userId;
        }
        return null;
    }

    /**
     * 获取当前登录用户角色类型
     */
    private String getCurrentRoleType() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "";
        }
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", "").toLowerCase())
                .orElse("");
    }

    // ==================== 闲置资源 ====================

    @OperationLogAnnotation(module = "资源流转", operation = "发布闲置资源")
    @Operation(summary = "发布闲置资源")
    @PostMapping("/publish")
    public Result<Long> publishResource(@RequestBody @Valid IdleResource resource) {
        // 闲置资源仅允许学生、教师或管理员发布，企业账号无此权限
        String roleType = getCurrentRoleType();
        if (!"student".equals(roleType) && !"teacher".equals(roleType) && !"admin".equals(roleType)) {
            return Result.error("无权发布闲置资源");
        }

        // 从 SecurityContext 获取当前登录用户 ID，设置为资源所有者
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Long userId) {
            resource.setOwnerId(userId);
        }
        // 新发布的闲置资源默认进入待审核状态，管理员审核通过后才可预约
        resource.setStatus("pending_audit");
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
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tradeMode,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        Page<IdleResource> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<IdleResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(IdleResource::getCreateTime);

        if (StringUtils.hasText(resourceType)) {
            wrapper.eq(IdleResource::getResourceType, resourceType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(IdleResource::getStatus, status);
        } else {
            // 默认只查询已审核通过的可预约/已租出资源，待审核内容不在列表展示
            wrapper.in(IdleResource::getStatus, List.of("available", "rented", "booked"));
        }
        if (StringUtils.hasText(tradeMode)) {
            wrapper.eq(IdleResource::getTradeMode, tradeMode);
        }
        if (minPrice != null) {
            wrapper.ge(IdleResource::getExpectPrice, minPrice);
        }
        if (maxPrice != null) {
            wrapper.le(IdleResource::getExpectPrice, maxPrice);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(IdleResource::getResourceName, keyword)
                    .or()
                    .like(IdleResource::getDescription, keyword));
        }

        return Result.success(idleResourceService.page(page, wrapper));
    }

    /**
     * 查询当前登录用户发布的资源列表（包含待审核等全部状态）
     */
    @Operation(summary = "查询我的资源列表")
    @GetMapping("/my-list")
    public Result<Page<IdleResource>> listMyResources(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        Page<IdleResource> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<IdleResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IdleResource::getOwnerId, currentUserId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(IdleResource::getStatus, status);
        }
        wrapper.orderByDesc(IdleResource::getCreateTime);
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
        // 仅资源所有者或管理员可修改资源状态
        IdleResource existing = idleResourceService.getById(id);
        if (existing == null) {
            return Result.error("资源不存在");
        }
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (!"admin".equals(currentRole) && !existing.getOwnerId().equals(currentUserId)) {
            return Result.error("无权修改该资源状态");
        }

        IdleResource resource = new IdleResource();
        resource.setId(id);
        resource.setStatus(status);
        return Result.success(idleResourceService.updateById(resource));
    }

    // ==================== 资源预约 ====================

    @Operation(summary = "提交资源预约申请")
    @PostMapping("/booking")
    public Result<Long> submitBooking(@RequestBody @Valid ResourceBooking booking) {
        // 资源预约仅允许学生、教师或管理员提交，企业账号无此权限
        String roleType = getCurrentRoleType();
        if (!"student".equals(roleType) && !"teacher".equals(roleType) && !"admin".equals(roleType)) {
            return Result.error("无权预约闲置资源");
        }

        // 从当前登录用户设置借用人，防止前端伪造
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null) {
            booking.setBorrowerId(currentUserId);
        }

        // 校验资源是否存在且可借用
        IdleResource resource = idleResourceService.getById(booking.getResourceId());
        if (resource == null) {
            return Result.error("资源不存在");
        }
        if (!"available".equals(resource.getStatus()) && !"rented".equals(resource.getStatus())) {
            return Result.error("当前资源不可预约");
        }

        // 校验开始时间不能早于当前时间（禁止预约今天以前的时间）
        if (booking.getStartTime() == null || booking.getStartTime().isBefore(LocalDateTime.now())) {
            return Result.error("开始时间不能早于当前时间，请选择今天及以后的时间");
        }

        // 校验结束时间必须晚于开始时间
        if (booking.getEndTime() == null || !booking.getEndTime().isAfter(booking.getStartTime())) {
            return Result.error("结束时间必须晚于开始时间");
        }

        // 校验时间是否冲突
        List<ResourceBooking> conflicts = resourceBookingService.findConflictBookings(
                booking.getResourceId(), booking.getStartTime(), booking.getEndTime());
        if (!conflicts.isEmpty()) {
            return Result.error("该时间段已被预约，请选择其他时间");
        }

        booking.setStatus("pending");
        resourceBookingService.save(booking);

        // 发送通知给资源所有者
        if (resource != null) {
            notificationService.sendNotification(
                    resource.getOwnerId(),
                    "新的资源预约申请",
                    "您的资源「" + resource.getResourceName() + "」收到了新的预约申请，请及时处理。",
                    "booking",
                    booking.getId(),
                    "resource_booking"
            );
        }

        return Result.success(booking.getId());
    }

    @Operation(summary = "查询资源的预约列表")
    @GetMapping("/{resourceId}/bookings")
    public Result<List<ResourceBooking>> listBookingsByResource(@PathVariable Long resourceId) {
        IdleResource resource = idleResourceService.getById(resourceId);
        if (resource == null) {
            return Result.success(List.of());
        }

        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }

        LambdaQueryWrapper<ResourceBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceBooking::getResourceId, resourceId);

        // 管理员或资源所有者：查看该资源全部预约
        // 普通借用人：只能查看自己的预约
        if (!"admin".equals(currentRole) && !resource.getOwnerId().equals(currentUserId)) {
            wrapper.eq(ResourceBooking::getBorrowerId, currentUserId);
        }

        wrapper.orderByDesc(ResourceBooking::getCreateTime);
        List<ResourceBooking> bookings = resourceBookingService.list(wrapper);
        enrichBookings(bookings);
        return Result.success(bookings);
    }

    @Operation(summary = "查询我的预约列表（管理员可查看全部预约）")
    @GetMapping("/booking/my")
    public Result<List<ResourceBooking>> listMyBookings(@RequestParam(required = false) Long borrowerId) {
        return doListBookings(borrowerId, false);
    }

    @Operation(summary = "查询我收到的预约列表（资源所有者视角）")
    @GetMapping("/booking/owner")
    public Result<List<ResourceBooking>> listOwnerBookings(@RequestParam(required = false) Long resourceId) {
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        // 查询当前用户拥有的所有资源
        LambdaQueryWrapper<IdleResource> resourceWrapper = new LambdaQueryWrapper<>();
        resourceWrapper.eq(IdleResource::getOwnerId, currentUserId);
        if (resourceId != null) {
            resourceWrapper.eq(IdleResource::getId, resourceId);
        }
        List<Long> resourceIds = idleResourceService.list(resourceWrapper).stream()
                .map(IdleResource::getId)
                .toList();

        if (resourceIds.isEmpty()) {
            return Result.success(List.of());
        }

        LambdaQueryWrapper<ResourceBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ResourceBooking::getResourceId, resourceIds)
                .orderByDesc(ResourceBooking::getCreateTime);
        List<ResourceBooking> bookings = resourceBookingService.list(wrapper);
        enrichBookings(bookings);
        return Result.success(bookings);
    }

    /**
     * 公共方法：查询预约列表并填充资源名称、检测超期
     */
    private Result<List<ResourceBooking>> doListBookings(Long filterUserId, boolean ownerView) {
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();

        LambdaQueryWrapper<ResourceBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ResourceBooking::getCreateTime);

        if ("admin".equals(currentRole)) {
            if (filterUserId != null) {
                wrapper.eq(ResourceBooking::getBorrowerId, filterUserId);
            }
        } else {
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            wrapper.eq(ResourceBooking::getBorrowerId, currentUserId);
        }

        List<ResourceBooking> bookings = resourceBookingService.list(wrapper);
        enrichBookings(bookings);
        return Result.success(bookings);
    }

    /**
     * 填充预约列表的展示信息并检测超期
     */
    private void enrichBookings(List<ResourceBooking> bookings) {
        LocalDateTime now = LocalDateTime.now();
        bookings.forEach(booking -> {
            IdleResource resource = idleResourceService.getById(booking.getResourceId());
            if (resource != null) {
                booking.setResourceName(resource.getResourceName());
            }

            if (List.of("approved", "ongoing", "return_request").contains(booking.getStatus())
                    && booking.getEndTime() != null
                    && now.isAfter(booking.getEndTime())) {
                booking.setOverdueStatus("overdue");
            }
        });
    }

    @OperationLogAnnotation(module = "资源流转", operation = "审批资源预约")
    @Operation(summary = "审批资源预约")
    @PutMapping("/booking/{id}/audit")
    public Result<Boolean> auditBooking(@PathVariable Long id,
                                       @RequestParam String status,
                                       @RequestParam(required = false) String replyMessage) {
        ResourceBooking booking = resourceBookingService.getById(id);
        if (booking == null) {
            return Result.error("预约记录不存在");
        }

        // 仅资源所有者或管理员可审批该资源的预约申请
        IdleResource resource = idleResourceService.getById(booking.getResourceId());
        if (resource == null) {
            return Result.error("资源不存在");
        }
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (!"admin".equals(currentRole) && !resource.getOwnerId().equals(currentUserId)) {
            return Result.error("无权审批该预约");
        }

        booking.setStatus(status);
        booking.setReplyMessage(replyMessage);
        boolean result = resourceBookingService.updateById(booking);

        // 审批通过时，预约进入进行中状态，资源变为已租出
        if ("approved".equals(status) && resource != null) {
            booking.setStatus("ongoing");
            booking.setUpdateTime(LocalDateTime.now());
            resourceBookingService.updateById(booking);

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

        // 发送审核结果通知给借用人
        if (resource != null) {
            String auditResult = "approved".equals(status) ? "已通过" : "未通过";
            notificationService.sendNotification(
                    booking.getBorrowerId(),
                    "资源预约" + auditResult,
                    "您对资源「" + resource.getResourceName() + "」的预约申请" + auditResult +
                            (StringUtils.hasText(replyMessage) ? "，回复：" + replyMessage : "。"),
                    "booking",
                    booking.getId(),
                    "resource_booking"
            );
        }

        return Result.success(result);
    }

    @OperationLogAnnotation(module = "资源流转", operation = "取消预约")
    @Operation(summary = "取消资源预约")
    @PutMapping("/booking/{id}/cancel")
    public Result<Boolean> cancelBooking(@PathVariable Long id) {
        ResourceBooking booking = resourceBookingService.getById(id);
        if (booking == null) {
            return Result.error("预约记录不存在");
        }

        // 只有待审批状态的预约可以取消
        if (!"pending".equals(booking.getStatus())) {
            return Result.error("只有待审批的预约可以取消");
        }

        // 校验权限：借用人本人或管理员可以取消
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (!"admin".equals(currentRole) && !booking.getBorrowerId().equals(currentUserId)) {
            return Result.error("无权取消该预约");
        }

        booking.setStatus("cancelled");
        booking.setUpdateTime(LocalDateTime.now());
        boolean result = resourceBookingService.updateById(booking);

        // 发送取消通知给资源所有者
        IdleResource resource = idleResourceService.getById(booking.getResourceId());
        if (resource != null) {
            notificationService.sendNotification(
                    resource.getOwnerId(),
                    "资源预约已取消",
                    "用户对资源「" + resource.getResourceName() + "」的预约申请已取消。",
                    "booking",
                    booking.getId(),
                    "resource_booking"
            );
        }

        return Result.success(result);
    }

    @OperationLogAnnotation(module = "资源流转", operation = "申请归还资源")
    @Operation(summary = "借用方申请归还资源")
    @PutMapping("/booking/{id}/return-request")
    public Result<Boolean> requestReturn(@PathVariable Long id) {
        ResourceBooking booking = resourceBookingService.getById(id);
        if (booking == null) {
            return Result.error("预约记录不存在");
        }

        // 仅借用方本人或管理员可申请归还
        IdleResource resource = idleResourceService.getById(booking.getResourceId());
        if (resource == null) {
            return Result.error("资源不存在");
        }
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (!"admin".equals(currentRole) && !booking.getBorrowerId().equals(currentUserId)) {
            return Result.error("无权申请归还该资源");
        }

        if (!"ongoing".equals(booking.getStatus()) && !"approved".equals(booking.getStatus())) {
            return Result.error("当前状态不可申请归还");
        }

        booking.setStatus("return_request");
        booking.setReturnRequestTime(LocalDateTime.now());
        booking.setUpdateTime(LocalDateTime.now());
        boolean result = resourceBookingService.updateById(booking);

        // 通知资源所有者确认归还
        notificationService.sendNotification(
                resource.getOwnerId(),
                "资源归还待确认",
                "用户对资源「" + resource.getResourceName() + "」发起了归还申请，请及时确认。",
                "booking",
                booking.getId(),
                "resource_booking"
        );

        return Result.success(result);
    }

    @OperationLogAnnotation(module = "资源流转", operation = "确认归还资源")
    @Operation(summary = "资源所有者确认归还资源")
    @PutMapping("/booking/{id}/return-confirm")
    public Result<Boolean> confirmReturn(@PathVariable Long id) {
        ResourceBooking booking = resourceBookingService.getById(id);
        if (booking == null) {
            return Result.error("预约记录不存在");
        }

        IdleResource resource = idleResourceService.getById(booking.getResourceId());
        if (resource == null) {
            return Result.error("资源不存在");
        }

        // 仅资源所有者或管理员可确认归还
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (!"admin".equals(currentRole) && !resource.getOwnerId().equals(currentUserId)) {
            return Result.error("无权确认归还该资源");
        }

        if (!"return_request".equals(booking.getStatus())) {
            return Result.error("当前状态不可确认归还");
        }

        LocalDateTime now = LocalDateTime.now();
        booking.setStatus("return_confirmed");
        booking.setReturnConfirmTime(now);
        booking.setReturnTime(now);
        booking.setUpdateTime(now);

        // 检查是否超期：实际确认归还时间晚于预约结束时间
        if (booking.getEndTime() != null && now.isAfter(booking.getEndTime())) {
            booking.setOverdueStatus("overdue");
            createOverduePenalty(booking, resource);
        } else {
            booking.setOverdueStatus("normal");
        }

        boolean result = resourceBookingService.updateById(booking);

        // 资源重新变为可预约状态
        resource.setStatus("available");
        idleResourceService.updateById(resource);

        // 记录流转日志
        ResourceTransferLog log = new ResourceTransferLog();
        log.setResourceId(booking.getResourceId());
        log.setBookingId(booking.getId());
        log.setFromUserId(booking.getBorrowerId());
        log.setToUserId(resource.getOwnerId());
        log.setTransferType("return");
        log.setRemark("资源归还确认");
        log.setCreateTime(now);
        resourceTransferLogService.save(log);

        // 通知借用人归还已确认
        notificationService.sendNotification(
                booking.getBorrowerId(),
                "资源归还已确认",
                "您对资源「" + resource.getResourceName() + "」的归还申请已被确认。",
                "booking",
                booking.getId(),
                "resource_booking"
        );

        return Result.success(result);
    }

    /**
     * 创建超期违约记录并扣除信用分
     */
    private void createOverduePenalty(ResourceBooking booking, IdleResource resource) {
        SysUser borrower = sysUserService.getById(booking.getBorrowerId());
        if (borrower == null) {
            return;
        }

        int penaltyScore = 10;
        borrower.setCreditScore(Math.max(0, (borrower.getCreditScore() == null ? 100 : borrower.getCreditScore()) - penaltyScore));
        sysUserService.updateById(borrower);

        UserPenaltyRecord penalty = new UserPenaltyRecord();
        penalty.setUserId(booking.getBorrowerId());
        penalty.setPenaltyType("overdue");
        penalty.setRelatedBookingId(booking.getId());
        penalty.setDescription("资源「" + resource.getResourceName() + "」归还超期");
        penalty.setPenaltyScore(penaltyScore);
        penalty.setStatus("active");
        userPenaltyRecordService.save(penalty);

        notificationService.sendNotification(
                booking.getBorrowerId(),
                "资源归还超期",
                "您对资源「" + resource.getResourceName() + "」的归还已超期，信用分扣除 " + penaltyScore + " 分。",
                "system",
                booking.getId(),
                "user_penalty"
        );
    }

    // ==================== 损坏赔偿记录 ====================

    @OperationLogAnnotation(module = "资源流转", operation = "上报资源损坏")
    @Operation(summary = "上报资源损坏赔偿")
    @PostMapping("/booking/{id}/damage")
    public Result<Long> reportDamage(@PathVariable Long id, @RequestBody ResourceDamageRecord damageRecord) {
        ResourceBooking booking = resourceBookingService.getById(id);
        if (booking == null) {
            return Result.error("预约记录不存在");
        }

        IdleResource resource = idleResourceService.getById(booking.getResourceId());
        if (resource == null) {
            return Result.error("资源不存在");
        }

        // 仅资源所有者或管理员可上报损坏
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (!"admin".equals(currentRole) && !resource.getOwnerId().equals(currentUserId)) {
            return Result.error("无权上报资源损坏");
        }

        damageRecord.setBookingId(id);
        damageRecord.setResourceId(booking.getResourceId());
        damageRecord.setReporterId(currentUserId);
        damageRecord.setStatus("pending");
        resourceDamageRecordService.save(damageRecord);

        // 更新预约损坏状态
        booking.setOverdueStatus("damage_pending".equals(booking.getOverdueStatus()) ? "damage_pending" : "damage_pending");
        resourceBookingService.updateById(booking);

        // 通知借用人处理赔偿
        notificationService.sendNotification(
                booking.getBorrowerId(),
                "资源损坏赔偿通知",
                "您借用的资源「" + resource.getResourceName() + "」被上报损坏，请及时处理赔偿事宜。",
                "booking",
                booking.getId(),
                "resource_damage"
        );

        return Result.success(damageRecord.getId());
    }

    @OperationLogAnnotation(module = "资源流转", operation = "处理资源损坏赔偿")
    @Operation(summary = "处理资源损坏赔偿")
    @PutMapping("/damage/{id}/resolve")
    public Result<Boolean> resolveDamage(@PathVariable Long id, @RequestParam(required = false) String resolveRemark) {
        ResourceDamageRecord damageRecord = resourceDamageRecordService.getById(id);
        if (damageRecord == null) {
            return Result.error("损坏记录不存在");
        }

        ResourceBooking booking = resourceBookingService.getById(damageRecord.getBookingId());
        if (booking == null) {
            return Result.error("预约记录不存在");
        }

        IdleResource resource = idleResourceService.getById(damageRecord.getResourceId());
        if (resource == null) {
            return Result.error("资源不存在");
        }

        // 仅资源所有者或管理员可处理赔偿
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (!"admin".equals(currentRole) && !resource.getOwnerId().equals(currentUserId)) {
            return Result.error("无权处理该赔偿记录");
        }

        damageRecord.setStatus("resolved");
        damageRecord.setResolveRemark(resolveRemark);
        damageRecord.setUpdateTime(LocalDateTime.now());
        boolean result = resourceDamageRecordService.updateById(damageRecord);

        // 创建违约记录并扣分
        SysUser borrower = sysUserService.getById(booking.getBorrowerId());
        if (borrower != null && damageRecord.getCompensationAmount() != null
                && damageRecord.getCompensationAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            int penaltyScore = damageRecord.getCompensationAmount().intValue() / 10;
            penaltyScore = Math.max(5, Math.min(penaltyScore, 50));
            borrower.setCreditScore(Math.max(0, (borrower.getCreditScore() == null ? 100 : borrower.getCreditScore()) - penaltyScore));
            sysUserService.updateById(borrower);

            UserPenaltyRecord penalty = new UserPenaltyRecord();
            penalty.setUserId(booking.getBorrowerId());
            penalty.setPenaltyType("damage");
            penalty.setRelatedBookingId(booking.getId());
            penalty.setDescription("资源「" + resource.getResourceName() + "」损坏赔偿");
            penalty.setPenaltyScore(penaltyScore);
            penalty.setStatus("active");
            userPenaltyRecordService.save(penalty);

            notificationService.sendNotification(
                    booking.getBorrowerId(),
                    "资源损坏赔偿已处理",
                    "您对资源「" + resource.getResourceName() + "」的损坏赔偿已处理，信用分扣除 " + penaltyScore + " 分。",
                    "system",
                    booking.getId(),
                    "user_penalty"
            );
        }

        return Result.success(result);
    }

    @Operation(summary = "查询损坏赔偿记录列表")
    @GetMapping("/damage-records")
    public Result<List<ResourceDamageRecord>> listDamageRecords(@RequestParam(required = false) Long resourceId,
                                                                @RequestParam(required = false) Long bookingId) {
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (currentUserId == null) {
            return Result.error("用户未登录");
        }

        LambdaQueryWrapper<ResourceDamageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ResourceDamageRecord::getCreateTime);

        if (resourceId != null) {
            wrapper.eq(ResourceDamageRecord::getResourceId, resourceId);
        }
        if (bookingId != null) {
            wrapper.eq(ResourceDamageRecord::getBookingId, bookingId);
        }

        // 非管理员只能查看自己相关记录（上报人或关联预约的借用人/所有者）
        if (!"admin".equals(currentRole)) {
            wrapper.and(w -> w.eq(ResourceDamageRecord::getReporterId, currentUserId)
                    .or()
                    .inSql(ResourceDamageRecord::getBookingId,
                            "SELECT id FROM resource_booking WHERE borrower_id = " + currentUserId));
        }

        return Result.success(resourceDamageRecordService.list(wrapper));
    }

    // ==================== 资源日历 ====================

    @Operation(summary = "查询资源预约日历")
    @GetMapping("/{resourceId}/calendar")
    public Result<List<java.util.Map<String, Object>>> getResourceCalendar(
            @PathVariable Long resourceId,
            @RequestParam String startDate,
            @RequestParam String endDate) {

        IdleResource resource = idleResourceService.getById(resourceId);
        if (resource == null) {
            return Result.error("资源不存在");
        }

        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
        java.time.LocalDate end = java.time.LocalDate.parse(endDate);

        // 查询审批通过及进行中的预约
        LambdaQueryWrapper<ResourceBooking> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceBooking::getResourceId, resourceId)
                .in(ResourceBooking::getStatus, List.of("approved", "ongoing", "return_request"))
                .ge(ResourceBooking::getStartTime, start.atStartOfDay())
                .le(ResourceBooking::getEndTime, end.plusDays(1).atStartOfDay());
        List<ResourceBooking> bookings = resourceBookingService.list(wrapper);

        Long currentUserId = getCurrentUserId();

        List<java.util.Map<String, Object>> calendar = new java.util.ArrayList<>();
        for (java.time.LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            java.time.LocalDateTime dayStart = date.atStartOfDay();
            java.time.LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            boolean hasMyBooking = false;
            boolean hasOthersBooking = false;

            for (ResourceBooking b : bookings) {
                if (b.getStartTime() == null || b.getEndTime() == null) {
                    continue;
                }
                if (b.getStartTime().isBefore(dayEnd) && b.getEndTime().isAfter(dayStart)) {
                    if (currentUserId != null && currentUserId.equals(b.getBorrowerId())) {
                        hasMyBooking = true;
                    } else {
                        hasOthersBooking = true;
                    }
                }
            }

            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("date", date.toString());
            item.put("available", !hasMyBooking && !hasOthersBooking);
            item.put("hasMyBooking", hasMyBooking);
            item.put("hasOthersBooking", hasOthersBooking);
            calendar.add(item);
        }

        return Result.success(calendar);
    }

    // ==================== 资源流转记录 ====================

    @Operation(summary = "查询资源的流转记录")
    @GetMapping("/{resourceId}/transfer-logs")
    public Result<List<ResourceTransferLog>> listTransferLogs(@PathVariable Long resourceId) {
        // 仅资源所有者或管理员可查看流转记录
        IdleResource resource = idleResourceService.getById(resourceId);
        if (resource == null) {
            return Result.success(List.of());
        }
        Long currentUserId = getCurrentUserId();
        String currentRole = getCurrentRoleType();
        if (!"admin".equals(currentRole) && !resource.getOwnerId().equals(currentUserId)) {
            return Result.error("无权查看该资源的流转记录");
        }

        LambdaQueryWrapper<ResourceTransferLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceTransferLog::getResourceId, resourceId)
                .orderByDesc(ResourceTransferLog::getCreateTime);
        return Result.success(resourceTransferLogService.list(wrapper));
    }

    // ==================== 资源转让意向 ====================

    @OperationLogAnnotation(module = "资源流转", operation = "提交转让意向")
    @Operation(summary = "提交转让意向")
    @PostMapping("/transfer/request")
    public Result<Long> submitTransferRequest(@RequestBody @Valid TransferRequestDTO dto) {
        Long buyerId = getCurrentUserId();
        if (buyerId == null) {
            return Result.error("用户未登录");
        }
        Long requestId = transferRequestService.submitRequest(buyerId, dto.getResourceId(), dto.getMessage(), dto.getContactInfo());

        // 通知卖家有新的转让意向
        IdleResource resource = idleResourceService.getById(dto.getResourceId());
        if (resource != null) {
            notificationService.sendNotification(
                    resource.getOwnerId(),
                    "新的资源转让意向",
                    "您的资源「" + resource.getResourceName() + "」收到了新的转让意向，请查看。",
                    "resource",
                    requestId,
                    "resource_transfer_request"
            );
        }

        return Result.success(requestId);
    }

    @Operation(summary = "我发出的转让意向")
    @GetMapping("/transfer/requests/sent")
    public Result<List<ResourceTransferRequest>> listSentTransferRequests() {
        Long buyerId = getCurrentUserId();
        if (buyerId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(transferRequestService.listSentRequests(buyerId));
    }

    @Operation(summary = "我收到的转让意向")
    @GetMapping("/transfer/requests/received")
    public Result<List<ResourceTransferRequest>> listReceivedTransferRequests() {
        Long sellerId = getCurrentUserId();
        if (sellerId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(transferRequestService.listReceivedRequests(sellerId));
    }

    @OperationLogAnnotation(module = "资源流转", operation = "确认转让成交")
    @Operation(summary = "卖家确认转让成交")
    @PutMapping("/transfer/request/{id}/accept")
    public Result<Boolean> acceptTransferRequest(@PathVariable Long id) {
        Long sellerId = getCurrentUserId();
        if (sellerId == null) {
            return Result.error("用户未登录");
        }
        boolean result = transferRequestService.acceptRequest(id, sellerId);

        // 通知买家意向已确认
        ResourceTransferRequest request = transferRequestService.getById(id);
        IdleResource resource = request != null ? idleResourceService.getById(request.getResourceId()) : null;
        if (request != null && resource != null) {
            notificationService.sendNotification(
                    request.getBuyerId(),
                    "资源转让意向已确认",
                    "您对资源「" + resource.getResourceName() + "」的转让意向已被卖家确认，请线下交割。",
                    "resource",
                    id,
                    "resource_transfer_request"
            );
        }

        return Result.success(result);
    }

    @OperationLogAnnotation(module = "资源流转", operation = "拒绝转让意向")
    @Operation(summary = "卖家拒绝转让意向")
    @PutMapping("/transfer/request/{id}/reject")
    public Result<Boolean> rejectTransferRequest(@PathVariable Long id) {
        Long sellerId = getCurrentUserId();
        if (sellerId == null) {
            return Result.error("用户未登录");
        }
        boolean result = transferRequestService.rejectRequest(id, sellerId);

        // 通知买家意向被拒绝
        ResourceTransferRequest request = transferRequestService.getById(id);
        IdleResource resource = request != null ? idleResourceService.getById(request.getResourceId()) : null;
        if (request != null && resource != null) {
            notificationService.sendNotification(
                    request.getBuyerId(),
                    "资源转让意向被拒绝",
                    "您对资源「" + resource.getResourceName() + "」的转让意向已被卖家拒绝。",
                    "resource",
                    id,
                    "resource_transfer_request"
            );
        }

        return Result.success(result);
    }

    @OperationLogAnnotation(module = "资源流转", operation = "取消转让意向")
    @Operation(summary = "买家取消转让意向")
    @PutMapping("/transfer/request/{id}/cancel")
    public Result<Boolean> cancelTransferRequest(@PathVariable Long id) {
        Long buyerId = getCurrentUserId();
        if (buyerId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(transferRequestService.cancelRequest(id, buyerId));
    }

    @OperationLogAnnotation(module = "资源流转", operation = "交易评价")
    @Operation(summary = "交易评价")
    @PostMapping("/transfer/request/{id}/review")
    public Result<Boolean> reviewTransferRequest(@PathVariable Long id,
                                                 @RequestBody @Valid TransferReviewDTO dto) {
        Long fromUserId = getCurrentUserId();
        if (fromUserId == null) {
            return Result.error("用户未登录");
        }
        return Result.success(transferRequestService.review(id, fromUserId, dto.getTargetUserId(), dto.getRating(), dto.getComment()));
    }

    @Data
    public static class TransferRequestDTO {
        @NotNull(message = "资源ID不能为空")
        private Long resourceId;
        @Size(max = 500, message = "留言不超过500字")
        private String message;
        @Size(max = 255, message = "联系方式不超过255字")
        private String contactInfo;
    }

    @Data
    public static class TransferReviewDTO {
        @NotNull(message = "评价对象不能为空")
        private Long targetUserId;
        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分最低1分")
        @Max(value = 5, message = "评分最高5分")
        private Integer rating;
        @Size(max = 500, message = "评价内容不超过500字")
        private String comment;
    }
}
