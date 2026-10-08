package com.knowledgehub.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI 模型配置实体
 */
@Data
@TableName("ai_model_config")
public class ModelConfig {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模型名（如 qwen2.5:7b） */
    private String modelName;

    /** Ollama 地址 */
    private String baseUrl;

    /** 温度 */
    private BigDecimal temperature;

    /** 是否默认：1是 0否 */
    private Integer isDefault;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
