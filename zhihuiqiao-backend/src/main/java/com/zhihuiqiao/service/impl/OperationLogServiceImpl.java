package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.OperationLog;
import com.zhihuiqiao.mapper.OperationLogMapper;
import com.zhihuiqiao.service.OperationLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 操作日志 Service 实现类
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
        implements OperationLogService {

    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 异步保存操作日志
     * 使用 Spring 的 @Async 注解，避免日志写入影响主业务响应速度
     *
     * @param operationLog 操作日志对象
     */
    @Override
    @Async
    public void saveAsync(OperationLog operationLog) {
        save(operationLog);
    }

    @Override
    public IPage<OperationLog> pageLogs(Integer pageNum, Integer pageSize, String module,
                                         String username, Integer status, String startTime, String endTime) {
        Page<OperationLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        // 按创建时间倒序排列
        wrapper.orderByDesc(OperationLog::getCreateTime);

        // 按操作模块筛选
        if (StringUtils.hasText(module)) {
            wrapper.like(OperationLog::getModule, module);
        }

        // 按用户名筛选
        if (StringUtils.hasText(username)) {
            wrapper.like(OperationLog::getUsername, username);
        }

        // 按操作状态筛选
        if (status != null) {
            wrapper.eq(OperationLog::getStatus, status);
        }

        // 按开始时间筛选
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(OperationLog::getCreateTime, LocalDateTime.parse(startTime, DATE_TIME_FORMATTER));
        }

        // 按结束时间筛选
        if (StringUtils.hasText(endTime)) {
            wrapper.le(OperationLog::getCreateTime, LocalDateTime.parse(endTime, DATE_TIME_FORMATTER));
        }

        return page(page, wrapper);
    }
}
