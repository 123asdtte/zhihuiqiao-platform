package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.entity.ProjectApplication;

/**
 * 项目申请 Service 接口
 */
public interface ProjectApplicationService extends IService<ProjectApplication> {

    /**
     * 申请人撤回申请
     */
    boolean withdrawApplication(Long applicationId, Long applicantId);

    /**
     * 发布人将申请标记为待沟通/面试
     */
    boolean interviewApplication(Long applicationId, String replyMessage);
}
