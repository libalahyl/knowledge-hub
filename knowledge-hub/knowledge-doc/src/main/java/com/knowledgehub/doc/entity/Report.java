package com.knowledgehub.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报实体
 */
@Data
@TableName("knowledge_report")
public class Report {

    /** 主键 */
    @TableId(type = IdType.AUTO)
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

    /** 状态：PENDING待处理/HANDLED已处理/IGNORED已忽略 */
    private String status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理备注 */
    private String handleRemark;

    /** 处理时间 */
    private LocalDateTime handleTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
