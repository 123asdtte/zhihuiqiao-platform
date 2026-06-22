package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.EnterpriseDemand;

import java.util.List;

/**
 * 企业需求 Service 接口
 */
public interface EnterpriseDemandService extends IService<EnterpriseDemand> {

    /**
     * 根据关键词全文检索企业需求
     *
     * @param keyword 关键词
     * @return 企业需求列表
     */
    List<EnterpriseDemand> searchByKeyword(String keyword);
}
