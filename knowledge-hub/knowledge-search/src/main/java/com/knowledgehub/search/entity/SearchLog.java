package com.knowledgehub.search.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索历史实体
 */
@Data
@TableName("knowledge_search_log")
public class SearchLog {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 搜索关键词 */
    private String keyword;

    /** 用户ID（可空，游客为空） */
    private Long userId;

    /** 搜索时间 */
    private LocalDateTime createTime;
}
