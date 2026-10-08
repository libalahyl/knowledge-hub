-- =============================================
-- knowledge-hub 初始化脚本（完整版，21 张表）
-- 全部幂等（可重复执行）：CREATE TABLE IF NOT EXISTS + INSERT IGNORE
-- 所有列均内联在 CREATE TABLE 中，无 ALTER，可安全重复执行
-- =============================================

CREATE DATABASE IF NOT EXISTS knowledge_hub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE knowledge_hub;


CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt）',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 预置管理员：admin / 123456（BCrypt）
INSERT IGNORE INTO sys_user (username, password, nickname, role, status)
VALUES ('admin', '$2a$10$GYnX8nGhrqebTBUkzaDOz.CzUN3OwiZTlWk7r0fKR5KMbS.cxSjD.', '管理员', 'ADMIN', 1);

-- =============================================
-- 1. sys_role 角色表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(50) NOT NULL COMMENT '角色名',
    code VARCHAR(50) NOT NULL COMMENT '角色编码（如 USER/ADMIN）',
    description VARCHAR(200) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- =============================================
-- 2. sys_user_role 用户-角色关联表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户-角色关联表';

-- =============================================
-- 3. sys_permission 权限表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(50) NOT NULL COMMENT '权限名',
    code VARCHAR(100) NOT NULL COMMENT '权限码（如 doc:upload）',
    description VARCHAR(200) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='权限表';

-- =============================================
-- 4. sys_role_permission 角色-权限关联表（只建表不写代码）
-- =============================================
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色-权限关联表';

