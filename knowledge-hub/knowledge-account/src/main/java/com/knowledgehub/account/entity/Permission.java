package com.knowledgehub.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限实体
 */
@Data
@TableName("sys_permission")
public class Permission {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 权限名 */
    private String name;

    /** 权限码（如 doc:upload） */
    private String code;

    /** 描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createTime;
}
