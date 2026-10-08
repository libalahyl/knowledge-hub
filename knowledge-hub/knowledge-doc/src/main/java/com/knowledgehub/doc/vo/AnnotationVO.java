package com.knowledgehub.doc.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档注释 VO
 */
@Data
public class AnnotationVO {

    private Long id;

    private Long docId;

    private Long userId;

    /** 作者昵称 */
    private String userName;

    /** 被回复人昵称（冗余快照，@某人用） */
    private String replyToUserName;

    private String content;

    /** 类型：SUPPLEMENT补充/CORRECTION纠错/QUESTION提问 */
    private String type;

    /** 父评论ID（NULL=顶层评论） */
    private Long parentId;

    private Integer status;

    /** 点赞数 */
    private Integer likeCount;

    /** 当前用户是否已点赞 */
    private Boolean liked;

    private LocalDateTime createTime;

    /** 子回复列表（仅顶层评论有值） */
    private List<AnnotationVO> replies;
}
