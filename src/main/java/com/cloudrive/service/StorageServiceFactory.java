package com.cloudrive.service;

import com.cloudrive.service.impl.MinioStorageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */
@Component
public class StorageServiceFactory {

    private final MinioStorageServiceImpl minioStorageService;

    @Autowired
    public StorageServiceFactory(MinioStorageServiceImpl minioStorageService) {
        this.minioStorageService = minioStorageService;
    }

    public StorageService getStorageService() {
        return minioStorageService;
    }
}
