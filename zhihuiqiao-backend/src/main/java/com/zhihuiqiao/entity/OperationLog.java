package com.zhihuiqiao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 用于记录用户的操作行为，便于管理员进行审计和追踪
 */
@Data
@TableName("operation_log")
public class OperationLog {

    /**
     * 日志ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作用户ID
     */
    private Long userId;

    /**
     * 操作用户名
     */
    private String username;

    /**
     * 操作用户角色
     */
    private String roleType;

    /**
     * 操作模块，如用户管理、内容审核等
     */
    private String module;

    /**
     * 操作描述，如新增用户、删除资源等
     */
    private String operation;

    /**
     * 请求方法，如 GET、POST、PUT、DELETE
     */
    private String method;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数
     */
    private String requestParams;

    /**
     * 响应状态码
     */
    private Integer responseCode;

    /**
     * 操作IP地址
     */
    private String ipAddress;

    /**
     * 浏览器 User-Agent
     */
    private String userAgent;

    /**
     * 方法执行耗时，单位毫秒
     */
    private Integer executionTime;

    /**
     * 操作状态：0-失败，1-成功
     */
    private Integer status;

    /**
     * 错误信息，操作失败时记录
     */
    private String errorMsg;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;
}
