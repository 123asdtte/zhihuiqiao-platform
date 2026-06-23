package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.KnowledgePoint;

import java.util.List;

/**
 * 知识点 Service 接口
 */
public interface KnowledgePointService extends IService<KnowledgePoint> {

    /**
     * 根据课程名称查询知识点列表
     */
    List<KnowledgePoint> getByCourseName(String courseName);
}
