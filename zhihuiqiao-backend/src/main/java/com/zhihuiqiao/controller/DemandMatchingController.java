package com.zhihuiqiao.controller;

import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.entity.*;
import com.zhihuiqiao.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 需求撮合 Controller
 * 提供需求承接、合同签订、里程碑交付等撮合闭环接口
 */
@Tag(name = "需求撮合模块", description = "企业需求承接、合同、交付相关接口")
@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class DemandMatchingController {

    private final DemandBidService demandBidService;
    private final CooperationContractService contractService;
    private final DeliveryMilestoneService milestoneService;
    private final EnterpriseDemandService enterpriseDemandService;

    // ==================== 需求承接（揭榜） ====================

    @Operation(summary = "提交需求承接方案")
    @PostMapping("/bid")
    public Result<Long> submitBid(@RequestBody DemandBid bid) {
        if (bid.getDemandId() == null) {
            return Result.error("需求ID不能为空");
        }
        if (!StringUtils.hasText(bid.getBidContent())) {
            return Result.error("承接方案不能为空");
        }
        String roleType = getCurrentRoleType();
        if (!"student".equals(roleType) && !"teacher".equals(roleType) && !"admin".equals(roleType)) {
            return Result.error("当前角色无法承接需求");
        }
        Long currentUserId = getCurrentUserId();
        bid.setBidderId(currentUserId);
        bid.setBidderType(roleType);
        return Result.success(demandBidService.submitBid(bid).getId());
    }

    @Operation(summary = "查询需求下的所有承接方案")
    @GetMapping("/bid/list/{demandId}")
    public Result<List<DemandBid>> listBids(@PathVariable Long demandId) {
        return Result.success(demandBidService.listByDemandId(demandId));
    }

    @Operation(summary = "企业审核通过承接方案")
    @PostMapping("/bid/{bidId}/approve")
    public Result<Boolean> approveBid(@PathVariable Long bidId, @RequestParam(required = false) String remark) {
        return Result.success(demandBidService.approveBid(bidId, remark));
    }

    @Operation(summary = "企业驳回承接方案")
    @PostMapping("/bid/{bidId}/reject")
    public Result<Boolean> rejectBid(@PathVariable Long bidId, @RequestParam(required = false) String remark) {
        return Result.success(demandBidService.rejectBid(bidId, remark));
    }

    // ==================== 合同 ====================

    @Operation(summary = "创建合作合同")
    @PostMapping("/contract")
    public Result<Long> createContract(@RequestBody CooperationContract contract) {
        if (contract.getDemandId() == null || contract.getBidId() == null) {
            return Result.error("需求和承接方案ID不能为空");
        }
        // 根据需求和企业方信息补全合同
        EnterpriseDemand demand = enterpriseDemandService.getById(contract.getDemandId());
        if (demand == null) {
            return Result.error("需求不存在");
        }
        // 根据承接方案确定乙方（承接方）
        DemandBid bid = demandBidService.getById(contract.getBidId());
        if (bid == null) {
            return Result.error("承接方案不存在");
        }
        contract.setPartyAId(demand.getEnterpriseId());
        contract.setPartyBId(bid.getBidderId());
        contract.setStatus("unsigned");
        contractService.save(contract);
        return Result.success(contract.getId());
    }

    @Operation(summary = "查询需求关联的合同")
    @GetMapping("/contract/demand/{demandId}")
    public Result<CooperationContract> getContractByDemand(@PathVariable Long demandId) {
        return Result.success(contractService.getByDemandId(demandId));
    }

    @Operation(summary = "签订合同")
    @PostMapping("/contract/{contractId}/sign")
    public Result<Boolean> signContract(@PathVariable Long contractId) {
        return Result.success(contractService.signContract(contractId));
    }

    @Operation(summary = "完成合同")
    @PostMapping("/contract/{contractId}/complete")
    public Result<Boolean> completeContract(@PathVariable Long contractId) {
        return Result.success(contractService.completeContract(contractId));
    }

    // ==================== 里程碑交付 ====================

    @Operation(summary = "创建里程碑")
    @PostMapping("/milestone")
    public Result<Long> createMilestone(@RequestBody DeliveryMilestone milestone) {
        if (milestone.getContractId() == null) {
            return Result.error("合同ID不能为空");
        }
        if (!StringUtils.hasText(milestone.getMilestoneName())) {
            return Result.error("里程碑名称不能为空");
        }
        if (milestone.getStatus() == null) {
            milestone.setStatus("pending");
        }
        milestoneService.save(milestone);
        return Result.success(milestone.getId());
    }

    @Operation(summary = "查询合同下的全部里程碑")
    @GetMapping("/milestone/list/{contractId}")
    public Result<List<DeliveryMilestone>> listMilestones(@PathVariable Long contractId) {
        return Result.success(milestoneService.listByContractId(contractId));
    }

    @Operation(summary = "提交交付物")
    @PostMapping("/milestone/{milestoneId}/submit")
    public Result<Boolean> submitDeliverable(@PathVariable Long milestoneId, @RequestParam String url) {
        return Result.success(milestoneService.submitDeliverable(milestoneId, url));
    }

    @Operation(summary = "企业验收通过")
    @PostMapping("/milestone/{milestoneId}/approve")
    public Result<Boolean> approveDeliverable(@PathVariable Long milestoneId, @RequestParam(required = false) String remark) {
        return Result.success(milestoneService.approveDeliverable(milestoneId, remark));
    }

    @Operation(summary = "企业验收驳回")
    @PostMapping("/milestone/{milestoneId}/reject")
    public Result<Boolean> rejectDeliverable(@PathVariable Long milestoneId, @RequestParam(required = false) String remark) {
        return Result.success(milestoneService.rejectDeliverable(milestoneId, remark));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Long userId) {
            return userId;
        }
        return null;
    }

    private String getCurrentRoleType() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "student";
        }
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority().replace("ROLE_", "").toLowerCase())
                .orElse("student");
    }
}
