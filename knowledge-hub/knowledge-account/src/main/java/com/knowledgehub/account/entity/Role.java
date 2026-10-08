package com.knowledgehub.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体
 */
@Data
@TableName("sys_role")
public class Role {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色名 */
    private String name;

    /** 角色编码（如 USER/ADMIN） */
    private String code;

    /** 描述 */
    private String description;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
