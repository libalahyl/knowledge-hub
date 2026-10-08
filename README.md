# Knowledge Hub · AI 个人知识库

> 面向软件工程学生的**学习笔记博客 + AI 助手**平台，前后端分离、多模块 Spring Boot 项目。

---

## 一、项目背景

学习过程中产生的 Markdown 笔记往往散落在本地、不便检索和分享，传统笔记软件又缺乏「社区互动」和「AI 辅助」能力。Knowledge Hub 旨在解决这一问题：

- **知识沉淀**：把 Markdown 学习笔记统一上传、归类到文件夹，形成结构化的个人/公共知识库；
- **社区协作**：其他学习者可以在文档下发表「补充 / 纠错 / 提问」三类评论，对不良内容举报，由管理员治理；
- **AI 助手**：接入本地 Ollama 大模型，支持基于「文档正文」的多轮问答，并支持流式输出，读笔记时随手问 AI；
- **分层权限**：游客可浏览搜索，注册用户可上传/评论/收藏/问答，管理员拥有分类管理与举报处理的治理权限。

系统采用前后端分离架构：后端为 Spring Boot 多模块工程，前端为 Vue 3 + Element Plus 单页应用，两者通过 RESTful API（含 SSE 流式接口）通信。

---

## 二、技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| JDK | 17 | |
| Spring Boot | 3.4.3 | 多模块 Maven 工程 |
| MyBatis-Plus | 3.5.10.1 | `mybatis-plus-spring-boot3-starter` |
| MySQL | 8.0 | 21 张表 |
| Redis | 5.0+ | JWT 黑名单（登出失效） |
| Spring AI | 1.0.1 | 对接 Ollama |
| Ollama | - | 本地模型 `qwen2.5-3b-20k` |
| MinIO Client | 8.5.12 | S3 兼容，连接 RustFS/MinIO 对象存储 |
| JWT | jjwt 0.11.5 | 登录态签发与校验 |
| SpringDoc | 2.8.5 | Swagger UI |
| Lombok | - | 简化实体样板代码 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.13 | `<script setup>` 组合式 API |
| Vite | 6.0.5 | 构建 + 开发服务器（`/api` 代理） |
| Element Plus | 2.9.1 | UI 组件库（按需自动导入） |
| Pinia | 2.3.0 | 状态管理（用户登录态） |
| Vue Router | 4.5.0 | 路由 + 守卫 |
| Axios | 1.7.9 | HTTP 请求 + 拦截器 |
| marked | 15.0.4 | Markdown 渲染（自定义极简渲染器） |
| Node.js | 22 | 运行环境 |

---

## 三、项目架构

### 3.1 后端多模块（7 个）

```
knowledge-hub/
├── knowledge-common/       # 统一返回 Result、异常、常量（DocConst、StorageConst）
├── knowledge-framework/    # JWT、Redis、MyBatis-Plus、MinIO、全局异常、拦截器、@RequireLogin/@RequireAdmin
├── knowledge-account/      # 用户、登录/注册/登出
├── knowledge-doc/          # 文档、文件夹、标签、注释(评论/回复/点赞)、收藏、举报、站内通知、切片、文件存储
├── knowledge-ai/           # AI 问答（非流式/流式）、会话
├── knowledge-search/       # 关键词搜索、搜索历史
└── knowledge-server/       # 启动入口 + application.yml
```

模块间依赖**单向**（`server → 各业务模块 → framework/common`），跨模块调用只走 Service 接口，禁止直接访问 Mapper。

### 3.2 单模块内部分层

```
com.knowledgehub.<模块>
├── controller/   # 接口层：收参数、鉴权、调 Service、返回 Result
├── service/      # 业务接口
│   └── impl/     # 业务实现
├── mapper/       # MyBatis-Plus Mapper
├── entity/       # 数据库表实体
├── dto/          # 请求参数对象
├── vo/           # 响应对象
├── config/       # 配置类
├── annotation/   # 自定义注解（@RequireLogin / @RequireAdmin）
└── context/      # 当前登录用户上下文（UserContext）
```

请求链路：`Controller`（收参、鉴权）→ `Service`（业务）→ `Mapper`（数据库）。

### 3.3 前端结构

