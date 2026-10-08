package com.knowledgehub.doc.dto;

import lombok.Data;

/**
 * 文档分页查询参数
 */
@Data
public class DocQueryDTO {

    /** 页码 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer size = 10;

    /** 标题模糊查询（可空） */
    private String title;

    /** 分类筛选（可空） */
    private Long categoryId;

    /** 上传者筛选（可空） */
    private Long uploaderId;
}
