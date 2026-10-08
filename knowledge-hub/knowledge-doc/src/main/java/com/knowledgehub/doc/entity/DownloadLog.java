package com.knowledgehub.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 下载记录实体
 */
@Data
@TableName("knowledge_download_log")
public class DownloadLog {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文档ID */
    private Long docId;

    /** 用户ID */
    private Long userId;

    /** 用户名（冗余） */
    private String userName;

    /** 下载IP */
    private String ip;

    /** 创建时间 */
    private LocalDateTime createTime;
}
