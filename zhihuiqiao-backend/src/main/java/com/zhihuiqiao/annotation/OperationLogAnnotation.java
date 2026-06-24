package com.zhihuiqiao.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标注在 Controller 方法上，用于描述该操作属于哪个模块、做了什么操作
 * 配合 AOP 切面自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLogAnnotation {

    /**
     * 操作模块，如用户管理、内容审核、科研项目管理等
     */
    String module() default "";

    /**
     * 操作描述，如新增用户、审核通过、删除资源等
     */
    String operation() default "";

    /**
     * 是否记录请求参数
     */
    boolean saveParams() default true;
}
