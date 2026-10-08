package com.knowledgehub.doc.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建/编辑文件夹参数
 */
@Data
public class CategoryCreateDTO {

    /** 文件夹名 */
    @NotBlank(message = "文件夹名不能为空")
    private String name;

    /** 是否公开：1公开 0私有 */
    private Integer isPublic;
}
