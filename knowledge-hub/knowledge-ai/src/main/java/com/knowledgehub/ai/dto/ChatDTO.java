package com.knowledgehub.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 全局问答参数
 */
@Data
public class ChatDTO {

    /** 用户消息 */
    @NotBlank(message = "消息不能为空")
    private String message;

    /** 会话ID（可空，空则新建会话） */
    private Long conversationId;
}
