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

    /**
     * 修改当前用户密码
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更换当前用户邮箱
     * @param userId   用户ID
     * @param password 登录密码
     * @param email    新邮箱
     */
    void changeEmail(Long userId, String password, String email);

    /**
     * 更换当前用户手机号
     * @param userId   用户ID
     * @param password 登录密码
     * @param phone    新手机号
     */
    void changePhone(Long userId, String password, String phone);
}
