package com.knowledgehub.doc.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.account.entity.User;
import com.knowledgehub.account.service.UserService;
import com.knowledgehub.common.constant.DocConst;
import com.knowledgehub.common.exception.BusinessException;
import com.knowledgehub.doc.entity.Annotation;
import com.knowledgehub.doc.entity.Doc;
import com.knowledgehub.doc.entity.Report;
import com.knowledgehub.doc.mapper.AnnotationMapper;
import com.knowledgehub.doc.mapper.DocMapper;
import com.knowledgehub.doc.mapper.ReportMapper;
import com.knowledgehub.doc.service.ReportService;
import com.knowledgehub.doc.vo.ReportVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 举报服务实现
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private AnnotationMapper annotationMapper;

    @Resource
    private DocMapper docMapper;

    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitReport(Long docId, Long commentId, String reason, Long userId) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException("举报原因不能为空");
        }
        if (commentId != null) {
            // 评论举报：校验评论存在，docId 从评论反查（冗余）
            Annotation comment = annotationMapper.selectById(commentId);
            if (comment == null || DocConst.STATUS_DELETED.equals(comment.getStatus())) {
                throw new BusinessException("被举报的评论不存在");
            }
            docId = comment.getDocId();
            Long pendingCount = reportMapper.selectCount(Wrappers.<Report>lambdaQuery()
                    .eq(Report::getCommentId, commentId)
                    .eq(Report::getReporterId, userId)
                    .eq(Report::getStatus, DocConst.REPORT_PENDING));
            if (pendingCount > 0) {
                throw new BusinessException("您已举报过该评论");
            }
        } else {
            // 文档举报（原有逻辑）
            Doc doc = docMapper.selectById(docId);
            if (doc == null || DocConst.STATUS_DELETED.equals(doc.getStatus())) {
                throw new BusinessException("文档不存在");
            }
            Long pendingCount = reportMapper.selectCount(Wrappers.<Report>lambdaQuery()
                    .eq(Report::getDocId, docId)
                    .eq(Report::getReporterId, userId)
                    .eq(Report::getStatus, DocConst.REPORT_PENDING));
            if (pendingCount > 0) {
                throw new BusinessException("您已举报过该文档");
            }
        }

        User user = userService.getById(userId);
        Report report = new Report();
        report.setDocId(docId);
        report.setCommentId(commentId);
        report.setReporterId(userId);
        report.setReporterName(user != null ? user.getNickname() : null);
        report.setReason(reason);
        report.setStatus(DocConst.REPORT_PENDING);
        reportMapper.insert(report);
        return report.getId();
    }

    @Override
    public Page<ReportVO> pageReports(Integer page, Integer size, String status) {
        Page<Report> p = new Page<>(page, size);
        reportMapper.selectPage(p, Wrappers.<Report>lambdaQuery()
                .eq(status != null && !status.isBlank(), Report::getStatus, status)
                .orderByDesc(Report::getCreateTime));

        List<Report> records = p.getRecords();
        // 批量查文档标题
        Set<Long> docIds = records.stream().map(Report::getDocId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> docTitleMap = new HashMap<>();
        if (!docIds.isEmpty()) {
            for (Doc d : docMapper.selectBatchIds(docIds)) {
                docTitleMap.put(d.getId(), d.getTitle());
            }
        }
        // 批量查被举报评论
        Set<Long> commentIds = records.stream().map(Report::getCommentId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Annotation> commentMap = new HashMap<>();
        if (!commentIds.isEmpty()) {
            for (Annotation a : annotationMapper.selectBatchIds(commentIds)) {
                commentMap.put(a.getId(), a);
            }
        }

        List<ReportVO> voList = new ArrayList<>();
        for (Report r : records) {
            ReportVO vo = new ReportVO();
            vo.setId(r.getId());
            vo.setDocId(r.getDocId());
            vo.setCommentId(r.getCommentId());
            vo.setReporterId(r.getReporterId());
            vo.setReporterName(r.getReporterName());
            vo.setReason(r.getReason());
            vo.setStatus(r.getStatus());
            vo.setHandlerId(r.getHandlerId());
            vo.setHandleRemark(r.getHandleRemark());
            vo.setHandleTime(r.getHandleTime());
            vo.setCreateTime(r.getCreateTime());
            vo.setTargetType(r.getCommentId() != null ? "COMMENT" : "DOC");
            vo.setDocTitle(docTitleMap.get(r.getDocId()));
            Annotation comment = commentMap.get(r.getCommentId());
            if (comment != null) {
                vo.setCommentContent(comment.getContent());
                vo.setCommentUserName(comment.getUserName());
            }
            voList.add(vo);
        }
        Page<ReportVO> voPage = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReport(Long id, String status, String handleRemark, Long adminId) {
        Report report = reportMapper.selectById(id);
        if (report == null) {
            throw new BusinessException("举报不存在");
        }
        if (!DocConst.REPORT_PENDING.equals(report.getStatus())) {
            throw new BusinessException("该举报已处理");
        }
        // status 只允许 HANDLED 或 IGNORED
        if (!DocConst.REPORT_HANDLED.equals(status) && !DocConst.REPORT_IGNORED.equals(status)) {
            throw new BusinessException("处理状态不合法");
        }
        report.setStatus(status);
        report.setHandlerId(adminId);
        report.setHandleRemark(handleRemark);
        report.setHandleTime(LocalDateTime.now());
        reportMapper.updateById(report);
        // 处理为 HANDLED 时，按类型下架目标
        if (DocConst.REPORT_HANDLED.equals(status)) {
            if (report.getCommentId() != null) {
                // 评论举报：软删评论（顶层则级联软删回复）
                Annotation comment = annotationMapper.selectById(report.getCommentId());
                if (comment != null) {
                    annotationMapper.update(null, Wrappers.<Annotation>lambdaUpdate()
                            .set(Annotation::getStatus, DocConst.STATUS_DELETED)
                            .eq(Annotation::getId, report.getCommentId()));
                    if (comment.getParentId() == null) {
                        annotationMapper.update(null, Wrappers.<Annotation>lambdaUpdate()
                                .set(Annotation::getStatus, DocConst.STATUS_DELETED)
                                .eq(Annotation::getParentId, report.getCommentId())
                                .eq(Annotation::getStatus, DocConst.STATUS_NORMAL));
                    }
                }
            } else {
                // 文档举报：软删文档
                docMapper.update(null, Wrappers.<Doc>lambdaUpdate()
                        .set(Doc::getStatus, DocConst.STATUS_DELETED)
                        .eq(Doc::getId, report.getDocId()));
            }
        }
    }
}