```
knowledge-hub-ui/
├── src/api/            # 按模块封装的接口函数（auth/doc/category/annotation/favorite/report/search/ai）
├── src/components/     # 通用组件（AiChatPanel、CommentSection、UploadDialog 等）
├── src/request/        # axios 实例 + 拦截器
├── src/router/         # 路由 + 守卫
├── src/stores/         # Pinia（user 登录态）
└── src/views/          # 页面
    ├── Login.vue / Register.vue
    ├── Home.vue        # 三栏主布局
    ├── docs/           # 文档列表、详情、文件夹、我的收藏/上传/文件夹/问答
    └── admin/          # 分类管理、举报处理
```

---

## 四、数据库建模

数据库 `knowledge_hub`，字符集 `utf8mb4`，共 21 张表，按业务域分为四组：**用户与角色**、**文档域**、**AI 域**、**日志**。初始化脚本见 `sql/init.sql`（幂等，可重复执行）。

### 4.1 用户与角色

**`sys_user` 用户表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(100) | 密码（BCrypt 加密） |
| nickname | VARCHAR(50) | 昵称 |
| avatar | VARCHAR(255) | 头像 |
| role | VARCHAR(20) | 角色：`USER` / `ADMIN` |
| status | TINYINT | 状态：1 正常 / 0 禁用 |
| create_time / update_time | DATETIME | 创建 / 更新时间 |

> 预置管理员账号：`admin / 123456`（BCrypt 存储）。
>
> `sys_role`、`sys_user_role`、`sys_permission`、`sys_role_permission` 四张表已在脚本中建好，但**当前版本未接入业务逻辑**：鉴权直接使用 `sys_user.role` 字段（`USER`/`ADMIN` 两级），未做细粒度 RBAC 权限点校验。

### 4.2 文档域

**`knowledge_doc` 知识文档表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| title | VARCHAR(200) | 标题 |
| summary | VARCHAR(500) | 摘要 |
| content | LONGTEXT | Markdown 原文 |
| category_id | BIGINT | 所属文件夹/分类 ID（可空，空=未归类） |
| tags | VARCHAR(200) | 标签，逗号分隔字符串（冗余存储） |
| uploader_id | BIGINT | 上传者 ID |
| uploader_name | VARCHAR(50) | 上传者昵称（冗余） |
| source_type | VARCHAR(20) | 来源：`PRESET` 预置 / `UPLOAD` 上传 |
| file_path | VARCHAR(500) | 对象存储中的文件路径 |
| file_size | BIGINT | 文件大小（字节） |
| audit_status | VARCHAR(20) | 审核状态：`PENDING`/`APPROVED`/`REJECTED` |
| status | TINYINT | 状态：1 正常 / 0 已删除（软删除） |
| view_count | INT | 浏览量 |
| favorite_count | INT | 收藏数 |
| is_public | TINYINT | 是否公开：1 公开 / 0 私有 |
| create_time / update_time | DATETIME | 创建 / 更新时间 |

**`knowledge_category` 分类 / 文件夹表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| name | VARCHAR(50) | 名称 |
| sort | INT | 排序值 |
| owner_id | BIGINT | 创建者 ID（`NULL` = 系统预置分类） |
| is_public | TINYINT | 是否公开：1 公开 / 0 私有 |
| parent_id | BIGINT | 父分类 ID（预留多级，暂未使用） |
| create_time | DATETIME | 创建时间 |

**`knowledge_chunk` 文档切片表**（供 AI 文档问答读取上下文）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| doc_id | BIGINT | 文档 ID |
| chunk_index | INT | 切片序号 |
| content | TEXT | 切片内容 |
| create_time | DATETIME | 创建时间 |

**`knowledge_annotation` 注释（评论）表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| doc_id | BIGINT | 文档 ID |
| user_id | BIGINT | 作者 ID |
| user_name | VARCHAR(50) | 作者昵称（冗余） |
| reply_to_user_name | VARCHAR(50) | 被回复人昵称（@某人） |
| content | VARCHAR(1000) | 内容 |
| type | VARCHAR(20) | 类型：`SUPPLEMENT` 补充 / `CORRECTION` 纠错 / `QUESTION` 提问 |
| parent_id | BIGINT | 父评论 ID（`NULL` = 顶层评论） |
| status | TINYINT | 状态：1 正常 / 0 已删除 |
| like_count | INT | 点赞数 |
| create_time / update_time | DATETIME | 创建 / 更新时间 |

