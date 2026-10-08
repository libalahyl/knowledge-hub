package com.knowledgehub.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 文档-标签关联实体
 */
@Data
@TableName("knowledge_doc_tag")
public class DocTag {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文档ID */
    private Long docId;

    /** 标签ID */
    private Long tagId;
}