-- =============================================
-- 5. knowledge_doc 知识文档表
-- =============================================
CREATE TABLE IF NOT EXISTS knowledge_doc (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    summary VARCHAR(500) COMMENT '摘要',
    content LONGTEXT COMMENT '原文内容（Markdown）',
    category_id BIGINT COMMENT '分类ID',
    tags VARCHAR(200) COMMENT '标签（逗号分隔，冗余展示）',
    uploader_id BIGINT COMMENT '上传者ID',
    uploader_name VARCHAR(50) COMMENT '上传者昵称（冗余）',
    source_type VARCHAR(20) NOT NULL DEFAULT 'UPLOAD' COMMENT '来源：PRESET预置/UPLOAD用户上传',
    file_path VARCHAR(500) COMMENT '文件存储路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    audit_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED' COMMENT '审核状态：PENDING/APPROVED/REJECTED',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0已删除',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    favorite_count INT NOT NULL DEFAULT 0 COMMENT '收藏数',
    is_public TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开：1公开 0私有',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_uploader (uploader_id),
    KEY idx_category (category_id),
    KEY idx_status (status),
    KEY idx_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='知识文档表';

-- =============================================
-- 6. knowledge_category 文档分类表
-- =============================================
CREATE TABLE IF NOT EXISTS knowledge_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(50) NOT NULL COMMENT '分类名',
    sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    owner_id BIGINT DEFAULT NULL COMMENT '创建者ID（null=系统预置分类）',
    is_public TINYINT NOT NULL DEFAULT 1 COMMENT '是否公开：1公开 0私有',
    parent_id BIGINT DEFAULT NULL COMMENT '父分类ID（预留多级）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name),
    KEY idx_category_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档分类表';

-- =============================================
-- 7. knowledge_tag 标签表
-- =============================================
CREATE TABLE IF NOT EXISTS knowledge_tag (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(50) NOT NULL COMMENT '标签名',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='标签表';

-- =============================================
-- 8. knowledge_doc_tag 文档-标签关联表
-- =============================================
CREATE TABLE IF NOT EXISTS knowledge_doc_tag (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    doc_id BIGINT NOT NULL COMMENT '文档ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (id),
    UNIQUE KEY uk_doc_tag (doc_id, tag_id),
    KEY idx_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档-标签关联表';

-- =============================================
-- 9. knowledge_chunk 文档切片表
-- =============================================
CREATE TABLE IF NOT EXISTS knowledge_chunk (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    doc_id BIGINT NOT NULL COMMENT '文档ID',
    chunk_index INT NOT NULL COMMENT '切片序号',
    content TEXT NOT NULL COMMENT '切片内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_doc (doc_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档切片表';

-- =============================================
-- 10. knowledge_annotation 文档注释表
-- =============================================
CREATE TABLE IF NOT EXISTS knowledge_annotation (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    doc_id BIGINT NOT NULL COMMENT '文档ID',
    user_id BIGINT NOT NULL COMMENT '注释作者ID',
    user_name VARCHAR(50) COMMENT '作者昵称（冗余）',
    reply_to_user_name VARCHAR(50) COMMENT '被回复人昵称（冗余快照，@某人用）',
    content VARCHAR(1000) NOT NULL COMMENT '注释内容',
    type VARCHAR(20) NOT NULL DEFAULT 'SUPPLEMENT' COMMENT '类型：SUPPLEMENT补充/CORRECTION纠错/QUESTION提问',
    parent_id BIGINT DEFAULT NULL COMMENT '父评论ID（NULL=顶层评论）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常 0已删除',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_doc (doc_id),
    KEY idx_user (user_id),
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档注释表';

-- =============================================
-- 11. knowledge_favorite 收藏表
-- =============================================
CREATE TABLE IF NOT EXISTS knowledge_favorite (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    doc_id BIGINT NOT NULL COMMENT '文档ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_doc (user_id, doc_id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收藏表';

-- =============================================
-- 12. knowledge_report 举报表
-- =============================================
CREATE TABLE IF NOT EXISTS knowledge_report (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    doc_id BIGINT NOT NULL COMMENT '被举报文档ID',
    comment_id BIGINT DEFAULT NULL COMMENT '被举报评论ID（NULL=文档举报）',
    reporter_id BIGINT NOT NULL COMMENT '举报人ID',
    reporter_name VARCHAR(50) COMMENT '举报人昵称（冗余）',
    reason VARCHAR(500) NOT NULL COMMENT '举报原因',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING待处理/HANDLED已处理/IGNORED已忽略',
    handler_id BIGINT COMMENT '处理人ID',
    handle_remark VARCHAR(500) COMMENT '处理备注',
    handle_time DATETIME COMMENT '处理时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_doc (doc_id),
    KEY idx_comment (comment_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='举报表';

-- =============================================
-- 13. knowledge_download_log 下载记录表
-- =============================================
CREATE TABLE IF NOT EXISTS knowledge_download_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    doc_id BIGINT NOT NULL COMMENT '文档ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(50) COMMENT '用户名（冗余）',
    ip VARCHAR(50) COMMENT '下载IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_doc (doc_id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='下载记录表';

-- =============================================
-- 14. ai_conversation AI 会话表
-- =============================================
CREATE TABLE IF NOT EXISTS ai_conversation (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    doc_id BIGINT COMMENT '关联文档ID（可空，空为全局问答）',
    title VARCHAR(200) COMMENT '会话标题',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI会话表';

-- =============================================
-- 15. ai_message AI 消息表
-- =============================================
CREATE TABLE IF NOT EXISTS ai_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色：user/assistant/system',
    content TEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_conversation (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI消息表';

-- =============================================
-- 16. ai_model_config AI 模型配置表
-- =============================================
CREATE TABLE IF NOT EXISTS ai_model_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    model_name VARCHAR(100) NOT NULL COMMENT '模型名（如 qwen2.5:7b）',
    base_url VARCHAR(255) COMMENT 'Ollama 地址',
    temperature DECIMAL(3,2) DEFAULT 0.70 COMMENT '温度',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认：1是 0否',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI模型配置表';

-- =============================================
-- 17. sys_operation_log 操作日志表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT COMMENT '操作人ID',
    username VARCHAR(50) COMMENT '操作人用户名',
    operation VARCHAR(100) COMMENT '操作描述',
    method VARCHAR(200) COMMENT '请求方法（类名.方法名）',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT '请求IP',
    cost_time BIGINT COMMENT '耗时（毫秒）',
    success TINYINT COMMENT '是否成功：1是 0否',
    error_msg VARCHAR(500) COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

-- =============================================
-- 初始化数据（INSERT IGNORE 防重复）
-- =============================================

-- 角色
INSERT IGNORE INTO sys_role (name, code, description) VALUES
('普通用户', 'USER', '普通用户角色'),
('管理员', 'ADMIN', '系统管理员');

-- 给 admin 绑定 ADMIN 角色（按 username/code 关联，不硬编码 id）
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.code = 'ADMIN'
WHERE u.username = 'admin';

-- =============================================
-- 18. knowledge_search_log 搜索历史表
-- =============================================
CREATE TABLE IF NOT EXISTS knowledge_search_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    keyword VARCHAR(200) NOT NULL COMMENT '搜索关键词',
    user_id BIGINT COMMENT '用户ID（可空，游客为空）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '搜索时间',
    PRIMARY KEY (id),
    KEY idx_user (user_id),
    KEY idx_keyword (keyword),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='搜索历史表';

-- =============================================
-- 19. knowledge_annotation_like 评论点赞表
-- =============================================
CREATE TABLE IF NOT EXISTS knowledge_annotation_like (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    annotation_id BIGINT NOT NULL COMMENT '评论ID',
    user_id BIGINT NOT NULL COMMENT '点赞人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_annotation_user (annotation_id, user_id),
    KEY idx_annotation (annotation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评论点赞表';

-- =============================================
-- 20. knowledge_notification 站内通知表
-- =============================================
CREATE TABLE IF NOT EXISTS knowledge_notification (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '接收人ID',
    type VARCHAR(32) NOT NULL COMMENT '类型：DOC_COMMENT/ANNOTATION_REPLY',
    doc_id BIGINT NOT NULL COMMENT '相关文档ID',
    doc_title VARCHAR(200) COMMENT '文档标题快照',
    annotation_id BIGINT COMMENT '相关评论ID',
    from_user_id BIGINT NOT NULL COMMENT '触发人ID',
    from_user_name VARCHAR(50) COMMENT '触发人昵称快照',
    content VARCHAR(500) COMMENT '通知摘要',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0已删除',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='站内通知表';
