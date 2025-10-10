package com.cloudrive.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */
public interface FileService {
    /**
     * 上传文件
     */
    String uploadFile(MultipartFile file, String parentId);
}
