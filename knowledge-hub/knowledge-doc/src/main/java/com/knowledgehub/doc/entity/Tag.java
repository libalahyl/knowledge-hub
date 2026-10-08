package com.knowledgehub.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体
 */
@Data
@TableName("knowledge_tag")
public class Tag {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名 */
    private String name;

    /** 创建时间 */
    private LocalDateTime createTime;
}
