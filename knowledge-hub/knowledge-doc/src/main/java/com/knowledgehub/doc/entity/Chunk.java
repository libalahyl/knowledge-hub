package com.knowledgehub.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档切片实体
 */
@Data
@TableName("knowledge_chunk")
public class Chunk {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文档ID */
    private Long docId;

    /** 切片序号 */
    private Integer chunkIndex;

    /** 切片内容 */
    private String content;

    /** 创建时间 */
    private LocalDateTime createTime;
}
