package com.knowledgehub.doc.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knowledgehub.account.entity.User;
import com.knowledgehub.account.service.UserService;
import com.knowledgehub.common.constant.DocConst;
import com.knowledgehub.common.exception.BusinessException;
import com.knowledgehub.common.result.ResultCode;
import com.knowledgehub.doc.entity.Annotation;
import com.knowledgehub.doc.entity.AnnotationLike;
import com.knowledgehub.doc.entity.Doc;
import com.knowledgehub.doc.mapper.AnnotationLikeMapper;
import com.knowledgehub.doc.mapper.AnnotationMapper;
import com.knowledgehub.doc.mapper.DocMapper;
import com.knowledgehub.doc.service.AnnotationService;
import com.knowledgehub.doc.service.NotificationService;
import com.knowledgehub.doc.vo.AnnotationLikeVO;
import com.knowledgehub.doc.vo.AnnotationVO;
import com.knowledgehub.framework.context.UserContext;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 注释服务实现
 */
@Slf4j
@Service
public class AnnotationServiceImpl implements AnnotationService {

    @Resource
    private AnnotationMapper annotationMapper;

    @Resource
    private AnnotationLikeMapper annotationLikeMapper;

    @Resource
    private DocMapper docMapper;

    @Resource
    private NotificationService notificationService;

    @Resource
    private UserService userService;

