package com.knowledgehub.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文档问答参数
 */
@Data
public class ChatWithDocDTO {

    /** 文档ID */
    @NotNull(message = "文档ID不能为空")
    private Long docId;

    /** 用户消息 */
    @NotBlank(message = "消息不能为空")
    private String message;

    /** 会话ID（可空，空则新建会话） */
    private Long conversationId;
}
