package com.cloudrive.service;

import com.cloudrive.model.vo.FileListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    /**
     * 获取文件列表
     */
    List<FileListVO> listFiles(String parentId);
}
