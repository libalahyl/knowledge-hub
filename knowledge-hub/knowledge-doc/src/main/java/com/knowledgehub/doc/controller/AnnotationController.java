package com.knowledgehub.doc.controller;

import com.knowledgehub.common.result.Result;
import com.knowledgehub.doc.dto.AnnotationCreateDTO;
import com.knowledgehub.doc.entity.Annotation;
import com.knowledgehub.doc.service.AnnotationService;
import com.knowledgehub.doc.vo.AnnotationLikeVO;
import com.knowledgehub.doc.vo.AnnotationVO;
import com.knowledgehub.framework.annotation.RequireLogin;
import com.knowledgehub.framework.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 注释接口
 */
@RestController
@RequestMapping("/api/annotation")
@Tag(name = "注释", description = "文档注释")
public class AnnotationController {

    @Resource
    private AnnotationService annotationService;

    @GetMapping("/list/{docId}")
    @Operation(summary = "某文档的注释列表")
    public Result<List<Annotation>> list(@PathVariable Long docId) {
        return Result.success(annotationService.listByDocId(docId));
    }

    @GetMapping("/tree/{docId}")
    @Operation(summary = "某文档的评论树（含回复）")
    public Result<List<AnnotationVO>> tree(@PathVariable Long docId) {
        return Result.success(annotationService.listCommentTree(docId, UserContext.getUserId()));
    }

    @PostMapping
    @RequireLogin
    @Operation(summary = "添加注释（支持回复）")
    public Result<Long> add(@RequestBody AnnotationCreateDTO dto) {
        Long userId = UserContext.getUserId();
        return Result.success(annotationService.addAnnotation(dto.getDocId(), dto.getContent(), dto.getType(), dto.getReplyToId(), userId));
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    @Operation(summary = "删除注释")
    public Result<?> delete(@PathVariable Long id) {
        annotationService.deleteAnnotation(id, UserContext.getUserId());
        return Result.success();
    }

    @PostMapping("/{id}/like")
    @RequireLogin
    @Operation(summary = "点赞/取消点赞评论")
    public Result<AnnotationLikeVO> like(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        return Result.success(annotationService.likeAnnotation(id, userId));
    }
}
