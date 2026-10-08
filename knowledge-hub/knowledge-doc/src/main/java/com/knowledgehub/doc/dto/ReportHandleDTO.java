package com.knowledgehub.doc.dto;

import lombok.Data;

/**
 * 处理举报参数
 */
@Data
public class ReportHandleDTO {

    /** 处理结果：HANDLED / IGNORED */
    private String status;

    /** 处理备注 */
    private String handleRemark;
}
