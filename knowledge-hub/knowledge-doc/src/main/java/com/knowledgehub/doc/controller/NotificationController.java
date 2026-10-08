package com.knowledgehub.doc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.common.result.Result;
import com.knowledgehub.doc.service.NotificationService;
import com.knowledgehub.doc.vo.NotificationVO;
import com.knowledgehub.framework.annotation.RequireLogin;
import com.knowledgehub.framework.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 站内通知接口
 */
@RestController
@RequestMapping("/api/notification")
@Tag(name = "站内通知", description = "通知查询与已读标记")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @GetMapping("/unread-count")
    @RequireLogin
    @Operation(summary = "当前用户未读数")
    public Result<Map<String, Long>> unreadCount() {
        Long userId = UserContext.getUserId();
        return Result.success(Map.of("count", notificationService.countUnread(userId)));
    }

    @GetMapping("/page")
    @RequireLogin
    @Operation(summary = "通知分页列表")
    public Result<Page<NotificationVO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        return Result.success(notificationService.page(userId, page, size));
    }

    @PutMapping("/{id}/read")
    @RequireLogin
    @Operation(summary = "标记单条已读")
    public Result<?> read(@PathVariable Long id) {
        notificationService.markRead(id, UserContext.getUserId());
        return Result.success();
    }

    @PutMapping("/read-all")
    @RequireLogin
    @Operation(summary = "全部标记已读")
    public Result<?> readAll() {
        notificationService.markAllRead(UserContext.getUserId());
        return Result.success();
    }
}
