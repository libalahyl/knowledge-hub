package com.knowledgehub.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论点赞实体
 */
@Data
@TableName("knowledge_annotation_like")
public class AnnotationLike {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评论ID */
    private Long annotationId;

    /** 点赞人ID */
    private Long userId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
