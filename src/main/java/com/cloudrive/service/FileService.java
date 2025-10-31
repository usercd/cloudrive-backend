package com.cloudrive.service;

import com.cloudrive.model.vo.FileListVO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    /**
     * 下载文件
     */
    byte[] downloadFile(String fileId);

    /**
     * 获取文件名
     */
    String getFilename(String fileId);

    void renameFile(String fileId, String newFilename);
}
