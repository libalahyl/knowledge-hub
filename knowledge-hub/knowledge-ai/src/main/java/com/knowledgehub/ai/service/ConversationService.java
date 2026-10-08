package com.knowledgehub.ai.service;

import com.knowledgehub.ai.entity.Conversation;
import com.knowledgehub.ai.entity.Message;

import java.util.List;

/**
 * AI 会话服务接口
 */
public interface ConversationService {

    /**
     * 新建会话
     */
    Long createConversation(Long userId, Long docId, String title);

    /**
     * 保存一条消息
     */
    void saveMessage(Long conversationId, String role, String content);

    /**
     * 根据 ID 查询会话
     */
    Conversation getById(Long id);

    /**
     * 查用户的会话列表
     */
    List<Conversation> listByUser(Long userId);

    /**
     * 查某会话的所有消息
     */
    List<Message> listMessages(Long conversationId);

    /**
     * 删除会话（物理删除 + 级联删消息）
     */
    void deleteConversation(Long id, Long userId);
}
