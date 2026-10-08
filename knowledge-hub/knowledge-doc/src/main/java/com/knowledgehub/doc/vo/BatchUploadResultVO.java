package com.knowledgehub.doc.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量上传结果
 */
@Data
public class BatchUploadResultVO {

    /** 成功数量 */
    private Integer successCount;

    /** 失败数量 */
    private Integer failCount;

    /** 失败清单 */
    private List<FailItem> failList = new ArrayList<>();

    /**
     * 失败项
     */
    @Data
    public static class FailItem {
        private String filename;
        private String reason;
    }
}
