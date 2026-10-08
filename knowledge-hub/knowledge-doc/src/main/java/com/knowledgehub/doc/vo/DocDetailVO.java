package com.knowledgehub.doc.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档详情 VO
 */
@Data
public class DocDetailVO {

    private Long id;

    private String title;

    private String summary;

    private String content;

    private Long categoryId;

    /** 分类名 */
    private String categoryName;

    private String tags;

    private Long uploaderId;

    private String uploaderName;

    private String sourceType;

    private String filePath;

    private Long fileSize;

    private Integer viewCount;

    private Integer favoriteCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 当前用户是否已收藏 */
    private Boolean favorited;

    /** 注释列表（本阶段先不填，留 null） */
    private List<AnnotationVO> annotations;
}
