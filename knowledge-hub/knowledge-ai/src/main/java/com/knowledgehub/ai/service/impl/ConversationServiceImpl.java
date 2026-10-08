package com.knowledgehub.ai.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knowledgehub.ai.entity.Conversation;
import com.knowledgehub.ai.entity.Message;
import com.knowledgehub.ai.mapper.ConversationMapper;
import com.knowledgehub.ai.mapper.MessageMapper;
import com.knowledgehub.ai.service.ConversationService;
import com.knowledgehub.common.exception.BusinessException;
import com.knowledgehub.common.result.ResultCode;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI 会话服务实现
 */
@Service
public class ConversationServiceImpl implements ConversationService {

    @Resource
    private ConversationMapper conversationMapper;

    @Resource
    private MessageMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConversation(Long userId, Long docId, String title) {
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setDocId(docId);
        conversation.setTitle(title == null || title.isBlank() ? "新对话" : title);
        conversationMapper.insert(conversation);
        return conversation.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMessage(Long conversationId, String role, String content) {
        // 校验会话存在，避免插孤儿消息
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException("会话不存在");
        }
        // 校验 role
        if (!"user".equals(role) && !"assistant".equals(role) && !"system".equals(role)) {
            throw new BusinessException("消息角色不合法");
        }
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        messageMapper.insert(message);
    }

    @Override
    public Conversation getById(Long id) {
        return conversationMapper.selectById(id);
    }

    @Override
    public List<Conversation> listByUser(Long userId) {
        return conversationMapper.selectList(Wrappers.<Conversation>lambdaQuery()
                .eq(Conversation::getUserId, userId)
                .orderByDesc(Conversation::getUpdateTime));
    }

    @Override
    public List<Message> listMessages(Long conversationId) {
        return messageMapper.selectList(Wrappers.<Message>lambdaQuery()
                .eq(Message::getConversationId, conversationId)
                .orderByAsc(Message::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long id, Long userId) {
        Conversation conversation = conversationMapper.selectById(id);
        // 会话不存在则静默返回（幂等）
        if (conversation == null) {
            return;
        }
        // 校验归属，防越权
        if (!userId.equals(conversation.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限删除该会话");
        }
        // 级联删除消息 + 删除会话
        messageMapper.delete(Wrappers.<Message>lambdaQuery().eq(Message::getConversationId, id));
        conversationMapper.deleteById(id);
    }
}
