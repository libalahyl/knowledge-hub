package com.knowledgehub.doc.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报列表 VO（含目标详情，供管理员端展示）
 */
@Data
public class ReportVO {

    private Long id;

    /** 被举报文档ID */
    private Long docId;

    /** 被举报评论ID（NULL=文档举报） */
    private Long commentId;

    /** 举报人ID */
    private Long reporterId;

    /** 举报人昵称（冗余） */
    private String reporterName;

    /** 举报原因 */
    private String reason;

    /** 状态：PENDING/HANDLED/IGNORED */
    private String status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理备注 */
    private String handleRemark;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 目标类型：DOC/COMMENT */
    private String targetType;

    /** 被举报文档标题 */
    private String docTitle;

    /** 被举报评论内容（评论举报时有值） */
    private String commentContent;

    /** 被举报评论作者昵称（评论举报时有值） */
    private String commentUserName;
}
