package com.knowledgehub.ai.controller;

import com.knowledgehub.ai.dto.ChatDTO;
import com.knowledgehub.ai.dto.ChatWithDocDTO;
import com.knowledgehub.ai.service.ChatService;
import com.knowledgehub.ai.vo.ChatResponseVO;
import com.knowledgehub.common.result.Result;
import com.knowledgehub.framework.annotation.RequireLogin;
import com.knowledgehub.framework.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 问答接口
 */
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI 问答", description = "全局问答 + 文档问答")
public class AiController {

    @Resource
    private ChatService chatService;

    @PostMapping("/chat")
    @RequireLogin
    @Operation(summary = "全局问答（非流式）")
    public Result<ChatResponseVO> chat(@Valid @RequestBody ChatDTO dto) {
        Long userId = UserContext.getUserId();
        return Result.success(chatService.chat(dto.getMessage(), userId, dto.getConversationId()));
    }

    @PostMapping("/chat/doc")
    @RequireLogin
    @Operation(summary = "文档问答（非流式）")
    public Result<ChatResponseVO> chatWithDoc(@Valid @RequestBody ChatWithDocDTO dto) {
        Long userId = UserContext.getUserId();
        return Result.success(chatService.chatWithDoc(dto.getDocId(), dto.getMessage(), userId, dto.getConversationId()));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RequireLogin
    @Operation(summary = "全局问答（流式 SSE）")
    public Flux<String> chatStream(@Valid @RequestBody ChatDTO dto, HttpServletResponse response) {
        Long userId = UserContext.getUserId();
        // 传了 conversationId 就复用，没传才新建，提前拿 id 放响应头
        Long conversationId = dto.getConversationId();
        if (conversationId == null) {
            conversationId = chatService.createConversationOnly(dto.getMessage(), userId, null);
        }
        response.setHeader("X-Conversation-Id", String.valueOf(conversationId));
        response.setHeader("Access-Control-Expose-Headers", "X-Conversation-Id");
        return chatService.chatStream(dto.getMessage(), userId, conversationId);
    }

    @PostMapping(value = "/chat/doc/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RequireLogin
    @Operation(summary = "文档问答（流式 SSE）")
    public Flux<String> chatWithDocStream(@Valid @RequestBody ChatWithDocDTO dto, HttpServletResponse response) {
        Long userId = UserContext.getUserId();
        Long conversationId = dto.getConversationId();
        if (conversationId == null) {
            conversationId = chatService.createConversationOnly(dto.getMessage(), userId, dto.getDocId());
        }
        response.setHeader("X-Conversation-Id", String.valueOf(conversationId));
        response.setHeader("Access-Control-Expose-Headers", "X-Conversation-Id");
        return chatService.chatWithDocStream(dto.getDocId(), dto.getMessage(), userId, conversationId);
    }
}
