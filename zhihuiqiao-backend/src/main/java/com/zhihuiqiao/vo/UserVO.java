package com.zhihuiqiao.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    private Long id;

    private String username;

    private String realName;

    private String email;

    private String phone;

    private String avatar;

    private String roleType;

    private String department;

    private String major;

    private String grade;

    private String title;

    private String companyName;

    private Integer status;

    private LocalDateTime createTime;
}
