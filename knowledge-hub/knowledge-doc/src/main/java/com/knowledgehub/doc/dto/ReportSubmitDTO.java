package com.knowledgehub.doc.dto;

import lombok.Data;

/**
 * 提交举报参数
 */
@Data
public class ReportSubmitDTO {

    /** 被举报文档ID（评论举报时可为空，后端从评论反查） */
    private Long docId;

    /** 被举报评论ID（空=文档举报） */
    private Long commentId;

    /** 举报原因 */
    private String reason;
}
