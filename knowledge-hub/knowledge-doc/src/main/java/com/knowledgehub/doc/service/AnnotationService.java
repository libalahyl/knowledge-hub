package com.knowledgehub.doc.service;

import com.knowledgehub.doc.entity.Annotation;
import com.knowledgehub.doc.vo.AnnotationLikeVO;
import com.knowledgehub.doc.vo.AnnotationVO;

import java.util.List;

/**
 * 注释服务接口
 */
public interface AnnotationService {

    List<Annotation> listByDocId(Long docId);

    /**
     * 添加注释（支持回复）
     *
     * @param replyToId 被回复的评论ID（空=顶层评论）
     */
    Long addAnnotation(Long docId, String content, String type, Long replyToId, Long userId);

    void deleteAnnotation(Long id, Long userId);

    /**
     * 查某文档的评论树（顶层评论 + 嵌套回复），供前端评论区展示
     *
     * @param currentUserId 当前用户ID（可空，用于计算是否已点赞）
     */
    List<AnnotationVO> listCommentTree(Long docId, Long currentUserId);

    /**
     * 点赞/取消点赞（toggle），返回最新状态
     */
    AnnotationLikeVO likeAnnotation(Long annotationId, Long userId);
}
