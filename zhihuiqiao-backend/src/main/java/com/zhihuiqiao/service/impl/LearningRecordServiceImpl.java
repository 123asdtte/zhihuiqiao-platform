package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.LearningRecord;
import com.zhihuiqiao.mapper.LearningRecordMapper;
import com.zhihuiqiao.service.LearningRecordService;
import org.springframework.stereotype.Service;

/**
 * 学习记录 Service 实现类
 */
@Service
public class LearningRecordServiceImpl extends ServiceImpl<LearningRecordMapper, LearningRecord>
        implements LearningRecordService {
}
