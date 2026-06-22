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
}
