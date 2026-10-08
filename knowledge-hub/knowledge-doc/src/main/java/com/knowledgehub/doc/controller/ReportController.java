package com.knowledgehub.doc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.common.result.Result;
import com.knowledgehub.doc.dto.ReportHandleDTO;
import com.knowledgehub.doc.dto.ReportSubmitDTO;
import com.knowledgehub.doc.service.ReportService;
import com.knowledgehub.doc.vo.ReportVO;
import com.knowledgehub.framework.annotation.RequireAdmin;
import com.knowledgehub.framework.annotation.RequireLogin;
import com.knowledgehub.framework.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 举报接口
 */
@RestController
@RequestMapping("/api/report")
@Tag(name = "举报", description = "文档举报")
public class ReportController {

    @Resource
    private ReportService reportService;

    @PostMapping
    @RequireLogin
    @Operation(summary = "提交举报（支持评论举报）")
    public Result<Long> submit(@RequestBody ReportSubmitDTO dto) {
        return Result.success(reportService.submitReport(dto.getDocId(), dto.getCommentId(), dto.getReason(), UserContext.getUserId()));
    }

    @GetMapping("/page")
    @RequireAdmin
    @Operation(summary = "举报列表")
    public Result<Page<ReportVO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return Result.success(reportService.pageReports(page, size, status));
    }

    @PutMapping("/{id}/handle")
    @RequireAdmin
    @Operation(summary = "处理举报")
    public Result<?> handle(@PathVariable Long id, @RequestBody ReportHandleDTO dto) {
        reportService.handleReport(id, dto.getStatus(), dto.getHandleRemark(), UserContext.getUserId());
        return Result.success();
    }
}
