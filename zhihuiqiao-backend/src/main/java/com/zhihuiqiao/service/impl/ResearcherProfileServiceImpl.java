package com.zhihuiqiao.service.impl;

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
}
