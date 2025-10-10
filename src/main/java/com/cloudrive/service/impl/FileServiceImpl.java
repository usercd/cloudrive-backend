package com.cloudrive.service.impl;

import com.cloudrive.common.constant.CommonConstants;
import com.cloudrive.common.util.FileHashUtil;
import com.cloudrive.common.util.GenerateID;
import com.cloudrive.common.util.UserContext;
import com.cloudrive.mapper.FileInfoMapper;
import com.cloudrive.mapper.UserMapper;
import com.cloudrive.model.entity.FileInfo;
import com.cloudrive.model.entity.User;
import com.cloudrive.model.vo.FileListVO;
import com.cloudrive.service.FileService;
import com.cloudrive.service.StorageService;
import com.cloudrive.service.StorageServiceFactory;
import com.cloudrive.service.UploadProgressService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    private final StorageServiceFactory storageServiceFactory;
    private final FileInfoMapper fileInfoMapper;
    private final UploadProgressService uploadProgressService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public String uploadFile(MultipartFile file, String parentId) {
        User currentUser = UserContext.getCurrentUser();

        // 1. 计算文件的SHA-256哈希值
        String sha256Hash = FileHashUtil.calculateSHA256(file);

        // 2. 检查是否存在相同哈希值的文件（秒传逻辑）
        if (sha256Hash != null && !sha256Hash.isEmpty()) {
            // 查找当前用户是否已经上传过相同哈希值的文件
            List<FileInfo> existingFiles = fileInfoMapper.findBySha256HashAndUserId(sha256Hash, currentUser.getUserId());

            if (!existingFiles.isEmpty()) {
                // 找到了相同哈希值的文件，实现秒传
                FileInfo existingFile = existingFiles.get(0);
                // 使用通用的秒传处理方法，传入null表示不需要进度跟踪
                FileInfo newFileInfo = handleFastUpload(file.getOriginalFilename(), file.getSize(), existingFile, sha256Hash, parentId, null, currentUser);
                return newFileInfo.getPath();
            }
        }

        // 3. 如果没有找到相同哈希值的文件，执行正常上传流程
        StorageService storageService = storageServiceFactory.getStorageService();
        String path = getUploadPath(parentId, currentUser);

        // 上传文件
        String filePath = storageService.uploadFile(file, path);
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileInfoId(GenerateID.generateFileInfoRandomId(13));
        fileInfo.setFilename(file.getOriginalFilename());
        fileInfo.setOriginalFilename(file.getOriginalFilename());
        fileInfo.setPath(filePath);
        fileInfo.setFileSize(file.getSize());
        fileInfo.setFileType(file.getContentType());
        fileInfo.setUser(currentUser);
        fileInfo.setParentId(parentId);
        fileInfo.setIsFolder(false);
        fileInfo.setIsDeleted(false);
        fileInfo.setCreatedAt(LocalDateTime.now());
        fileInfo.setUpdatedAt(LocalDateTime.now());
        fileInfo.setSha256Hash(sha256Hash);
        fileInfoMapper.insertFileInfo(fileInfo);
        return filePath;
    }

    /**
     * 获取上传路径
     */
    private String getUploadPath(String parentId, User currentUser) {
        String path = CommonConstants.File.FILE_PATH_PREFIX + currentUser.getUserId();
        if (parentId != null) {
            FileInfo parent = fileInfoMapper.findFileInfoByParentId(parentId);
            path = parent.getPath();
        }
        return path;
    }

    /**
     * 处理秒传逻辑，可用于普通上传和带进度上传
     *
     * @param filename 文件名
     * @param fileSize 文件大小
     * @param existingFile 已存在的文件
     * @param sha256Hash 文件哈希值
     * @param parentId 父目录ID
     * @param taskId 上传任务ID，如果为null则不进行进度跟踪
     * @param currentUser 当前用户
     * @return 新创建的文件信息对象
     */
    private FileInfo handleFastUpload(String filename, long fileSize, FileInfo existingFile, String sha256Hash, String parentId, String taskId, User currentUser) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileInfoId(GenerateID.generateFileInfoRandomId(13));
        fileInfo.setFilename(filename);
        fileInfo.setOriginalFilename(filename);
        fileInfo.setPath(existingFile.getPath());
        fileInfo.setFileSize(fileSize);
        fileInfo.setFileType(existingFile.getFileType());
        fileInfo.setUser(currentUser);
        fileInfo.setParentId(parentId);
        fileInfo.setIsFolder(false);
        fileInfo.setIsDeleted(false);
        fileInfo.setCreatedAt(LocalDateTime.now());
        fileInfo.setUpdatedAt(LocalDateTime.now());
        fileInfo.setSha256Hash(sha256Hash);
        // 如果有任务ID，则进行进度跟踪
        if (taskId != null) {
            // 模拟上传进度（秒传情况下直接完成）
            uploadProgressService.updateProgress(taskId, 100.0,fileSize, fileSize);
            uploadProgressService.completeUploadTask(taskId, true, "文件秒传成功");
        }

        // 保存新的文件记录
        fileInfoMapper.insertFileInfo(fileInfo);
        return fileInfo;
    }

    @Override
    public List<FileListVO> listFiles(String parentId) {
        String userId = UserContext.getCurrentUserId();
        //List<FileInfo> fileInfos = fileInfoRepository.findByUser_UserIdAndParentIdAndIsDeletedFalse(userId, parentId);
        List<FileInfo> fileInfos = fileInfoMapper.findFileInfoByUserIdAndParentId(userId, parentId);

        return fileInfos.stream().map(this::convertToFileListVO).collect(Collectors.toList());
    }

    private FileListVO convertToFileListVO(FileInfo fileInfo) {
        FileListVO vo = new FileListVO();
        vo.setFileInfoId(fileInfo.getFileInfoId());
        vo.setFilename(fileInfo.getFilename());
        vo.setOriginalFilename(fileInfo.getOriginalFilename());
        vo.setPath(fileInfo.getPath());
        vo.setParentId(fileInfo.getParentId());
        vo.setFileSize(fileInfo.getFileSize());
        vo.setFileType(fileInfo.getFileType());
        vo.setCreatedAt(fileInfo.getCreatedAt());
        vo.setUpdatedAt(fileInfo.getUpdatedAt());
        vo.setIsFolder(fileInfo.getIsFolder());
        return vo;
    }
}
