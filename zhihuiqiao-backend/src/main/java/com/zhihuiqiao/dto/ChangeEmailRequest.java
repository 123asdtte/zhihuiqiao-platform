package com.zhihuiqiao.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeEmailRequest {

    @NotBlank(message = "登录密码不能为空")
    private String password;

    @NotBlank(message = "新邮箱不能为空")
    @Email(message = "请输入正确的邮箱格式")
    private String email;
}
