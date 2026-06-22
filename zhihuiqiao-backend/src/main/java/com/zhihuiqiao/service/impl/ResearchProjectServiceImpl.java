package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.ResearchProject;
import com.zhihuiqiao.mapper.ResearchProjectMapper;
import com.zhihuiqiao.service.ResearchProjectService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 科研项目 Service 实现类
 */
@Service
public class ResearchProjectServiceImpl extends ServiceImpl<ResearchProjectMapper, ResearchProject> implements ResearchProjectService {

    @Override
    public List<ResearchProject> searchByKeyword(String keyword) {
        return baseMapper.searchByKeyword(keyword);
    }
}
