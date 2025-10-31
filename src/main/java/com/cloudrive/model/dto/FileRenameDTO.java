package com.cloudrive.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author CD
 * @date 10/31/2025
 * @description
 */
@Data
public class FileRenameDTO {
    @NotBlank(message = "新文件名不能为空")
    @Size(max = 50, message = "新文件名不能超过50个字符")
    private String newFilename;
}
