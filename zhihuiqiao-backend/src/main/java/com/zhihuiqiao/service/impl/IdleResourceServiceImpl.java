package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.IdleResource;
import com.zhihuiqiao.mapper.IdleResourceMapper;
import com.zhihuiqiao.service.IdleResourceService;
import org.springframework.stereotype.Service;

/**
 * 闲置资源 Service 实现类
 */
@Service
public class IdleResourceServiceImpl extends ServiceImpl<IdleResourceMapper, IdleResource> implements IdleResourceService {
}
