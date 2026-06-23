package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.LearningRecord;
import com.zhihuiqiao.mapper.LearningRecordMapper;
import com.zhihuiqiao.service.LearningRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学习记录 Service 实现类
 */
@Service
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord> implements LearningRecordService {

    @Override
    public List<LearningRecord> listByUserAndResources(Long userId, List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return List.of();
        }
        return baseMapper.selectByUserAndResources(userId, resourceIds);
    }
}
