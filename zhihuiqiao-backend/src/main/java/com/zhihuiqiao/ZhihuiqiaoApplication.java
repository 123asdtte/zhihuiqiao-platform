package com.zhihuiqiao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.zhihuiqiao.**.mapper")
@EnableAsync
public class ZhihuiqiaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhihuiqiaoApplication.class, args);
    }
}
