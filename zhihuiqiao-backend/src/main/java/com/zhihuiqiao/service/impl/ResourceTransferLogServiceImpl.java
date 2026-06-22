package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.ResourceTransferLog;
import com.zhihuiqiao.mapper.ResourceTransferLogMapper;
import com.zhihuiqiao.service.ResourceTransferLogService;
import org.springframework.stereotype.Service;

/**
 * 资源流转记录 Service 实现类
 */
@Service
public class ResourceTransferLogServiceImpl extends ServiceImpl<ResourceTransferLogMapper, ResourceTransferLog> implements ResourceTransferLogService {
}
