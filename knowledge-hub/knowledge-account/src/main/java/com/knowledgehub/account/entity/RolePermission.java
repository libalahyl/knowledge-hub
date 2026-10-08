package com.knowledgehub.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色-权限关联实体
 */
@Data
@TableName("sys_role_permission")
public class RolePermission {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色ID */
    private Long roleId;

    /** 权限ID */
    private Long permissionId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
