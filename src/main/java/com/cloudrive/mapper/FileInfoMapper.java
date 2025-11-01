package com.cloudrive.mapper;

import com.cloudrive.model.entity.FileInfo;
import com.cloudrive.model.vo.FileListVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author CD
 * @date 11/1/2025
 * @description
 */
@Mapper(componentModel = "spring")
public interface FileInfoMapper {

    @Mapping(source = "fileInfoId", target = "fileInfoId")
    @Mapping(source = "filename", target = "filename")
    @Mapping(source = "originalFilename", target = "originalFilename")
    @Mapping(source = "path", target = "path")
    @Mapping(source = "fileSize", target = "fileSize")
    @Mapping(source = "fileType", target = "fileType")
    @Mapping(source = "parentId", target = "parentId")
    @Mapping(source = "isFolder", target = "isFolder")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    FileListVO fileInfoToFileListVO(FileInfo fileInfo);


}
