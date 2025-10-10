package com.cloudrive.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */
@Data
public class FileInfo {

    private String fileInfoId;

    private String filename;

    private String originalFilename;

    private String path;

    private Long fileSize;

    private String fileType;

    private String sha256Hash;

    private User user;

    private String parentId;

    private Boolean isFolder = false;

    private Boolean isDeleted = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
