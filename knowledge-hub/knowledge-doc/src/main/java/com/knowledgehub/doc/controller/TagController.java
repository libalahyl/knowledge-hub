package com.knowledgehub.doc.controller;

import com.knowledgehub.common.result.Result;
import com.knowledgehub.doc.entity.Tag;
import com.knowledgehub.doc.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 标签接口（本阶段只读）
 */
@RestController
@RequestMapping("/api/tag")
@io.swagger.v3.oas.annotations.tags.Tag(name = "标签", description = "标签查询")
public class TagController {

    @Resource
    private TagService tagService;

    @GetMapping("/list")
    @Operation(summary = "标签列表")
    public Result<List<Tag>> list() {
        return Result.success(tagService.listAll());
    }

    @GetMapping("/doc/{docId}")
    @Operation(summary = "某文档的标签列表")
    public Result<List<Tag>> listByDoc(@PathVariable Long docId) {
        return Result.success(tagService.listByDocId(docId));
    }
}
