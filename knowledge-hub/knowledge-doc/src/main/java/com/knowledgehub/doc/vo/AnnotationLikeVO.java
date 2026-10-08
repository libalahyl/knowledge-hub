package com.knowledgehub.doc.vo;

import lombok.Data;

/**
 * 点赞结果 VO
 */
@Data
public class AnnotationLikeVO {

    /** 当前用户是否已点赞 */
    private Boolean liked;

    /** 最新点赞数 */
    private Integer likeCount;
}
