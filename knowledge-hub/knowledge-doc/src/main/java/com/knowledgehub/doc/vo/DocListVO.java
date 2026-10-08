package com.knowledgehub.doc.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档列表 VO
 */
@Data
public class DocListVO {

    private Long id;

    private String title;

    private String summary;

    /** 分类名 */
    private String categoryName;

    /** 上传者昵称 */
    private String uploaderName;

    private Integer viewCount;

    private Integer favoriteCount;

    private LocalDateTime createTime;
}
