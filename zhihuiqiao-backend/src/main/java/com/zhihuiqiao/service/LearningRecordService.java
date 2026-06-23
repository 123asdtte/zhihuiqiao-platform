package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.LearningRecord;

import java.util.List;

/**
 * 学习记录 Service 接口
 */
public interface LearningRecordService extends IService<LearningRecord> {

    /**
     * 查询用户对指定资源列表的学习记录
     */
    List<LearningRecord> listByUserAndResources(Long userId, List<Long> resourceIds);
}