**`knowledge_annotation_like` 评论点赞表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| annotation_id | BIGINT | 评论 ID |
| user_id | BIGINT | 点赞人 ID |
| create_time / update_time | DATETIME | 创建 / 更新时间 |

> `(annotation_id, user_id)` 唯一约束，防重复点赞。

**`knowledge_favorite` 收藏表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户 ID |
| doc_id | BIGINT | 文档 ID |
| create_time | DATETIME | 收藏时间 |

> `(user_id, doc_id)` 唯一约束，防重复收藏。

**`knowledge_report` 举报表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| doc_id | BIGINT | 被举报文档 ID |
| comment_id | BIGINT | 被举报评论 ID（`NULL` = 文档举报） |
| reporter_id | BIGINT | 举报人 ID |
| reporter_name | VARCHAR(50) | 举报人昵称（冗余） |
| reason | VARCHAR(500) | 举报原因 |
| status | VARCHAR(20) | 状态：`PENDING` 待处理 / `HANDLED` 已处理 / `IGNORED` 已忽略 |
| handler_id | BIGINT | 处理人 ID |
| handle_remark | VARCHAR(500) | 处理备注 |
| handle_time | DATETIME | 处理时间 |
| create_time | DATETIME | 创建时间 |

**`knowledge_download_log` 下载记录表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| doc_id | BIGINT | 文档 ID |
| user_id | BIGINT | 用户 ID |
| user_name | VARCHAR(50) | 用户名（冗余） |
| ip | VARCHAR(50) | 下载 IP |
| create_time | DATETIME | 下载时间 |

**`knowledge_notification` 站内通知表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 接收人 ID |
| type | VARCHAR(32) | 类型：`DOC_COMMENT` 文档被评论 / `ANNOTATION_REPLY` 评论被回复 |
| doc_id | BIGINT | 相关文档 ID |
| doc_title | VARCHAR(200) | 文档标题快照 |
| annotation_id | BIGINT | 相关评论 ID |
| from_user_id | BIGINT | 触发人 ID |
| from_user_name | VARCHAR(50) | 触发人昵称快照 |
| content | VARCHAR(500) | 通知摘要 |
| is_read | TINYINT | 0 未读 / 1 已读 |
| status | TINYINT | 1 正常 / 0 已删除 |
| create_time / update_time | DATETIME | 创建 / 更新时间 |

### 4.3 AI 域

**`ai_conversation` 会话表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 用户 ID |
| doc_id | BIGINT | 关联文档 ID（`NULL` = 全局问答） |
| title | VARCHAR(200) | 会话标题 |
| create_time / update_time | DATETIME | 创建 / 更新时间 |

**`ai_message` 消息表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| conversation_id | BIGINT | 会话 ID |
| role | VARCHAR(20) | 角色：`user` / `assistant` / `system` |
| content | TEXT | 消息内容 |
| create_time | DATETIME | 创建时间 |

### 4.4 日志

**`knowledge_search_log` 搜索历史表**

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| keyword | VARCHAR(200) | 搜索关键词 |
| user_id | BIGINT | 用户 ID（游客为 `NULL`） |
| create_time | DATETIME | 搜索时间 |

> **预留表（已建表、未接入业务）**：`knowledge_tag` / `knowledge_doc_tag`（标签关联，实际标签以字符串冗余存于 `knowledge_doc.tags`）、`ai_model_config`（模型配置，实际模型来自 `application.yml`）、`sys_operation_log`（操作日志，暂无切面写入）。

---

## 五、后端设计

### 5.1 统一响应

所有接口返回统一结构 `Result<T>`，**HTTP 状态码统一为 200**，业务成败看 body 内的 `code`：

