package com.zhihuiqiao.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhihuiqiao.common.Result;
import com.zhihuiqiao.entity.OperationLog;
import com.zhihuiqiao.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志 Controller
 * 提供管理员查询操作日志、审计用户行为的接口
 */
@Tag(name = "操作日志", description = "系统操作日志查询与审计")
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
public class OperationLogController {

    /**
     * 操作日志 Service
     */
    private final OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     *
     * @param pageNum   当前页码，默认 1
     * @param pageSize  每页大小，默认 10
     * @param module    操作模块，可选
     * @param username  操作用户名，可选
     * @param status    操作状态：0-失败 1-成功，可选
     * @param startTime 开始时间，格式 yyyy-MM-dd HH:mm:ss，可选
     * @param endTime   结束时间，格式 yyyy-MM-dd HH:mm:ss，可选
     * @return 操作日志分页结果
     */
    @Operation(summary = "分页查询操作日志")
    @GetMapping
    public Result<IPage<OperationLog>> listLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        IPage<OperationLog> page = operationLogService.pageLogs(
                pageNum, pageSize, module, username, status, startTime, endTime);
        return Result.success(page);
    }
}
