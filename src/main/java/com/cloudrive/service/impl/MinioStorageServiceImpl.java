package com.cloudrive.service.impl;

import com.cloudrive.common.constant.CommonConstants;
import com.cloudrive.common.enums.ErrorCode;
import com.cloudrive.common.util.ExceptionUtil;
import com.cloudrive.common.util.MinioUtil;
import com.cloudrive.service.StorageService;
import com.cloudrive.service.UploadProgressService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */
@Service
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(MinioStorageServiceImpl.class);
    private final UploadProgressService uploadProgressService;
    private final MinioUtil minioUtil;

    @Override
    public String uploadFile(MultipartFile file, String path) {
        String fileName = generateUniqueFileName();
        String objectName = buildObjectName(path, fileName);
        try {
            minioUtil.uploadFile(objectName, file.getInputStream(), file.getContentType());
            return objectName;
        } catch (Exception e) {
            logger.error("Failed to upload file to MinIO: objectName={}, error={}",  objectName, e.getMessage());
            ExceptionUtil.throwBizException(ErrorCode.FILE_UPLOAD_FAILED, e.getMessage());
            return null;
        }
    }

    private String generateUniqueFileName() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String buildObjectName(String path, String fileName) {
        return path.endsWith(CommonConstants.File.SLASH) ? path + fileName : path + CommonConstants.File.SLASH + fileName;
    }

    @Override
    public String uploadFileWithProgressFromPath(File file, String path, String taskId, String originalFilename, long fileSize) {
        // 参数验证
        if (file == null || !file.exists() || !file.isFile()) {
            handleUploadError(taskId, "文件不存在或不是常规文件: " + (file != null ? file.getAbsolutePath() : "null"));
            return null;
        }

        if (fileSize <= 0) {
            fileSize = file.length(); // 如果传入的文件大小不正确，使用实际文件大小
        }

        // 生成唯一文件名和对象路径
        String fileName = generateUniqueFileName();
        String objectName = buildObjectName(path, fileName);

        logger.info("Starting MinIO upload with progress tracking: file={}, objectName={}, taskId={}, fileSize={}",
                originalFilename, objectName, taskId, fileSize);

        try {
            // 初始化进度任务
            uploadProgressService.updateProgress(taskId, 0.0,0, fileSize);

            final long finalFileSize = fileSize; // final变量用于lambda表达式

            // 上传文件并监听进度
            try (InputStream is = new FileInputStream(file)) {
                minioUtil.uploadFileWithProgress(
                        objectName,
                        is,
                        determineContentType(originalFilename),
                        finalFileSize,
                        (Double progress) -> {
                            // 更新进度百分比
                            logger.info("Upload progress: {}%, taskId: {}", String.format("%.2f", progress), taskId);

                            // 更新任务进度
                            long bytesTransferred = Math.round(finalFileSize * progress / 100.0);
                            uploadProgressService.updateProgress(taskId, progress, bytesTransferred, finalFileSize);
                        }
                );

                // 标记上传完成
                uploadProgressService.completeUploadTask(taskId, true, "上传完成");
                logger.info("File uploaded successfully to MinIO: objectName={}, taskId={}", objectName, taskId);

                return objectName;

            } catch (IOException e) {
                logger.error("Failed to upload file to MinIO with progress tracking: objectName={}, error={}",
                        objectName, e.getMessage(), e);
                uploadProgressService.completeUploadTask(taskId, false, "MinIO上传失败: " + e.getMessage());
                ExceptionUtil.throwBizException(ErrorCode.FILE_UPLOAD_FAILED, "MinIO上传失败: " + e.getMessage());
                return null;
            }

        } catch (Exception e) {
            handleUploadError(taskId, "文件上传失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 根据文件名确定内容类型
     */
    private String determineContentType(String filename) {
        if (filename == null) {
            return "application/octet-stream";
        }

        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".zip")) {
            return "application/zip";
        } else if (lowerFilename.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFilename.endsWith(".png")) {
            return "image/png";
        } else if (lowerFilename.endsWith(".txt")) {
            return "text/plain";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * 处理上传错误
     */
    private void handleUploadError(String taskId, String errorMessage) {
        logger.error(errorMessage);
        uploadProgressService.completeUploadTask(taskId, false, errorMessage);
        ExceptionUtil.throwBizException(ErrorCode.FILE_UPLOAD_FAILED, errorMessage);
    }


    @Override
    public void deleteFile(String path) {
        try {
            minioUtil.deleteFile(path);
            logger.info("File deleted successfully from MinIO: path={}", path);
        } catch (Exception e) {
            logger.error("Failed to delete file from MinIO: path={}, error={}", path, e.getMessage());
            ExceptionUtil.throwBizException(ErrorCode.FILE_DELETE_FAILED, e.getMessage());
        }
    }

    @Override
    public byte[] downloadFile(String path) {
        try (InputStream inputStream = minioUtil.downloadFile(path)) {
            if (inputStream == null) {
                logger.error("File not found in MinIO: path={}", path);
                ExceptionUtil.throwBizException(ErrorCode.FILE_NOT_FOUND);
                return null;
            }

            byte[] bytes = inputStream.readAllBytes();
            logger.info("File downloaded successfully from MinIO: path={}, size={} bytes", path, bytes.length);
            return bytes;
        } catch (IOException e) {
            logger.error("Failed to download file from MinIO: path={}, error={}", path, e.getMessage());
            ExceptionUtil.throwBizException(ErrorCode.FILE_DOWNLOAD_FAILED, e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Failed to download file, error: {}", e.getMessage());
            ExceptionUtil.throwBizException(ErrorCode.FILE_DOWNLOAD_FAILED, e.getMessage());
            return null;
        }
    }
}
