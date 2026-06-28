package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.ResourceDamageRecord;
import com.zhihuiqiao.mapper.ResourceDamageRecordMapper;
import com.zhihuiqiao.service.ResourceDamageRecordService;
import org.springframework.stereotype.Service;

/**
 * 资源损坏赔偿记录 Service 实现
 */
@Service
public class ResourceDamageRecordServiceImpl extends ServiceImpl<ResourceDamageRecordMapper, ResourceDamageRecord>
        implements ResourceDamageRecordService {
}
