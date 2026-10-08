package com.knowledgehub.doc.controller;

import com.knowledgehub.common.result.Result;
import com.knowledgehub.doc.dto.CategoryCreateDTO;
import com.knowledgehub.doc.dto.CategoryDTO;
import com.knowledgehub.doc.entity.Category;
import com.knowledgehub.doc.service.CategoryService;
import com.knowledgehub.framework.annotation.RequireAdmin;
import com.knowledgehub.framework.annotation.RequireLogin;
import com.knowledgehub.framework.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类管理接口
 */
@RestController
@RequestMapping("/api/category")
@Tag(name = "分类管理", description = "文档分类的查询和管理")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @GetMapping("/list")
    @Operation(summary = "分类列表")
    public Result<List<Category>> list() {
        return Result.success(categoryService.listAll());
    }

    @PostMapping
    @RequireAdmin
    @Operation(summary = "新建分类")
    public Result<Long> create(@RequestBody CategoryDTO dto) {
        return Result.success(categoryService.create(dto.getName(), dto.getSort()));
    }

    @PutMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "更新分类")
    public Result<?> update(@PathVariable Long id, @RequestBody CategoryDTO dto) {
        categoryService.update(id, dto.getName(), dto.getSort());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin
    @Operation(summary = "删除分类")
    public Result<?> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @PostMapping("/mine")
    @RequireLogin
    @Operation(summary = "创建我的文件夹")
    public Result<Long> createMyFolder(@RequestBody CategoryCreateDTO dto) {
        Long userId = UserContext.getUserId();
        return Result.success(categoryService.createMyFolder(userId, dto.getName(), dto.getIsPublic()));
    }

    @GetMapping("/mine")
    @RequireLogin
    @Operation(summary = "我的文件夹列表")
    public Result<List<Category>> listMyFolders() {
        Long userId = UserContext.getUserId();
        return Result.success(categoryService.listMyFolders(userId));
    }

    @PutMapping("/mine/{id}")
    @RequireLogin
    @Operation(summary = "编辑我的文件夹")
    public Result<?> updateMyFolder(@PathVariable Long id, @RequestBody CategoryCreateDTO dto) {
        Long userId = UserContext.getUserId();
        categoryService.updateMyFolder(id, userId, dto.getName(), dto.getIsPublic());
        return Result.success();
    }

    @DeleteMapping("/mine/{id}")
    @RequireLogin
    @Operation(summary = "删除我的文件夹")
    public Result<?> deleteMyFolder(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        categoryService.deleteMyFolder(id, userId);
        return Result.success();
    }

    @GetMapping("/public")
    @Operation(summary = "公开文件夹列表")
    public Result<List<Category>> listPublicFolders() {
        return Result.success(categoryService.listPublicFolders());
    }

    @GetMapping("/visible")
    @RequireLogin
    @Operation(summary = "当前用户可见的分类列表（首页全部分类下拉框）")
    public Result<List<Category>> listVisible() {
        Long userId = UserContext.getUserId();
        return Result.success(categoryService.listVisibleFolders(userId));
    }
}
