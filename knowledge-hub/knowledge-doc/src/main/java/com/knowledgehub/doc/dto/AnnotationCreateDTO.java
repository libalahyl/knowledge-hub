package com.knowledgehub.doc.dto;

import lombok.Data;

/**
 * 添加注释参数
 */
@Data
public class AnnotationCreateDTO {

    /** 文档ID */
    private Long docId;

    /** 注释内容 */
    private String content;

    /** 类型：SUPPLEMENT/CORRECTION/QUESTION */
    private String type;

    /** 被回复的评论ID（空=顶层评论；回复"回复"时后端归一化到顶层） */
    private Long replyToId;
}
