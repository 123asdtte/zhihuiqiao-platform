package com.zhihuiqiao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhihuiqiao.entity.UserPenaltyRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户违约记录 Mapper
 */
@Mapper
public interface UserPenaltyRecordMapper extends BaseMapper<UserPenaltyRecord> {
}
