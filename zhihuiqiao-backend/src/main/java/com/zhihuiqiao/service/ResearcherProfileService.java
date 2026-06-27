package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.ResearcherProfile;

/**
 * 科研画像 Service 接口
 */
public interface ResearcherProfileService extends IService<ResearcherProfile> {

    /**
     * 根据用户ID查询科研画像
     *
     * @param userId 用户ID
     * @return 科研画像
     */
    ResearcherProfile getByUserId(Long userId);
}
