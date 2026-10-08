package com.knowledgehub.ai.service.impl;

import com.knowledgehub.ai.entity.Conversation;
import com.knowledgehub.ai.entity.Message;
import com.knowledgehub.ai.mapper.ConversationMapper;
import com.knowledgehub.ai.service.ChatService;
import com.knowledgehub.ai.service.ConversationService;
import com.knowledgehub.ai.vo.ChatResponseVO;
import com.knowledgehub.common.exception.BusinessException;
import com.knowledgehub.common.result.ResultCode;
import com.knowledgehub.doc.entity.Annotation;
import com.knowledgehub.doc.service.AnnotationService;
import com.knowledgehub.doc.service.DocService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 对话服务实现（支持多轮上下文 + 流式）
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private ChatClient chatClient;

    @Resource
    private DocService docService;

    @Resource
    private AnnotationService annotationService;

    @Resource
    private ConversationService conversationService;

    @Resource
    private ConversationMapper conversationMapper;

    @Override
    public ChatResponseVO chat(String message, Long userId, Long conversationId) {
        // 1. 解析会话（无则新建，有则校验归属）
        conversationId = resolveConversationId(message, userId, conversationId, null);
        // 2. 先取历史消息（在保存当前消息之前，避免重复）
        List<Message> history = conversationService.listMessages(conversationId);
        // 3. 保存当前用户消息
        conversationService.saveMessage(conversationId, "user", message);

        String systemPrompt = "你是软件工程学习助手，擅长 Java、Spring、MyBatis-Plus、数据库。" +
                "请基于你的知识回答用户问题，回答要简洁、准确、有代码示例。";
        // 4. 调 AI（带历史上下文）
        String answer = callAiWithHistory(systemPrompt, history, message);
        // 5. 保存 AI 回答
        conversationService.saveMessage(conversationId, "assistant", answer);

        ChatResponseVO vo = new ChatResponseVO();
        vo.setConversationId(conversationId);
        vo.setAnswer(answer);
        return vo;
    }

    @Override
    public ChatResponseVO chatWithDoc(Long docId, String message, Long userId, Long conversationId) {
        // 先取文档切片并校验（文档为空直接抛，不新建会话、不留脏数据）
        String systemPrompt = buildDocSystemPrompt(docId);

        // 1. 解析会话（无则新建，有则校验归属）
        conversationId = resolveConversationId(message, userId, conversationId, docId);
        // 2. 先取历史消息
        List<Message> history = conversationService.listMessages(conversationId);
        // 3. 保存当前用户消息
        conversationService.saveMessage(conversationId, "user", message);
        // 4. 调 AI（带历史上下文）
        String answer = callAiWithHistory(systemPrompt, history, message);
        // 5. 保存 AI 回答
        conversationService.saveMessage(conversationId, "assistant", answer);

        ChatResponseVO vo = new ChatResponseVO();
        vo.setConversationId(conversationId);
        vo.setAnswer(answer);
        return vo;
    }

    @Override
    public Flux<String> chatStream(String message, Long userId, Long conversationId) {
        Long resolvedConversationId = resolveConversationId(message, userId, conversationId, null);
        List<Message> history = conversationService.listMessages(resolvedConversationId);
        conversationService.saveMessage(resolvedConversationId, "user", message);

        String systemPrompt = "你是软件工程学习助手，擅长 Java、Spring、MyBatis-Plus、数据库。" +
                "请基于你的知识回答用户问题，回答要简洁、准确、有代码示例。";
        List<org.springframework.ai.chat.messages.Message> messages = buildMessages(systemPrompt, history, message);
        Prompt prompt = new Prompt(messages);

        StringBuilder fullAnswer = new StringBuilder();
        return chatClient.prompt(prompt).stream().content()
                .doOnNext(token -> fullAnswer.append(token))
                .doOnComplete(() -> conversationService.saveMessage(resolvedConversationId, "assistant", fullAnswer.toString()));
    }

    @Override
    public Flux<String> chatWithDocStream(Long docId, String message, Long userId, Long conversationId) {
        String systemPrompt = buildDocSystemPrompt(docId);

        Long resolvedConversationId = resolveConversationId(message, userId, conversationId, docId);
        List<Message> history = conversationService.listMessages(resolvedConversationId);
        conversationService.saveMessage(resolvedConversationId, "user", message);

        List<org.springframework.ai.chat.messages.Message> messages = buildMessages(systemPrompt, history, message);
        Prompt prompt = new Prompt(messages);

        StringBuilder fullAnswer = new StringBuilder();
        return chatClient.prompt(prompt).stream().content()
                .doOnNext(token -> fullAnswer.append(token))
                .doOnComplete(() -> conversationService.saveMessage(resolvedConversationId, "assistant", fullAnswer.toString()));
    }

    @Override
    public Long createConversationOnly(String message, Long userId, Long docId) {
        String title = (message == null || message.isBlank())
                ? "新对话"
                : (message.length() > 20 ? message.substring(0, 20) : message);
        return conversationService.createConversation(userId, docId, title);
    }

    /**
     * 解析会话ID：无则新建，有则校验归属（防越权）
     */
    private Long resolveConversationId(String message, Long userId, Long conversationId, Long docId) {
        if (conversationId == null) {
            String title;
            if (message == null || message.isBlank()) {
                title = "新对话";
            } else {
                title = message.length() > 20 ? message.substring(0, 20) : message;
            }
            return conversationService.createConversation(userId, docId, title);
        }
        validateOwnership(conversationId, userId);
        return conversationId;
    }

    /**
     * 校验会话归属
     */
    private void validateOwnership(Long conversationId, Long userId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException("会话不存在");
        }
        if (!userId.equals(conversation.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限访问该会话");
        }
    }

    /**
     * 构建文档问答的 system prompt（含文档全文 + 评论）
     */
    private String buildDocSystemPrompt(Long docId) {
        // 优先取全文（doc.content），避免只取前几条切片导致长文档后面章节丢失
        String content = docService.getDocContent(docId);
        if (content == null || content.isBlank()) {
            // 兜底：content 为空时，回退到切片拼接
            List<String> chunks = docService.getDocChunks(docId);
            if (chunks == null || chunks.isEmpty()) {
                throw new BusinessException("文档内容为空");
            }
            content = String.join("\n---\n", chunks);
        }
        // 纯 CPU 推理下全文过长会非常慢，超过 50000 字截断，避免长时间无响应
        if (content.length() > 50000) {
            content = content.substring(0, 50000) + "\n\n...(文档过长，已截断)";
        }

        // 取评论（最新 10 条），作为补充信息
        List<Annotation> annotations = annotationService.listByDocId(docId);
        StringBuilder commentsText = new StringBuilder();
        if (annotations != null && !annotations.isEmpty()) {
            int count = Math.min(10, annotations.size());
            for (int i = 0; i < count; i++) {
                Annotation a = annotations.get(i);
                String typeText = switch (a.getType()) {
                    case "SUPPLEMENT" -> "补充";
                    case "CORRECTION" -> "纠错";
                    case "QUESTION" -> "提问";
                    default -> "评论";
                };
                commentsText.append("- [").append(typeText).append("] ")
                        .append(a.getUserName() != null ? a.getUserName() : "匿名")
                        .append("：").append(a.getContent()).append("\n");
            }
        }

        StringBuilder sys = new StringBuilder();
        sys.append("你是知识库助手，请基于以下文档内容和用户评论回答用户问题。\n");
        sys.append("如果文档中没有相关信息，请明确说明\"文档中未提及\"，不要编造。\n\n");
        // 评论放正文之前：正文可能很长，放后面容易被模型上下文截断导致“读不到评论”
        if (commentsText.length() > 0) {
            sys.append("用户评论（可作为补充信息参考）：\n");
            sys.append(commentsText).append("\n");
        }
        sys.append("文档内容：\n");
        sys.append(content);

        log.info("文档问答上下文构建完成, docId={}, contentLength={}, comments={}",
                docId, content.length(), annotations == null ? 0 : annotations.size());
        return sys.toString();
    }

    /**
     * 非流式调用：带历史上下文调 AI，失败返回容错文案
     */
    private String callAiWithHistory(String systemPrompt, List<Message> history, String currentMessage) {
        try {
            Prompt prompt = new Prompt(buildMessages(systemPrompt, history, currentMessage));
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            log.error("AI 调用失败", e);
            return "AI 服务暂时不可用，请稍后重试";
        }
    }

    /**
     * 构造 Spring AI 消息列表：system + 历史（最多 10 条）+ 当前消息
     */
    private List<org.springframework.ai.chat.messages.Message> buildMessages(
            String systemPrompt, List<Message> history, String currentMessage) {
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        int start = Math.max(0, history.size() - 10);
        for (int i = start; i < history.size(); i++) {
            Message m = history.get(i);
            if ("user".equals(m.getRole())) {
                messages.add(new UserMessage(m.getContent()));
            } else if ("assistant".equals(m.getRole())) {
                messages.add(new AssistantMessage(m.getContent()));
            }
        }
        messages.add(new UserMessage(currentMessage));
        return messages;
    }
}
