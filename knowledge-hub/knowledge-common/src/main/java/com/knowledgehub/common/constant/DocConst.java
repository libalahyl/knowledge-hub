package com.knowledgehub.common.constant;

import java.util.List;

/**
 * 文档模块常量
 */
public class DocConst {

    private DocConst() {
    }

    /** 文档来源：预置 */
    public static final String SOURCE_PRESET = "PRESET";
    /** 文档来源：用户上传 */
    public static final String SOURCE_UPLOAD = "UPLOAD";

    /** 审核状态：待审核 */
    public static final String AUDIT_PENDING = "PENDING";
    /** 审核状态：已通过 */
    public static final String AUDIT_APPROVED = "APPROVED";
    /** 审核状态：已拒绝 */
    public static final String AUDIT_REJECTED = "REJECTED";

    /** 状态：正常 */
    public static final Integer STATUS_NORMAL = 1;
    /** 状态：已删除 */
    public static final Integer STATUS_DELETED = 0;

    /** 注释类型：补充 */
    public static final String ANNOTATION_SUPPLEMENT = "SUPPLEMENT";
    /** 注释类型：纠错 */
    public static final String ANNOTATION_CORRECTION = "CORRECTION";
    /** 注释类型：提问 */
    public static final String ANNOTATION_QUESTION = "QUESTION";
    /** 注释最大长度 */
    public static final int ANNOTATION_MAX_LENGTH = 1000;
    /** 同一用户对同一文档最多注释条数 */
    public static final int ANNOTATION_MAX_COUNT_PER_DOC = 10;

    /** 举报状态：待处理 */
    public static final String REPORT_PENDING = "PENDING";
    /** 举报状态：已处理 */
    public static final String REPORT_HANDLED = "HANDLED";
    /** 举报状态：已忽略 */
    public static final String REPORT_IGNORED = "IGNORED";

    /** 上传文件允许的后缀 */
    public static final List<String> ALLOWED_EXTENSIONS = List.of("md");
    /** 上传文件最大大小（字节） */
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;

    /** 角色：管理员 */
    public static final String ROLE_ADMIN = "ADMIN";
    /** 角色：普通用户 */
    public static final String ROLE_USER = "USER";

    /** 通知类型：文档被评论 */
    public static final String NOTIFICATION_DOC_COMMENT = "DOC_COMMENT";
    /** 通知类型：评论被回复 */
    public static final String NOTIFICATION_ANNOTATION_REPLY = "ANNOTATION_REPLY";
}
