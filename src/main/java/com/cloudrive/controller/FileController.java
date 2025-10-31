package com.cloudrive.controller;

import com.cloudrive.common.Result;
import com.cloudrive.model.dto.FileRenameDTO;
import com.cloudrive.model.vo.FileListVO;
import com.cloudrive.service.FileService;
import com.cloudrive.service.UploadProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author cd
 * @date 2025/10/10
 * @description 文件管理
 */

@RestController
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

    /**
     * 下载文件
     */
    @GetMapping("/{fileId}/content")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String fileId) {
        byte[] content = fileService.downloadFile(fileId);
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(fileService.getFilename(fileId), StandardCharsets.UTF_8)
                .build());
        headers.setContentLength(content.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }

    /**
     * 重命名文件
     */
    @PatchMapping("/{fileId}/name")
    public Result<Void> renameFile(@PathVariable String fileId, @Valid @RequestBody FileRenameDTO dto) {
        fileService.renameFile(fileId, dto.getNewFilename());
        return Result.success();
    }

}
