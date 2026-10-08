package com.knowledgehub.doc.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知列表 VO
 */
@Data
public class NotificationVO {

    private Long id;

    /** 类型：DOC_COMMENT/ANNOTATION_REPLY */
    private String type;

    /** 相关文档ID */
    private Long docId;

    /** 文档标题快照 */
    private String docTitle;

    /** 相关评论ID */
    private Long annotationId;

    /** 触发人ID */
    private Long fromUserId;

    /** 触发人昵称快照 */
    private String fromUserName;

    /** 通知摘要 */
    private String content;

    /** 0未读 1已读 */
    private Integer isRead;

    private LocalDateTime createTime;
}
