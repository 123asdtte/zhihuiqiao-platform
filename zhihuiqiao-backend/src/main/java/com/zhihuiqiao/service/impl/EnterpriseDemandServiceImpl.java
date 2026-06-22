package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.EnterpriseDemand;
import com.zhihuiqiao.mapper.EnterpriseDemandMapper;
import com.zhihuiqiao.service.EnterpriseDemandService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 企业需求 Service 实现类
 */
@Service
public class EnterpriseDemandServiceImpl extends ServiceImpl<EnterpriseDemandMapper, EnterpriseDemand> implements EnterpriseDemandService {

    @Override
    public List<EnterpriseDemand> searchByKeyword(String keyword) {
        return baseMapper.searchByKeyword(keyword);
    }
}
