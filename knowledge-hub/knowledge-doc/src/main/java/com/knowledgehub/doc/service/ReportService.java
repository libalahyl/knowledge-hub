package com.knowledgehub.doc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.doc.vo.ReportVO;

/**
 * 举报服务接口
 */
public interface ReportService {

    /**
     * 提交举报
     *
     * @param docId     被举报文档ID（评论举报时可为空，后端从评论反查）
     * @param commentId 被举报评论ID（空=文档举报）
     */
    Long submitReport(Long docId, Long commentId, String reason, Long userId);

    Page<ReportVO> pageReports(Integer page, Integer size, String status);

    void handleReport(Long id, String status, String handleRemark, Long adminId);
}
