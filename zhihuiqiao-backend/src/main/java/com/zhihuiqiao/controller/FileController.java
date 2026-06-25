package com.zhihuiqiao.controller;

import com.zhihuiqiao.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通用文件上传 Controller
 * 提供图片、附件等文件上传能力，文件存储在本地 uploads 目录
 */
@Tag(name = "通用文件", description = "文件上传相关接口")
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class FileController {

    /**
     * 上传文件保存的根目录，默认使用项目运行目录下的 uploads 文件夹
     */
    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    /**
     * 文件访问的基础 URL 前缀，默认 /uploads
     */
    @Value("${file.access.prefix:/uploads}")
    private String accessPrefix;

    /**
     * 允许上传的图片扩展名
     */
    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp", "bmp"};

    /**
     * 允许上传的通用文件扩展名
     */
    private static final String[] FILE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp", "bmp", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "zip", "rar", "7z", "mp4", "mp3"};

    /**
     * 通用文件上传接口
     *
     * @param file 上传的文件
     * @param type 文件类型：image 图片、file 通用文件，默认 file
     * @return 文件访问 URL 和文件名
     */
    @Operation(summary = "通用文件上传")
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "file") String type) {

        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 校验文件大小，图片最大 10MB，其他文件最大 50MB
        long maxSize = "image".equals(type) ? 10 * 1024 * 1024 : 50 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            return Result.error("文件大小超过限制");
        }

        // 获取原始文件名和扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.error("文件名格式不正确");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        // 校验扩展名
        if ("image".equals(type)) {
            if (!contains(IMAGE_EXTENSIONS, extension)) {
                return Result.error("仅支持 jpg、jpeg、png、gif、webp、bmp 格式的图片");
            }
        } else {
            if (!contains(FILE_EXTENSIONS, extension)) {
                return Result.error("不支持的文件格式");
            }
        }

        // 按日期创建子目录，避免单个目录文件过多
        String dateDir = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        Path targetDir = Paths.get(uploadPath, type, dateDir).toAbsolutePath().normalize();
        File dir = targetDir.toFile();
        if (!dir.exists() && !dir.mkdirs()) {
            return Result.error("创建上传目录失败");
        }

        // 生成唯一文件名
        String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        File targetFile = new File(dir, newFilename);

        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            return Result.error("文件保存失败");
        }

        // 构建访问 URL：/uploads/{type}/{dateDir}/{newFilename}
        String accessUrl = accessPrefix + "/" + type + "/" + dateDir + "/" + newFilename;

        Map<String, String> result = new HashMap<>();
        result.put("url", accessUrl);
        result.put("filename", originalFilename);
        result.put("size", String.valueOf(file.getSize()));
        return Result.success(result);
    }

    /**
     * 判断数组中是否包含指定字符串
     */
    private boolean contains(String[] array, String value) {
        for (String item : array) {
            if (item.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
