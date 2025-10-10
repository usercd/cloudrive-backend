package com.cloudrive.controller;

import com.cloudrive.common.Result;
import com.cloudrive.model.vo.FileListVO;
import com.cloudrive.service.FileService;
import com.cloudrive.service.UploadProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author cd
 * @date 2025/10/10
 * @description 文件管理
 */

@RestController
@Validated
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;
    private final UploadProgressService uploadProgressService;

    /**
     * 上传文件
     */
    @PostMapping
    public Result<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "parentId", required = false) String parentId) {
        String filePath = fileService.uploadFile(file, parentId);
        return Result.success(filePath);
    }

    /**
     * 获取文件列表
     */
    @GetMapping
    public Result<List<FileListVO>> listFiles(@RequestParam(value = "parentId", required = false) String parentId) {
        List<FileListVO> fileListVOs = fileService.listFiles(parentId);
        return Result.success(fileListVOs);
    }

}
