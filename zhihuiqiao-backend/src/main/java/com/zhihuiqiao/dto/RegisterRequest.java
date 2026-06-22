package com.zhihuiqiao.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "角色类型不能为空")
    private String roleType;

    private String realName;

    private String email;

    private String phone;

    private String department;

    private String major;

    private String grade;

    private String title;

    private String companyName;
}
