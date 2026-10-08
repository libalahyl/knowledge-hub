package com.knowledgehub.doc.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档上传参数
 */
@Data
public class DocUploadDTO {

    /** .md 文件 */
    private MultipartFile file;

    /** 标题（必填） */
    private String title;

    /** 摘要（可空） */
    private String summary;

    /** 分类（可空） */
    private Long categoryId;

    /** 标签（逗号分隔，可空） */
    private String tags;
}
