package com.knowledgehub.doc.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.common.constant.DocConst;
import com.knowledgehub.common.exception.BusinessException;
import com.knowledgehub.common.result.ResultCode;
import com.knowledgehub.doc.entity.Notification;
import com.knowledgehub.doc.mapper.NotificationMapper;
import com.knowledgehub.doc.service.NotificationService;
import com.knowledgehub.doc.vo.NotificationVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 站内通知服务实现
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Resource
    private NotificationMapper notificationMapper;

    @Override
    public long countUnread(Long userId) {
        return notificationMapper.selectCount(Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .eq(Notification::getStatus, DocConst.STATUS_NORMAL));
    }

    @Override
    public Page<NotificationVO> page(Long userId, Integer page, Integer size) {
        Page<Notification> p = new Page<>(page, size);
        notificationMapper.selectPage(p, Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getStatus, DocConst.STATUS_NORMAL)
                .orderByDesc(Notification::getCreateTime));
        List<NotificationVO> voList = new ArrayList<>();
        for (Notification n : p.getRecords()) {
            voList.add(toVO(n));
        }
        Page<NotificationVO> voPage = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id, Long userId) {
        Notification n = notificationMapper.selectById(id);
        if (n == null || !userId.equals(n.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限操作该通知");
        }
        if (n.getIsRead() != null && n.getIsRead() == 0) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long userId) {
        notificationMapper.update(null, Wrappers.<Notification>lambdaUpdate()
                .set(Notification::getIsRead, 1)
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    public void create(Long userId, String type, Long docId, String docTitle, Long annotationId,
                       Long fromUserId, String fromUserName, String content) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setDocId(docId);
        n.setDocTitle(docTitle);
        n.setAnnotationId(annotationId);
        n.setFromUserId(fromUserId);
        n.setFromUserName(fromUserName);
        n.setContent(content != null && content.length() > 500 ? content.substring(0, 500) : content);
        n.setIsRead(0);
        n.setStatus(DocConst.STATUS_NORMAL);
        notificationMapper.insert(n);
    }

    private NotificationVO toVO(Notification n) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setType(n.getType());
        vo.setDocId(n.getDocId());
        vo.setDocTitle(n.getDocTitle());
        vo.setAnnotationId(n.getAnnotationId());
        vo.setFromUserId(n.getFromUserId());
        vo.setFromUserName(n.getFromUserName());
        vo.setContent(n.getContent());
        vo.setIsRead(n.getIsRead());
        vo.setCreateTime(n.getCreateTime());
        return vo;
    }
}
