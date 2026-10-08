package com.knowledgehub.doc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档分类实体
 */
@Data
@TableName("knowledge_category")
public class Category {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名 */
    private String name;

    /** 排序 */
    private Integer sort;

    /** 创建者ID（null 表示系统预置分类） */
    private Long ownerId;

    /** 是否公开：1公开 0私有 */
    private Integer isPublic;

    /** 父分类ID（预留多级，暂不用） */
    private Long parentId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
