package com.zhihuiqiao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhihuiqiao.common.BusinessException;
import com.zhihuiqiao.common.ResultCode;
import com.zhihuiqiao.constant.RoleType;
import com.zhihuiqiao.constant.UserStatus;
import com.zhihuiqiao.dto.LoginRequest;
import com.zhihuiqiao.dto.RegisterRequest;
import com.zhihuiqiao.entity.SysUser;
import com.zhihuiqiao.mapper.SysUserMapper;
import com.zhihuiqiao.security.JwtUtil;
import com.zhihuiqiao.service.SysUserService;
import com.zhihuiqiao.vo.LoginVO;
import com.zhihuiqiao.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final List<String> VALID_ROLES = Arrays.asList(
            RoleType.STUDENT, RoleType.TEACHER, RoleType.ENTERPRISE, RoleType.ADMIN
    );

    public SysUserServiceImpl(PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginVO login(LoginRequest request) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        SysUser user = getOne(wrapper);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (UserStatus.DISABLED.equals(user.getStatus())) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        if (UserStatus.PENDING.equals(user.getStatus())) {
            throw new BusinessException(ResultCode.ACCOUNT_PENDING);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoleType());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(convertToVO(user));
        return loginVO;
    }

    @Override
    public void register(RegisterRequest request) {
        if (!VALID_ROLES.contains(request.getRoleType())) {
            throw new BusinessException("无效的角色类型");
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        if (count(wrapper) > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoleType(request.getRoleType());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setDepartment(request.getDepartment());
        user.setMajor(request.getMajor());
        user.setGrade(request.getGrade());
        user.setTitle(request.getTitle());
        user.setCompanyName(request.getCompanyName());
        user.setStatus(UserStatus.ENABLED);

        save(user);
    }

    @Override
    public UserVO getCurrentUserInfo(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }

    private UserVO convertToVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
