package com.cloudrive.mapper;

import com.cloudrive.model.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author cd
 * @date 2025/10/10
 * @description
 */

@Mapper
public interface FileInfoMapper {

    // 插入新的文件信息
    void insertFileInfo(@Param("fileInfo") FileInfo fileInfo);

    // 更新已有的文件信息
    int updateFileInfo(@Param("fileInfo") FileInfo fileInfo);

    // 根据ID查找文件信息（用于判断是插入还是更新）
    FileInfo selectFileInfoByFileId(@Param("fileInfoId") String fileInfoId);

    //List<FileInfo> findByUser_UserIdAndParentIdIsNullAndIsDeletedFalse(String userId);
    //List<FileInfo> findByUser_UserIdAndParentIdAndIsDeletedFalse(String userId, Long parentId);
    //Optional<FileInfo> findByPathAndUser_UserId(String path, String userId);
    //long countByParentIdAndIsDeletedFalse(Long parentId);

    /**
     * 根据SHA-256哈希值和用户ID查找未删除的文件
     */
    List<FileInfo> findBySha256HashAndUserId(@Param("sha256Hash") String sha256Hash, @Param("userId") String userId);


    /**
     * 根据父目录ID查找父目录路径
     * @param parentId
     * @return
     */
    FileInfo findFileInfoByParentId(@Param("parentId") String parentId);


    /**
     * 统计引用同一文件路径的文件数量
     */
    //long countByPathAndIsDeletedFalse(@Param("path") String path);

    /**
     * 根据文件名模糊搜索文件（不区分大小写）
     */
    //List<FileInfo> searchByFilename(@Param("userId") String userId, @Param("keyword") String keyword);
}