```json
{ "code": 200, "message": "操作成功", "data": { } }
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未登录 / 登录失效 |
| 403 | 无权限 |
| 500 | 业务失败 / 系统异常 |

分页统一使用 MyBatis-Plus `Page` 结构：`{ records, total, size, current, pages }`，时间字段为 ISO-8601 字符串。

### 5.2 鉴权机制

1. **登录**：`POST /api/auth/login` 校验用户名密码（BCrypt），签发 JWT（24h），返回 `token` 与 `user`。
2. **携带**：前端将 token 存于 `localStorage`，请求头带 `Authorization: Bearer <token>`。
3. **校验**：`JwtInterceptor` 解析并校验 token，把用户信息写入 `UserContext`（ThreadLocal）。
4. **登出**：token 写入 Redis 黑名单（`TokenBlacklistUtil`），立即失效。
5. **权限分级**（自定义注解）：
   - 公开：无注解，游客可访问；
   - 登录：`@RequireLogin`（上传、编辑、删除、评论、收藏、举报、AI 问答等）；
   - 管理员：`@RequireAdmin`（分类管理、举报处理），基于 `user.role == "ADMIN"`。

### 5.3 全局异常处理

`GlobalExceptionHandler` 统一捕获 `BusinessException` 与系统异常，转换为 `Result` 结构返回，前端拦截器据此提示。

### 5.4 核心机制

- **软删除**：文档删除仅置 `status=0`，不物理删除，保留审计记录；收藏/评论删除同理。
- **文档切片**：上传时用 `MarkdownChunkUtil` 将正文切块存入 `knowledge_chunk`，AI 文档问答据此取上下文。
- **文件存储**：`FileStorageService` 抽象接口，按 `storage.type` 配置在 `RustFsFileStorageServiceImpl`（S3 兼容对象存储）与 `LocalFileStorageServiceImpl`（本地磁盘）之间切换，上传返回路径，下载按路径读回字节。
- **评论防刷**：同一用户对同一文档 1 分钟内不能重复评论，单文档评论上限 10 条（`DocConst`）。
- **评论回复**：二级评论模型（顶层评论 + 一层回复），回复「回复」时 `parent_id` 自动归一化到顶层；删除顶层评论级联软删其回复。
- **评论点赞**：`knowledge_annotation_like` 记录点赞关系，`like_count` 计数，点赞/取消为 toggle。
- **站内通知**：文档被评论、评论被回复时触发通知（自己触发自己的不通知），前端铃铛轮询未读数，支持单条/全部已读。
- **AI 多轮 + 流式**：非流式接口返回 `conversationId`；流式接口通过响应头 `X-Conversation-Id` 回传会话 ID，前端据此续接多轮上下文；流式基于 Spring WebFlux `Flux<String>` 以 SSE 逐 token 输出。

---

## 六、接口设计

> 全部接口前缀 `/api`；标记 🔒 需登录（`@RequireLogin`）、🔑 需管理员（`@RequireAdmin`），无标记为公开。

### 6.1 认证与用户

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 注册，`{ username, password, nickname }` |
| POST | `/api/auth/login` | 登录，`{ username, password }` → `{ token, user }` |
| POST | `/api/auth/logout` | 登出（Bearer token 进黑名单） |
| GET | `/api/user/info` | 当前用户信息 → `UserVO` |
| GET | `/api/ping` | 健康检查 → `"pong"` |

### 6.2 文档 Doc（`/api/doc`）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/page` | 公开 | 分页列表，参数 `page/size/title/categoryId/uploaderId` |
| GET | `/page/public` | 公开 | 公开文档列表：无 `categoryId` 时返回**未归类的公开文档**（首页）；传 `categoryId` 返回该文件夹下公开文档（文件夹详情页） |
| GET | `/search` | 公开 | 关键词搜索 `keyword/page/size` |
| GET | `/{id}` | 公开 | 文档详情（浏览量 +1）→ `DocDetailVO` |
| POST | `/upload` | 🔒 | 上传，multipart：`file(.md)/title/summary/categoryId/tags` → `docId` |
| POST | `/upload/batch` | 🔒 | 批量上传，`files[]/categoryId` → `{ successCount, failCount, failList }` |
| PUT | `/{id}` | 🔒 | 编辑，`{ title, summary, categoryId, tags }`（本人或管理员） |
| DELETE | `/{id}` | 🔒 | 删除（软删除，本人或管理员） |
| GET | `/{id}/download` | 公开 | 下载原始 `.md` 文件流（记录下载日志） |

### 6.3 分类 / 文件夹 Category（`/api/category`）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/list` | 公开 | 全部分类列表 |
| POST | `` | 🔑 | 新建系统分类 `{ name, sort }` |
| PUT | `/{id}` | 🔑 | 更新分类 `{ name, sort }` |
| DELETE | `/{id}` | 🔑 | 删除分类（下有文档则报错） |
| GET | `/public` | 公开 | 公开文件夹列表（首页展示） |
| GET | `/visible` | 🔒 | 当前用户可见分类（系统预置 + 公开 + 自己的，首页「全部分类」下拉） |
| GET | `/mine` | 🔒 | 我的文件夹列表 |
| POST | `/mine` | 🔒 | 创建我的文件夹 `{ name, isPublic }` |
| PUT | `/mine/{id}` | 🔒 | 编辑我的文件夹（改名称/公开性，校验归属） |
| DELETE | `/mine/{id}` | 🔒 | 删除我的文件夹（空文件夹才能删） |

