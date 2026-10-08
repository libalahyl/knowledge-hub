package com.knowledgehub.ai.service;

import com.knowledgehub.ai.vo.ChatResponseVO;
import reactor.core.publisher.Flux;

/**
 * AI 对话服务接口
 */
public interface ChatService {

    /**
     * 全局问答（非流式）
     */
    ChatResponseVO chat(String message, Long userId, Long conversationId);

    /**
     * 文档问答（非流式）
     */
    ChatResponseVO chatWithDoc(Long docId, String message, Long userId, Long conversationId);

    /**
     * 全局流式问答
     *
     * @return Flux<String>，每个元素是一个 token
     */
    Flux<String> chatStream(String message, Long userId, Long conversationId);

    /**
     * 文档流式问答
     *
     * @return Flux<String>，每个元素是一个 token
     */
    Flux<String> chatWithDocStream(Long docId, String message, Long userId, Long conversationId);

    /**
     * 只创建会话（不调 AI），用于流式接口提前拿 conversationId
     */
    Long createConversationOnly(String message, Long userId, Long docId);
}
