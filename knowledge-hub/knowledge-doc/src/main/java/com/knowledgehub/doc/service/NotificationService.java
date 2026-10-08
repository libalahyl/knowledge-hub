package com.knowledgehub.doc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.doc.vo.NotificationVO;

/**
 * 站内通知服务接口
 */
public interface NotificationService {

    long countUnread(Long userId);

    Page<NotificationVO> page(Long userId, Integer page, Integer size);

    void markRead(Long id, Long userId);

    void markAllRead(Long userId);

    /**
     * 创建一条通知（供评论/回复后调用）
     *
     * @param userId       接收人ID
     * @param type         类型：DOC_COMMENT/ANNOTATION_REPLY
     * @param docId        相关文档ID
     * @param docTitle     文档标题快照
     * @param annotationId 相关评论ID
     * @param fromUserId   触发人ID
     * @param fromUserName 触发人昵称快照
     * @param content      通知摘要
     */
    void create(Long userId, String type, Long docId, String docTitle, Long annotationId,
                Long fromUserId, String fromUserName, String content);
}
