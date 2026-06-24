package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.OperationLog;

/**
 * 操作日志 Service 接口
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 异步保存操作日志
     *
     * @param operationLog 操作日志对象
     */
    void saveAsync(OperationLog operationLog);

    /**
     * 分页查询操作日志
     *
     * @param pageNum    当前页码
     * @param pageSize   每页大小
     * @param module     操作模块（可选）
     * @param username   操作用户名（可选）
     * @param status     操作状态（可选）
     * @param startTime  开始时间（可选）
     * @param endTime    结束时间（可选）
     * @return 分页结果
     */
    IPage<OperationLog> pageLogs(Integer pageNum, Integer pageSize, String module,
                                  String username, Integer status, String startTime, String endTime);
}
