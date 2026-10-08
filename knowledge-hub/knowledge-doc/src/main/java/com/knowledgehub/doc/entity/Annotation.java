package com.knowledgehub.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档注释实体
 */
@Data
@TableName("knowledge_annotation")
public class Annotation {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文档ID */
    private Long docId;

    /** 注释作者ID */
    private Long userId;

    /** 作者昵称（冗余） */
    private String userName;

    /** 被回复人昵称（冗余快照，@某人用） */
    private String replyToUserName;

    /** 注释内容 */
    private String content;

    /** 类型：SUPPLEMENT补充/CORRECTION纠错/QUESTION提问 */
    private String type;

    /** 父评论ID（NULL=顶层评论） */
    private Long parentId;

    /** 状态：1正常 0已删除 */
    private Integer status;

    /** 点赞数 */
    private Integer likeCount;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
