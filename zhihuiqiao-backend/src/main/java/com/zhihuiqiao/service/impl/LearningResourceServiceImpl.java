package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.LearningResource;
import com.zhihuiqiao.mapper.LearningResourceMapper;
import com.zhihuiqiao.service.LearningResourceService;
import org.springframework.stereotype.Service;

/**
 * 学习资源 Service 实现类
 */
@Service
public class LearningResourceServiceImpl extends ServiceImpl<LearningResourceMapper, LearningResource> implements LearningResourceService {
}
