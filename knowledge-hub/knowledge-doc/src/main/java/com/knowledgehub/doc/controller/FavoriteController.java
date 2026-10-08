package com.knowledgehub.doc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.common.result.Result;
import com.knowledgehub.doc.service.FavoriteService;
import com.knowledgehub.doc.vo.DocListVO;
import com.knowledgehub.framework.annotation.RequireLogin;
import com.knowledgehub.framework.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收藏接口
 */
@RestController
@RequestMapping("/api/favorite")
@Tag(name = "收藏", description = "文档收藏")
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    @PostMapping("/{docId}")
    @RequireLogin
    @Operation(summary = "收藏文档")
    public Result<?> add(@PathVariable Long docId) {
        favoriteService.addFavorite(docId, UserContext.getUserId());
        return Result.success();
    }

    @DeleteMapping("/{docId}")
    @RequireLogin
    @Operation(summary = "取消收藏")
    public Result<?> remove(@PathVariable Long docId) {
        favoriteService.removeFavorite(docId, UserContext.getUserId());
        return Result.success();
    }

    @GetMapping("/check/{docId}")
    @RequireLogin
    @Operation(summary = "是否已收藏")
    public Result<Boolean> check(@PathVariable Long docId) {
        return Result.success(favoriteService.isFavorited(docId, UserContext.getUserId()));
    }

    @GetMapping("/my")
    @RequireLogin
    @Operation(summary = "我的收藏")
    public Result<Page<DocListVO>> myFavorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(favoriteService.pageMyFavorites(UserContext.getUserId(), page, size));
    }
}
