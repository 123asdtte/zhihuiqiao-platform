package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.entity.UserPenaltyRecord;
import com.zhihuiqiao.mapper.UserPenaltyRecordMapper;
import com.zhihuiqiao.service.UserPenaltyRecordService;
import org.springframework.stereotype.Service;

/**
 * 用户违约记录 Service 实现
 */
@Service
public class UserPenaltyRecordServiceImpl extends ServiceImpl<UserPenaltyRecordMapper, UserPenaltyRecord>
        implements UserPenaltyRecordService {
}
