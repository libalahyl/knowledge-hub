package com.knowledgehub.search.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.common.result.Result;
import com.knowledgehub.doc.vo.DocListVO;
import com.knowledgehub.framework.context.UserContext;
import com.knowledgehub.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 搜索接口（公开，游客可搜）
 */
@RestController
@RequestMapping("/api/search")
@Tag(name = "搜索", description = "关键词搜索")
public class SearchController {

    @Resource
    private SearchService searchService;

    @GetMapping
    @Operation(summary = "关键词搜索")
    public Result<Page<DocListVO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.getUserId();
        return Result.success(searchService.search(keyword, page, size, userId));
    }
}
