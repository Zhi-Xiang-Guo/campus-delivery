package com.sky.utils;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LocalFileUploadUtil {

    private LocalFileUploadUtil() {
    }

    public static String upload(MultipartFile file, String uploadPath, String requestHeader) {
        if (file == null || file.isEmpty()) {
            log.warn("上传文件为空");
            throw new BaseException(MessageConstant.UPLOAD_FAILED);
        }

        try {
            File dir = new File(uploadPath);
            if (!dir.exists() && !dir.mkdirs()) {
                log.error("创建图片上传目录失败: {}", uploadPath);
                throw new BaseException(MessageConstant.UPLOAD_FAILED);
            }

            String objectName = UUID.randomUUID() + resolveExtension(file);
            file.transferTo(new File(dir, objectName));

            String baseUrl = requestHeader.endsWith("/") ? requestHeader : requestHeader + "/";
            return baseUrl + objectName;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BaseException(MessageConstant.UPLOAD_FAILED);
        }
    }

    private static String resolveExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.hasText(originalFilename)) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
                return originalFilename.substring(dotIndex).toLowerCase();
            }
        }

        String contentType = file.getContentType();
        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            return ".jpeg";
        }
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return ".webp";
        }
        if ("image/gif".equalsIgnoreCase(contentType)) {
            return ".gif";
        }

        return ".jpeg";
    }
}
