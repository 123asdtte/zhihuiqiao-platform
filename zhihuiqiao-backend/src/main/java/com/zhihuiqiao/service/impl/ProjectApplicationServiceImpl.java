package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.ProjectApplication;
import com.zhihuiqiao.mapper.ProjectApplicationMapper;
import com.zhihuiqiao.service.ProjectApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 项目申请 Service 实现类
 */
@Service
public class ProjectApplicationServiceImpl extends ServiceImpl<ProjectApplicationMapper, ProjectApplication> implements ProjectApplicationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean withdrawApplication(Long applicationId, Long applicantId) {
        ProjectApplication application = getById(applicationId);
        if (application == null || application.getDeleted() == 1) {
            throw new RuntimeException("申请记录不存在");
        }
        if (!application.getApplicantId().equals(applicantId)) {
            throw new RuntimeException("无权撤回他人申请");
        }
        if (!"pending".equals(application.getStatus())) {
            throw new RuntimeException("当前状态无法撤回");
        }
        application.setStatus("withdrawn");
        application.setHandleTime(LocalDateTime.now());
        return updateById(application);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean interviewApplication(Long applicationId, String replyMessage) {
        ProjectApplication application = getById(applicationId);
        if (application == null || application.getDeleted() == 1) {
            throw new RuntimeException("申请记录不存在");
        }
        if (!"pending".equals(application.getStatus())) {
            throw new RuntimeException("当前状态无法标记为待沟通");
        }
        application.setStatus("interview");
        application.setReplyMessage(replyMessage);
        application.setHandleTime(LocalDateTime.now());
        return updateById(application);
    }
}
