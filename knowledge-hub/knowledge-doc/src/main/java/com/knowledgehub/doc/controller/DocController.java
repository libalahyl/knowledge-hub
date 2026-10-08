package com.knowledgehub.doc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.common.result.Result;
import com.knowledgehub.doc.dto.DocQueryDTO;
import com.knowledgehub.doc.dto.DocUpdateDTO;
import com.knowledgehub.doc.dto.DocUploadDTO;
import com.knowledgehub.doc.entity.Doc;
import com.knowledgehub.doc.service.DocService;
import com.knowledgehub.doc.vo.BatchUploadResultVO;
import com.knowledgehub.doc.vo.DocDetailVO;
import com.knowledgehub.doc.vo.DocListVO;
import com.knowledgehub.framework.annotation.RequireLogin;
import com.knowledgehub.framework.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 文档管理接口
 */
@RestController
@RequestMapping("/api/doc")
@Tag(name = "文档管理", description = "文档的查询、上传、编辑、删除、下载、搜索")
public class DocController {

    @Resource
    private DocService docService;

    @GetMapping("/page")
    @Operation(summary = "分页查询文档列表")
    public Result<Page<DocListVO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long uploaderId) {
        DocQueryDTO dto = new DocQueryDTO();
        dto.setPage(page);
        dto.setSize(size);
        dto.setTitle(title);
        dto.setCategoryId(categoryId);
        dto.setUploaderId(uploaderId);
        return Result.success(docService.pageDocs(dto));
    }

    @GetMapping("/page/public")
    @Operation(summary = "公开文档列表（含自己的私有文档）")
    public Result<Page<DocListVO>> pagePublic(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId) {
        Long currentUserId = UserContext.getUserId();
        DocQueryDTO dto = new DocQueryDTO();
        dto.setPage(page);
        dto.setSize(size);
        dto.setTitle(title);
        dto.setCategoryId(categoryId);
        return Result.success(docService.pagePublicDocs(dto, currentUserId));
    }

    @GetMapping("/search")
    @Operation(summary = "关键词搜索")
    public Result<Page<DocListVO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(docService.searchDocs(keyword, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询文档详情")
    public Result<DocDetailVO> detail(@PathVariable Long id) {
        Long currentUserId = UserContext.getUserId();
        return Result.success(docService.getDocDetail(id, currentUserId));
    }

    @PostMapping("/upload")
    @RequireLogin
    @Operation(summary = "上传文档")
    public Result<Long> upload(@ModelAttribute DocUploadDTO dto) {
        return Result.success(docService.uploadDoc(dto, UserContext.getUserId()));
    }

    @PostMapping("/upload/batch")
    @RequireLogin
    @Operation(summary = "批量上传文档")
    public Result<BatchUploadResultVO> uploadBatch(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        Long userId = UserContext.getUserId();
        return Result.success(docService.uploadDocsBatch(files, categoryId, userId));
    }

    @PutMapping("/{id}")
    @RequireLogin
    @Operation(summary = "编辑文档")
    public Result<?> update(@PathVariable Long id, @RequestBody DocUpdateDTO dto) {
        docService.updateDoc(id, dto, UserContext.getUserId());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    @Operation(summary = "删除文档")
    public Result<?> delete(@PathVariable Long id) {
        docService.deleteDoc(id, UserContext.getUserId());
        return Result.success();
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "下载文档")
    public ResponseEntity<byte[]> download(@PathVariable Long id, HttpServletRequest request) {
        Long userId = UserContext.getUserId();
        // 查文档拿标题（不增加浏览量）
        Doc doc = docService.getById(id);
        byte[] bytes = docService.downloadDoc(id, userId, request.getRemoteAddr());
        String filename = URLEncoder.encode(doc.getTitle() + ".md", StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