    @Override
    public List<Annotation> listByDocId(Long docId) {
        return annotationMapper.selectList(Wrappers.<Annotation>lambdaQuery()
                .eq(Annotation::getDocId, docId)
                .eq(Annotation::getStatus, DocConst.STATUS_NORMAL)
                .orderByDesc(Annotation::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addAnnotation(Long docId, String content, String type, Long replyToId, Long userId) {
        // 1. 文档存在校验
        Doc doc = docMapper.selectById(docId);
        if (doc == null || DocConst.STATUS_DELETED.equals(doc.getStatus())) {
            throw new BusinessException("文档不存在");
        }
        // 2. 字数校验
        if (content == null || content.isBlank()) {
            throw new BusinessException("注释内容不能为空");
        }
        if (content.length() > DocConst.ANNOTATION_MAX_LENGTH) {
            throw new BusinessException("注释不能超过1000字");
        }
        // 3. 条数校验
        Long count = annotationMapper.selectCount(Wrappers.<Annotation>lambdaQuery()
                .eq(Annotation::getUserId, userId)
                .eq(Annotation::getDocId, docId)
                .eq(Annotation::getStatus, DocConst.STATUS_NORMAL));
        if (count >= DocConst.ANNOTATION_MAX_COUNT_PER_DOC) {
            throw new BusinessException("同一文档最多注释10条");
        }
        // 4. 频率校验（1 分钟内发过则拒绝）
        Annotation last = annotationMapper.selectOne(Wrappers.<Annotation>lambdaQuery()
                .eq(Annotation::getUserId, userId)
                .eq(Annotation::getDocId, docId)
                .orderByDesc(Annotation::getCreateTime)
                .last("LIMIT 1"));
        if (last != null && last.getCreateTime() != null
                && Duration.between(last.getCreateTime(), LocalDateTime.now()).toMinutes() < 1) {
            throw new BusinessException("操作太频繁，请稍后再试");
        }
        // type 容错：不合法默认 SUPPLEMENT
        String validType = type;
        if (!DocConst.ANNOTATION_SUPPLEMENT.equals(type)
                && !DocConst.ANNOTATION_CORRECTION.equals(type)
                && !DocConst.ANNOTATION_QUESTION.equals(type)) {
            validType = DocConst.ANNOTATION_SUPPLEMENT;
        }

        // 5. 处理回复：replyToId 为空=顶层评论；否则找到被回复评论并归一化到顶层
        Long parentId = null;
        String replyToUserName = null;
        Long replyToUserId = null;
        if (replyToId != null) {
            Annotation target = annotationMapper.selectById(replyToId);
            if (target == null || DocConst.STATUS_DELETED.equals(target.getStatus())
                    || !docId.equals(target.getDocId())) {
                throw new BusinessException("被回复的评论不存在");
            }
            replyToUserName = target.getUserName();
            replyToUserId = target.getUserId();
            // 归一化：被回复的是顶层评论 → parent 就是它；被回复的是回复 → parent 提升到顶层
            parentId = (target.getParentId() == null) ? target.getId() : target.getParentId();
        }

        User user = userService.getById(userId);
        Annotation annotation = new Annotation();
        annotation.setDocId(docId);
        annotation.setUserId(userId);
        annotation.setUserName(user != null ? user.getNickname() : null);
        annotation.setContent(content);
        annotation.setType(validType);
        annotation.setParentId(parentId);
        annotation.setReplyToUserName(replyToUserName);
        annotation.setStatus(DocConst.STATUS_NORMAL);
        annotationMapper.insert(annotation);
        Long annotationId = annotation.getId();

        // 触发站内通知（失败不影响主流程）
        try {
            if (parentId == null) {
                // 顶层评论 → 通知文档作者（自己评自己的文档不通知）
                if (doc.getUploaderId() != null && !doc.getUploaderId().equals(userId)) {
                    notificationService.create(doc.getUploaderId(), DocConst.NOTIFICATION_DOC_COMMENT,
                            docId, doc.getTitle(), annotationId, userId, annotation.getUserName(),
                            "评论了你的文档：" + content);
                }
            } else if (replyToUserId != null && !replyToUserId.equals(userId)) {
                // 回复 → 通知被回复评论作者（自己回复自己不通知）
                notificationService.create(replyToUserId, DocConst.NOTIFICATION_ANNOTATION_REPLY,
                        docId, doc.getTitle(), annotationId, userId, annotation.getUserName(),
                        "回复了你：" + content);
            }
        } catch (Exception e) {
            log.warn("发送站内通知失败, docId={}, annotationId={}", docId, annotationId, e);
        }

        return annotationId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnnotation(Long id, Long userId) {
        Annotation annotation = annotationMapper.selectById(id);
        if (annotation == null || DocConst.STATUS_DELETED.equals(annotation.getStatus())) {
            throw new BusinessException("注释不存在");
        }
        // 权限：作者或管理员
        if (!userId.equals(annotation.getUserId()) && !DocConst.ROLE_ADMIN.equals(UserContext.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限删除该注释");
        }
        annotation.setStatus(DocConst.STATUS_DELETED);
        annotationMapper.updateById(annotation);
        // 顶层评论：级联软删其所有回复
        if (annotation.getParentId() == null) {
            annotationMapper.update(null, Wrappers.<Annotation>lambdaUpdate()
                    .set(Annotation::getStatus, DocConst.STATUS_DELETED)
                    .eq(Annotation::getParentId, id)
                    .eq(Annotation::getStatus, DocConst.STATUS_NORMAL));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnotationLikeVO likeAnnotation(Long annotationId, Long userId) {
        Annotation annotation = annotationMapper.selectById(annotationId);
        if (annotation == null || DocConst.STATUS_DELETED.equals(annotation.getStatus())) {
            throw new BusinessException("评论不存在");
        }
        AnnotationLike exist = annotationLikeMapper.selectOne(Wrappers.<AnnotationLike>lambdaQuery()
                .eq(AnnotationLike::getAnnotationId, annotationId)
                .eq(AnnotationLike::getUserId, userId));
        boolean liked;
        if (exist != null) {
            // 已点赞 → 取消：删记录 + 计数 -1
            annotationLikeMapper.deleteById(exist.getId());
            annotationMapper.update(null, Wrappers.<Annotation>lambdaUpdate()
                    .setSql("like_count = like_count - 1")
                    .eq(Annotation::getId, annotationId));
            liked = false;
        } else {
            // 未点赞 → 点赞：插记录 + 计数 +1
            AnnotationLike like = new AnnotationLike();
            like.setAnnotationId(annotationId);
            like.setUserId(userId);
            annotationLikeMapper.insert(like);
            annotationMapper.update(null, Wrappers.<Annotation>lambdaUpdate()
                    .setSql("like_count = like_count + 1")
                    .eq(Annotation::getId, annotationId));
            liked = true;
        }
        // 回读最新计数
        Annotation fresh = annotationMapper.selectById(annotationId);
        AnnotationLikeVO vo = new AnnotationLikeVO();
        vo.setLiked(liked);
        vo.setLikeCount(fresh != null && fresh.getLikeCount() != null ? fresh.getLikeCount() : 0);
        return vo;
    }

    @Override
    public List<AnnotationVO> listCommentTree(Long docId, Long currentUserId) {
        List<Annotation> all = annotationMapper.selectList(Wrappers.<Annotation>lambdaQuery()
                .eq(Annotation::getDocId, docId)
                .eq(Annotation::getStatus, DocConst.STATUS_NORMAL)
                .orderByAsc(Annotation::getCreateTime));
        // 当前用户已点赞的评论ID集合
        Set<Long> likedIds = new HashSet<>();
        if (currentUserId != null && !all.isEmpty()) {
            List<Long> ids = all.stream().map(Annotation::getId).collect(Collectors.toList());
            List<AnnotationLike> likes = annotationLikeMapper.selectList(Wrappers.<AnnotationLike>lambdaQuery()
                    .in(AnnotationLike::getAnnotationId, ids)
                    .eq(AnnotationLike::getUserId, currentUserId));
            likedIds = likes.stream().map(AnnotationLike::getAnnotationId).collect(Collectors.toSet());
        }
        // 顶层评论 + 其回复（parent 已归一化到顶层，可直接归到对应顶层下）
        Map<Long, AnnotationVO> topMap = new LinkedHashMap<>();
        List<AnnotationVO> tops = new ArrayList<>();
        for (Annotation a : all) {
            if (a.getParentId() == null) {
                AnnotationVO vo = toVO(a, likedIds);
                vo.setReplies(new ArrayList<>());
                topMap.put(a.getId(), vo);
                tops.add(vo);
            }
        }
        for (Annotation a : all) {
            if (a.getParentId() != null) {
                AnnotationVO parent = topMap.get(a.getParentId());
                if (parent != null && parent.getReplies() != null) {
                    parent.getReplies().add(toVO(a, likedIds));
                }
            }
        }
        return tops;
    }

    private AnnotationVO toVO(Annotation a, Set<Long> likedIds) {
        AnnotationVO vo = new AnnotationVO();
        vo.setId(a.getId());
        vo.setDocId(a.getDocId());
        vo.setUserId(a.getUserId());
        vo.setUserName(a.getUserName());
        vo.setReplyToUserName(a.getReplyToUserName());
        vo.setContent(a.getContent());
        vo.setType(a.getType());
        vo.setParentId(a.getParentId());
        vo.setStatus(a.getStatus());
        vo.setLikeCount(a.getLikeCount());
        vo.setLiked(likedIds.contains(a.getId()));
        vo.setCreateTime(a.getCreateTime());
        return vo;
    }
}
