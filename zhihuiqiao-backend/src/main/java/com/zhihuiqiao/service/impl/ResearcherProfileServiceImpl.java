package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.ResearcherProfile;
import com.zhihuiqiao.mapper.ResearcherProfileMapper;
import com.zhihuiqiao.service.ResearcherProfileService;
import org.springframework.stereotype.Service;

/**
 * 科研画像 Service 实现类
 */
@Service
public class ResearcherProfileServiceImpl extends ServiceImpl<ResearcherProfileMapper, ResearcherProfile> implements ResearcherProfileService {

    /**
     * 根据用户ID查询科研画像
     */
    @Override
    public ResearcherProfile getByUserId(Long userId) {
        LambdaQueryWrapper<ResearcherProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResearcherProfile::getUserId, userId);
        return getOne(wrapper);
    }
}
