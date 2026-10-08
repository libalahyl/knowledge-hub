package com.knowledgehub.ai.controller;

import com.knowledgehub.ai.entity.Conversation;
import com.knowledgehub.ai.entity.Message;
import com.knowledgehub.ai.service.ConversationService;
import com.knowledgehub.common.exception.BusinessException;
import com.knowledgehub.common.result.Result;
import com.knowledgehub.common.result.ResultCode;
import com.knowledgehub.framework.annotation.RequireLogin;
import com.knowledgehub.framework.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 会话接口
 */
@RestController
@RequestMapping("/api/ai/conversation")
@Tag(name = "AI 会话", description = "AI 会话管理")
public class ConversationController {

    @Resource
    private ConversationService conversationService;

    @GetMapping("/list")
    @RequireLogin
    @Operation(summary = "我的会话列表")
    public Result<List<Conversation>> list() {
        Long userId = UserContext.getUserId();
        return Result.success(conversationService.listByUser(userId));
    }

    @GetMapping("/{id}/messages")
    @RequireLogin
    @Operation(summary = "某会话的所有消息")
    public Result<List<Message>> messages(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        // 归属校验，防越权
        Conversation conv = conversationService.getById(id);
        if (conv == null) {
            throw new BusinessException("会话不存在");
        }
        if (!userId.equals(conv.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限");
        }
        return Result.success(conversationService.listMessages(id));
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    @Operation(summary = "删除会话")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        conversationService.deleteConversation(id, userId);
        return Result.success();
    }
}
