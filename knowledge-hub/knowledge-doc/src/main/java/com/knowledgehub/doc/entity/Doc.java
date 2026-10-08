package com.knowledgehub.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识文档实体
 */
@Data
@TableName("knowledge_doc")
public class Doc {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题 */
    private String title;

    /** 摘要 */
    private String summary;

    /** 原文内容（Markdown） */
    private String content;

    /** 分类ID */
    private Long categoryId;

    /** 标签（逗号分隔，冗余展示） */
    private String tags;

    /** 上传者ID */
    private Long uploaderId;

    /** 上传者昵称（冗余） */
    private String uploaderName;

    /** 来源：PRESET预置/UPLOAD用户上传 */
    private String sourceType;

    /** 文件存储路径 */
    private String filePath;

    /** 文件大小（字节） */
    private Long fileSize;

    /** 审核状态：PENDING/APPROVED/REJECTED */
    private String auditStatus;

    /** 状态：1正常 0已删除 */
    private Integer status;

    /** 浏览量 */
    private Integer viewCount;

    /** 收藏数 */
    private Integer favoriteCount;

    /** 是否公开：1公开 0私有 */
    private Integer isPublic;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
