package com.cloudrive.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */
@Data
public class FileListVO {
    private String fileInfoId;
    private String filename;
    private String originalFilename;
    private String path;
    private Long fileSize;
    private String fileType;
    private String parentId;
    private Boolean isFolder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
