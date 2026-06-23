package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.KnowledgePoint;
import com.zhihuiqiao.mapper.KnowledgePointMapper;
import com.zhihuiqiao.service.KnowledgePointService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识点 Service 实现类
 */
@Service
public class KnowledgePointServiceImpl extends ServiceImpl<KnowledgePointMapper, KnowledgePoint> implements KnowledgePointService {

    @Override
    public List<KnowledgePoint> getByCourseName(String courseName) {
        LambdaQueryWrapper<KnowledgePoint> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgePoint::getCourseName, courseName);
        wrapper.orderByAsc(KnowledgePoint::getSortOrder);
        return baseMapper.selectList(wrapper);
    }
}
