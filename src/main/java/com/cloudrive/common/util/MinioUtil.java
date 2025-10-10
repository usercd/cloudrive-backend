package com.cloudrive.common.util;

import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */
@Component
public class MinioUtil {
    private static final Logger logger = LoggerFactory.getLogger(MinioUtil.class);

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key-id}")
    private String accessKey;

    @Value("${minio.access-key-secret}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    @Getter
    private String bucketName;

    private MinioClient minioClient;

    /**
     * 初始化Minio客户端
     */
    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // 检查并创建存储桶
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                logger.info("Bucket {} created successfully", bucketName);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize Minio client: {}", e.getMessage());
            throw new RuntimeException("Minio initialization failed", e);
        }
    }


    /**
     * 上传文件
     *
     * @param objectName  文件对象名
     * @param inputStream 文件输入流
     * @param contentType 文件类型
     */
    public void uploadFile(String objectName, InputStream inputStream, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, inputStream.available(), -1)
                            .contentType(contentType)
                            .build()
            );
            logger.info("File {} uploaded successfully", objectName);
        } catch (Exception e) {
            logger.error("Failed to upload file {}: {}", objectName, e.getMessage());
            throw new RuntimeException("File upload failed", e);
        }
    }

    /**
     * 上传文件并支持进度监听
     *
     * @param objectName       文件对象名
     * @param inputStream      文件输入流
     * @param contentType      文件类型
     * @param fileSize         文件大小（字节，用于计算百分比）
     * @param progressListener 进度回调（接收 0-100%）
     */
    public void uploadFileWithProgress(String objectName, InputStream inputStream, String contentType, long fileSize, Consumer<Double> progressListener) {
        try {
            // 包装为进度流
            ProgressInputStream progressStream = new ProgressInputStream(inputStream, fileSize, progressListener);
            // -1 表示自动分片
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(progressStream, fileSize, -1)
                    .contentType(contentType).build());
            logger.info("File {} uploaded successfully (progress: 100%)", objectName);
        } catch (Exception e) {
            logger.error("Failed to upload {}: {}", objectName, e.getMessage(), e);
            throw new RuntimeException("Upload failed", e);
        }
    }


    /**
     * 上传字节数组
     *
     * @param objectName  文件对象名
     * @param bytes       文件字节数组
     * @param contentType 文件类型
     * @return 文件URL
     */
    /*public String uploadBytes(String objectName, byte[] bytes, String contentType) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            return uploadFile(objectName, bais, contentType);
        } catch (IOException e) {
            logger.error("Failed to upload bytes for {}: {}", objectName, e.getMessage());
            throw new RuntimeException("Bytes upload failed", e);
        }
    }*/

    /**
     * 下载文件
     *
     * @param objectName 文件对象名
     * @return 文件输入流
     */
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Failed to download file {}: {}", objectName, e.getMessage());
            throw new RuntimeException("File download failed", e);
        }
    }

    /**
     * 删除文件
     *
     * @param objectName 文件对象名
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            logger.info("File {} deleted successfully", objectName);
        } catch (Exception e) {
            logger.error("Failed to delete file {}: {}", objectName, e.getMessage());
            throw new RuntimeException("File deletion failed", e);
        }
    }

    /**
     * 获取文件URL（预签名URL，有效期7天）
     *
     * @param objectName 文件对象名
     * @return 文件URL
     */
    public String getFileUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            logger.error("Failed to get URL for {}: {}", objectName, e.getMessage());
            throw new RuntimeException("Get file URL failed", e);
        }
    }


    private static class ProgressInputStream extends FilterInputStream {
        private final long totalSize;
        private final Consumer<Double> progressListener;
        private long readBytes = 0;
        private double lastNotifiedProgress = -1.0; // 上次通知的进度，避免重复通知
        private static final double PROGRESS_THRESHOLD = 1.0; // 进度变化阈值（1%）

        protected ProgressInputStream(InputStream in, long totalSize, Consumer<Double> progressListener) {
            super(in);
            this.totalSize = totalSize;
            this.progressListener = progressListener;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b != -1) {
                readBytes++;
                notifyProgress();
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int bytesRead = super.read(b, off, len);
            if (bytesRead > 0) {
                readBytes += bytesRead;
                notifyProgress();
            }
            return bytesRead;
        }

        private void notifyProgress() {
            if (totalSize > 0 && progressListener != null) {
                double progress = Math.min((readBytes * 100.0) / totalSize, 100.0);

                // 只有当进度变化超过阈值或达到100%时才通知
                if (progress >= 100.0 ||
                        lastNotifiedProgress < 0 ||
                        (progress - lastNotifiedProgress) >= PROGRESS_THRESHOLD) {

                    progressListener.accept(progress);
                    lastNotifiedProgress = progress;
                }
            }
        }
    }
}
