package com.knowledgehub.ai.vo;

import lombok.Data;

/**
 * AI 对话响应
 */
@Data
public class ChatResponseVO {

    /** 会话ID（本阶段透传，下一阶段返回真实 ID） */
    private Long conversationId;

    /** AI 回答内容 */
    private String answer;
}