### 6.4 注释 Annotation（`/api/annotation`）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/list/{docId}` | 公开 | 某文档的注释列表 |
| GET | `/tree/{docId}` | 公开 | 某文档的评论树（顶层评论 + 嵌套回复，含点赞状态） |
| POST | `` | 🔒 | 添加注释 `{ docId, content, type, replyToId }` → `id`（`replyToId` 非空=回复） |
| DELETE | `/{id}` | 🔒 | 删除注释（本人/管理员；顶层评论级联删回复） |
| POST | `/{id}/like` | 🔒 | 点赞/取消点赞 → `{ liked, likeCount }` |

`type` 取值：`SUPPLEMENT` / `CORRECTION` / `QUESTION`。

### 6.5 收藏 Favorite（`/api/favorite`）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/{docId}` | 🔒 | 收藏 |
| DELETE | `/{docId}` | 🔒 | 取消收藏 |
| GET | `/check/{docId}` | 🔒 | 是否已收藏 → `Boolean` |
| GET | `/my` | 🔒 | 我的收藏分页 `page/size` |

### 6.6 举报 Report（`/api/report`）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `` | 🔒 | 提交举报 `{ docId, commentId, reason }` → `id`（`commentId` 非空=举报评论） |
| GET | `/page` | 🔑 | 举报分页列表 `page/size/status` |
| PUT | `/{id}/handle` | 🔑 | 处理举报 `{ status, handleRemark }`（`HANDLED` 会软删被举报文档/评论） |

`status` 取值：`PENDING` / `HANDLED` / `IGNORED`。

### 6.7 标签 Tag（`/api/tag`，只读）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/list` | 标签列表 |
| GET | `/doc/{docId}` | 某文档的标签列表 |

### 6.8 AI 问答 Ai（`/api/ai`）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| POST | `/chat` | 🔒 | 全局问答（非流式）`{ message, conversationId }` → `{ conversationId, answer }` |
| POST | `/chat/doc` | 🔒 | 文档问答（非流式）`{ docId, message, conversationId }` |
| POST | `/chat/stream` | 🔒 | 全局问答（流式 SSE） |
| POST | `/chat/doc/stream` | 🔒 | 文档问答（流式 SSE） |
| GET | `/ai/conversation/list` | 🔒 | 我的会话列表 |
| GET | `/ai/conversation/{id}/messages` | 🔒 | 某会话消息（归属校验） |
| DELETE | `/ai/conversation/{id}` | 🔒 | 删除会话 |

> `conversationId` 传 `null` 新建会话，传上次返回的 ID 续接多轮上下文；流式接口通过响应头 `X-Conversation-Id` 回传会话 ID。

### 6.9 搜索 Search（`/api/search`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `` | 关键词搜索 `keyword/page/size`（标题命中优先，自动记录搜索历史） |

### 6.10 站内通知 Notification（`/api/notification`）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/unread-count` | 🔒 | 当前用户未读数 → `{ count }` |
| GET | `/page` | 🔒 | 通知分页列表 `page/size` |
| PUT | `/{id}/read` | 🔒 | 标记单条已读 |
| PUT | `/read-all` | 🔒 | 全部标记已读 |

---

## 七、前端设计

### 7.1 整体布局（三栏）

`Home.vue` 采用 `el-container` 三栏布局：

```
┌──────────────────────────────────────────────────────────┐
│ 顶栏：Logo「知识库」 + 通知铃铛 + 用户下拉（退出登录） + AI 面板折叠按钮 │
├──────────┬───────────────────────────────┬───────────────┤
│ 左侧菜单  │       中间内容区 <router-view>  │  右侧 AI 面板  │
│ · 知识库  │  根据路由切换不同页面            │  (可折叠)      │
│ · 我的文件夹│  · 文档列表 / 详情 / 文件夹     │  全局/文档问答  │
│ · 我的收藏 │  · 我的收藏 / 上传 / 问答       │  流式打字机    │
│ · 我的上传 │  · 分类管理 / 举报处理(管理员)  │               │
│ · 我的问答 │                               │               │
│ · 分类管理 │                               │               │
│ · 举报处理 │                               │               │
└──────────┴───────────────────────────────┴───────────────┘
```

