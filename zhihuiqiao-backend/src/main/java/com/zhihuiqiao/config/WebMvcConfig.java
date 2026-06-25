package com.zhihuiqiao.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC 配置类
 * 配置本地上传文件的静态资源访问映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 上传文件保存的根目录
     */
    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    /**
     * 文件访问 URL 前缀
     */
    @Value("${file.access.prefix:/uploads}")
    private String accessPrefix;

    /**
     * 添加静态资源映射：将 /uploads/** 映射到本地 uploads 目录
     * 例如访问 /uploads/image/2024/01/01/xxx.jpg 会读取 uploads/image/2024/01/01/xxx.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path targetPath = Paths.get(uploadPath).toAbsolutePath().normalize();
        registry.addResourceHandler(accessPrefix + "/**")
                .addResourceLocations("file:" + targetPath + "/");
    }
}
