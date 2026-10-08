package com.knowledgehub.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 站内通知实体
 */
@Data
@TableName("knowledge_notification")
public class Notification {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收人ID */
    private Long userId;

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

    /** 1正常 0已删除 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