右侧 AI 面板常驻，当路由处于 `/doc/:id` 时自动切换为「文档问答」模式（把当前 `docId` 传给 `AiChatPanel`）。

### 7.2 路由

| 路径 | 页面 | 说明 |
|------|------|------|
| `/login` | Login.vue | 登录 |
| `/register` | Register.vue | 注册 |
| `/` | docs/DocList.vue | 知识库首页（文件夹区 + 未归类公开文档区） |
| `/doc/:id` | docs/DocDetail.vue | 文档详情（Markdown + 目录 + 评论） |
| `/folder/:id` | docs/FolderDetail.vue | 文件夹详情（该文件夹下公开文档） |
| `/my-folders` | docs/MyFolders.vue | 我的文件夹（增删改） |
| `/favorites` | docs/MyFavorites.vue | 我的收藏 |
| `/uploads` | docs/MyUploads.vue | 我的上传 |
| `/conversations` | docs/MyConversations.vue | 我的问答（会话历史） |
| `/admin/categories` | admin/CategoryManage.vue | 分类管理（🔑 管理员） |
| `/admin/reports` | admin/ReportManage.vue | 举报处理（🔑 管理员） |

路由守卫 `beforeEach`：未登录访问受保护页面跳登录页；`requiresAdmin` 路由校验 `user.role === 'ADMIN'`。

### 7.3 状态管理（Pinia）

`stores/user.js` 管理登录态：`token` 与 `user` 持久化到 `localStorage`，提供 `setLogin` / `logout`；侧栏管理员菜单、文档详情编辑/删除按钮均据此判断显隐。

### 7.4 请求封装

`request/request.js` 创建 axios 实例（`baseURL: '/api'`，开发环境由 Vite 代理到 `http://localhost:8081`）：

- **请求拦截器**：自动附加 `Authorization: Bearer <token>`；
- **响应拦截器**：统一处理 `code` —— `200` 直接返回 `data`；`401` 清 token 跳登录页；其余弹 `message` 错误提示。

### 7.5 页面与组件

| 组件 | 说明 |
|------|------|
| `AiChatPanel` | AI 对话面板，`fetch + ReadableStream` 流式读取 SSE，从 `X-Conversation-Id` 续接多轮 |
| `CommentSection` | 评论区：发表/回复/点赞（三种类型：补充/纠错/提问），本人/管理员可删 |
| `NotificationBell` | 通知铃铛：未读数角标 + 轮询刷新 + 已读标记 |
| `UploadDialog` | 批量上传 `.md`（≤20 个），可选目标文件夹 |
| `CreateFolderDialog` | 新建我的文件夹（名称 + 公开/私有） |
| `EditDocDialog` | 编辑文档标题/摘要/文件夹/标签 |
| `ReportDialog` | 提交举报 |

### 7.6 Markdown 渲染与性能优化

`DocDetail.vue` 使用 `marked` 的**自定义极简渲染器**（标题、代码块、行内代码、表格、引用）：只保留标题用于生成目录（TOC），不启用语法高亮、表格等增强渲染以节省 DOM；正文超过 100KB 时不生成目录，保证大文档流畅滚动。

---

## 八、快速开始

### 环境要求

- JDK 17、Maven 3.9+
- Node.js ≥ 20（推荐 22）、npm
- MySQL 8.0、Redis 5.0+
- Ollama（本地拉起 `qwen2.5-3b-20k`）
- RustFS 或 MinIO（S3 兼容对象存储）

### 1. 初始化数据库

```bash
mysql -uroot -p < sql/init.sql
```

### 2. 启动后端

```bash
cd knowledge-hub
mvn clean install -DskipTests
java -jar knowledge-server/target/knowledge-server-0.0.1-SNAPSHOT.jar
```

后端端口 **8081**，Swagger UI：`http://localhost:8081/swagger-ui.html`，健康检查：`http://localhost:8081/api/ping`。

### 3. 启动前端

```bash
cd knowledge-hub-ui
npm install
npm run dev
```

前端端口 **5173**，访问 `http://localhost:5173`（`/api` 已代理到 8081）。

### 默认账号

- 管理员：`admin` / `123456`
- 普通用户：注册页自行注册

## License

MIT
