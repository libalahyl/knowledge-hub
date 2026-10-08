package com.knowledgehub.framework.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("sys_operation_log")
public class OperationLog {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人ID */
    private Long userId;

    /** 操作人用户名 */
    private String username;

    /** 操作描述 */
    private String operation;

    /** 请求方法（类名.方法名） */
    private String method;

    /** 请求参数 */
    private String params;

    /** 请求IP */
    private String ip;

    /** 耗时（毫秒） */
    private Long costTime;

    /** 是否成功：1是 0否 */
    private Integer success;

    /** 错误信息 */
    private String errorMsg;

    /** 创建时间 */
    private LocalDateTime createTime;
}
