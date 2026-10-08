package com.knowledgehub.doc.dto;

import lombok.Data;

/**
 * 分类创建/更新参数
 */
@Data
public class CategoryDTO {

    /** 分类名 */
    private String name;

    /** 排序 */
    private Integer sort;
}
