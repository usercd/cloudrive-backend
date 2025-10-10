package com.cloudrive.controller;

import com.cloudrive.common.Result;
import com.cloudrive.service.FileService;
import com.cloudrive.service.UploadProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

}
