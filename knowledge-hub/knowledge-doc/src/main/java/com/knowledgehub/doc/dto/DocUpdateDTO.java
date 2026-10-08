package com.knowledgehub.doc.dto;

import lombok.Data;

/**
 * 文档更新参数
 */
@Data
public class DocUpdateDTO {

    /** 标题 */
    private String title;

    /** 摘要 */
    private String summary;

    /** 分类 */
    private Long categoryId;

    /** 标签 */
    private String tags;
}
