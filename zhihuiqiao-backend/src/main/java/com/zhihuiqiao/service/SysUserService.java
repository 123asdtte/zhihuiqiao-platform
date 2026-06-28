package com.zhihuiqiao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhihuiqiao.dto.LoginRequest;
import com.zhihuiqiao.dto.RegisterRequest;
import com.zhihuiqiao.entity.SysUser;
import com.zhihuiqiao.vo.LoginVO;
import com.zhihuiqiao.vo.UserVO;

public interface SysUserService extends IService<SysUser> {

    LoginVO login(LoginRequest request);

    void register(RegisterRequest request);

    UserVO getCurrentUserInfo(Long userId);

    /**
     * 扣减用户信用分
     * @param userId 用户ID
     * @param score  扣减分数
     * @return 是否成功
     */
    boolean deductCreditScore(Long userId, Integer score);
}
