package com.knowledgehub.doc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.account.entity.User;
import com.knowledgehub.account.service.UserService;
import com.knowledgehub.common.constant.DocConst;
import com.knowledgehub.common.constant.StorageConst;
import com.knowledgehub.common.exception.BusinessException;
import com.knowledgehub.common.result.ResultCode;
import com.knowledgehub.doc.dto.DocQueryDTO;
import com.knowledgehub.doc.dto.DocUpdateDTO;
import com.knowledgehub.doc.dto.DocUploadDTO;
import com.knowledgehub.doc.entity.Annotation;
import com.knowledgehub.doc.entity.Category;
import com.knowledgehub.doc.entity.Chunk;
import com.knowledgehub.doc.entity.Doc;
import com.knowledgehub.doc.entity.DownloadLog;
import com.knowledgehub.doc.mapper.ChunkMapper;
import com.knowledgehub.doc.mapper.DocMapper;
import com.knowledgehub.doc.mapper.DownloadLogMapper;
import com.knowledgehub.doc.service.AnnotationService;
import com.knowledgehub.doc.service.CategoryService;
import com.knowledgehub.doc.service.DocService;
import com.knowledgehub.doc.service.FavoriteService;
import com.knowledgehub.doc.service.FileStorageService;
import com.knowledgehub.doc.util.MarkdownChunkUtil;
import com.knowledgehub.doc.vo.AnnotationVO;
import com.knowledgehub.doc.vo.BatchUploadResultVO;
import com.knowledgehub.doc.vo.DocDetailVO;
import com.knowledgehub.doc.vo.DocListVO;
import com.knowledgehub.framework.context.UserContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文档服务实现
 */
@Slf4j
@Service
public class DocServiceImpl implements DocService {

    @Resource
    private UserService userService;

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private AnnotationService annotationService;

    @Resource
    private DocMapper docMapper;

    @Resource
    private ChunkMapper chunkMapper;

    @Resource
    private DownloadLogMapper downloadLogMapper;

    @Override
    public Page<DocListVO> pageDocs(DocQueryDTO dto) {
        Page<Doc> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Doc> wrapper = Wrappers.<Doc>lambdaQuery()
                .eq(Doc::getStatus, DocConst.STATUS_NORMAL)
                .like(dto.getTitle() != null && !dto.getTitle().isBlank(), Doc::getTitle, dto.getTitle())
                .eq(dto.getCategoryId() != null, Doc::getCategoryId, dto.getCategoryId())
                .eq(dto.getUploaderId() != null, Doc::getUploaderId, dto.getUploaderId())
                .orderByDesc(Doc::getCreateTime);
        Page<Doc> docPage = docMapper.selectPage(page, wrapper);

        List<DocListVO> voList = toDocListVOList(docPage.getRecords());
        Page<DocListVO> voPage = new Page<>(docPage.getCurrent(), docPage.getSize(), docPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<DocListVO> pagePublicDocs(DocQueryDTO dto, Long currentUserId) {
        Page<Doc> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Doc> wrapper = Wrappers.<Doc>lambdaQuery()
                .eq(Doc::getStatus, DocConst.STATUS_NORMAL)
                .eq(Doc::getIsPublic, 1)
                .like(dto.getTitle() != null && !dto.getTitle().isBlank(), Doc::getTitle, dto.getTitle());
        if (dto.getCategoryId() != null) {
            // 文件夹详情页：查指定文件夹的文档
            wrapper.eq(Doc::getCategoryId, dto.getCategoryId());
        } else {
            // 首页：只查未归类的公开文档
            wrapper.isNull(Doc::getCategoryId);
        }
        wrapper.orderByDesc(Doc::getCreateTime);
        Page<Doc> docPage = docMapper.selectPage(page, wrapper);
        List<DocListVO> voList = toDocListVOList(docPage.getRecords());
        Page<DocListVO> voPage = new Page<>(docPage.getCurrent(), docPage.getSize(), docPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public DocDetailVO getDocDetail(Long id, Long currentUserId) {
        Doc doc = docMapper.selectById(id);
        if (doc == null || DocConst.STATUS_DELETED.equals(doc.getStatus())) {
            throw new BusinessException("文档不存在");
        }
        // 浏览量 +1（原子操作）
        docMapper.update(null, Wrappers.<Doc>lambdaUpdate()
                .setSql("view_count = view_count + 1")
                .eq(Doc::getId, id));

        DocDetailVO vo = new DocDetailVO();
        vo.setId(doc.getId());
        vo.setTitle(doc.getTitle());
        vo.setSummary(doc.getSummary());
        vo.setContent(doc.getContent());
        vo.setCategoryId(doc.getCategoryId());
        vo.setTags(doc.getTags());
        vo.setUploaderId(doc.getUploaderId());
        vo.setUploaderName(doc.getUploaderName());
        vo.setSourceType(doc.getSourceType());
        vo.setFilePath(doc.getFilePath());
        vo.setFileSize(doc.getFileSize());
        vo.setViewCount(doc.getViewCount());
        vo.setFavoriteCount(doc.getFavoriteCount());
        vo.setCreateTime(doc.getCreateTime());
        vo.setUpdateTime(doc.getUpdateTime());

        // 分类名
        if (doc.getCategoryId() != null) {
            Category category = categoryService.getById(doc.getCategoryId());
            vo.setCategoryName(category != null ? category.getName() : null);
        }

        // 是否已收藏
        if (currentUserId != null) {
            vo.setFavorited(favoriteService.isFavorited(id, currentUserId));
        } else {
            vo.setFavorited(false);
        }

        // 注释列表
        List<AnnotationVO> annotations = annotationService.listByDocId(id).stream()
                .map(this::toAnnotationVO)
                .collect(Collectors.toList());
        vo.setAnnotations(annotations);

        return vo;
    }

    @Override
    public Doc getById(Long id) {
        Doc doc = docMapper.selectById(id);
        if (doc == null || DocConst.STATUS_DELETED.equals(doc.getStatus())) {
            throw new BusinessException("文档不存在");
        }
        return doc;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long uploadDoc(DocUploadDTO dto, Long userId) {
        MultipartFile file = dto.getFile();
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BusinessException("标题不能为空");
        }
        // ① 上传文件到对象存储（先于写库；写库失败会留孤儿文件，后续可加清理补偿）
        String filePath = fileStorageService.upload(file, StorageConst.DIR_DOC);
        // ② 读取文件文本（统一 UTF-8，后续支持多编码）
        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BusinessException("读取文件内容失败");
        }
        // ③ 拿上传者昵称
        User user = userService.getById(userId);
        String nickname = user != null ? user.getNickname() : null;
        // ④ 组装 Doc
        Doc doc = new Doc();
        doc.setTitle(dto.getTitle());
        doc.setSummary(dto.getSummary());
        doc.setCategoryId(dto.getCategoryId());
        doc.setTags(dto.getTags());
        doc.setContent(content);
        doc.setUploaderId(userId);
        doc.setUploaderName(nickname);
        doc.setSourceType(DocConst.SOURCE_UPLOAD);
        doc.setFilePath(filePath);
        doc.setFileSize(file.getSize());
        doc.setAuditStatus(DocConst.AUDIT_APPROVED);
        doc.setStatus(DocConst.STATUS_NORMAL);
        doc.setViewCount(0);
        doc.setFavoriteCount(0);
        docMapper.insert(doc);
        // ⑤ 切片并批量插入
        List<String> chunks = MarkdownChunkUtil.splitIntoChunks(content);
        for (int i = 0; i < chunks.size(); i++) {
            Chunk chunk = new Chunk();
            chunk.setDocId(doc.getId());
            chunk.setChunkIndex(i);
            chunk.setContent(chunks.get(i));
            chunkMapper.insert(chunk);
        }
        return doc.getId();
    }

    @Override
    public BatchUploadResultVO uploadDocsBatch(MultipartFile[] files, Long categoryId, Long userId) {
        BatchUploadResultVO result = new BatchUploadResultVO();
        List<BatchUploadResultVO.FailItem> failList = new ArrayList<>();
        int successCount = 0;

        if (files == null || files.length == 0) {
            throw new BusinessException("请选择文件");
        }
        if (files.length > 20) {
            throw new BusinessException("单次最多上传 20 个文件");
        }

        for (MultipartFile file : files) {
            try {
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".md")) {
                    throw new BusinessException("只支持 .md 文件");
                }
                String title = originalFilename.substring(0, originalFilename.lastIndexOf("."));

                DocUploadDTO dto = new DocUploadDTO();
                dto.setFile(file);
                dto.setTitle(title);
                dto.setSummary("");
                dto.setCategoryId(categoryId);

                uploadDoc(dto, userId);
                successCount++;
            } catch (Exception e) {
                BatchUploadResultVO.FailItem item = new BatchUploadResultVO.FailItem();
                item.setFilename(file.getOriginalFilename());
                item.setReason(e.getMessage());
                failList.add(item);
            }
        }

        result.setSuccessCount(successCount);
        result.setFailCount(failList.size());
        result.setFailList(failList);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDoc(Long id, DocUpdateDTO dto, Long userId) {
        Doc doc = docMapper.selectById(id);
        if (doc == null || DocConst.STATUS_DELETED.equals(doc.getStatus())) {
            throw new BusinessException("文档不存在");
        }
        // 权限：上传者或管理员
        if (!userId.equals(doc.getUploaderId()) && !DocConst.ROLE_ADMIN.equals(UserContext.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限");
        }
        doc.setTitle(dto.getTitle());
        doc.setSummary(dto.getSummary());
        doc.setCategoryId(dto.getCategoryId());
        doc.setTags(dto.getTags());
        docMapper.updateById(doc);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDoc(Long id, Long userId) {
        Doc doc = docMapper.selectById(id);
        if (doc == null || DocConst.STATUS_DELETED.equals(doc.getStatus())) {
            throw new BusinessException("文档不存在");
        }
        if (!userId.equals(doc.getUploaderId()) && !DocConst.ROLE_ADMIN.equals(UserContext.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限");
        }
        // 软删除（不删 RustFS 文件和切片，审计留痕）
        doc.setStatus(DocConst.STATUS_DELETED);
        docMapper.updateById(doc);
    }

    @Override
    public byte[] downloadDoc(Long id, Long userId, String ip) {
        Doc doc = docMapper.selectById(id);
        if (doc == null || DocConst.STATUS_DELETED.equals(doc.getStatus())) {
            throw new BusinessException("文档不存在");
        }
        byte[] bytes = fileStorageService.download(doc.getFilePath());
        // 写下载记录（独立 try-catch，失败不影响下载）
        try {
            DownloadLog downloadLog = new DownloadLog();
            downloadLog.setDocId(id);
            downloadLog.setUserId(userId);
            downloadLog.setUserName(UserContext.getUsername());
            downloadLog.setIp(ip);
            downloadLogMapper.insert(downloadLog);
        } catch (Exception e) {
            log.error("写下载记录失败, docId={}", id, e);
        }
        return bytes;
    }

    @Override
    public Page<DocListVO> searchDocs(String keyword, Integer page, Integer size) {
        if (keyword == null || keyword.isBlank()) {
            return new Page<>(page, size);
        }
        Page<Doc> docPage = new Page<>(page, size);
        IPage<Doc> result = docMapper.searchDocs(docPage, keyword);
        List<DocListVO> voList = toDocListVOList(result.getRecords());
        Page<DocListVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public String getDocContent(Long docId) {
        Doc doc = docMapper.selectById(docId);
        return doc != null && doc.getContent() != null ? doc.getContent() : "";
    }

    @Override
    public List<String> getDocChunks(Long docId) {
        List<Chunk> chunks = chunkMapper.selectList(Wrappers.<Chunk>lambdaQuery()
                .eq(Chunk::getDocId, docId)
                .orderByAsc(Chunk::getChunkIndex));
        if (chunks != null && !chunks.isEmpty()) {
            return chunks.stream().map(Chunk::getContent).collect(Collectors.toList());
        }
        // 兜底：没切片，返回整篇内容
        String content = getDocContent(docId);
        if (content == null || content.isBlank()) {
            return List.of();
        }
        return List.of(content);
    }

    /**
     * Doc 实体列表转 DocListVO 列表（批量查分类名）
     */
    private List<DocListVO> toDocListVOList(List<Doc> docs) {
        Map<Long, String> categoryNameMap = categoryService.getNamesByIds(
                docs.stream().map(Doc::getCategoryId)
                        .filter(Objects::nonNull).collect(Collectors.toSet()));
        return docs.stream().map(doc -> {
            DocListVO vo = new DocListVO();
            vo.setId(doc.getId());
            vo.setTitle(doc.getTitle());
            vo.setSummary(doc.getSummary());
            vo.setCategoryName(categoryNameMap.get(doc.getCategoryId()));
            vo.setUploaderName(doc.getUploaderName());
            vo.setViewCount(doc.getViewCount());
            vo.setFavoriteCount(doc.getFavoriteCount());
            vo.setCreateTime(doc.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * Annotation 实体转 AnnotationVO（不暴露 status 字段）
     */
    private AnnotationVO toAnnotationVO(Annotation annotation) {
        AnnotationVO vo = new AnnotationVO();
        vo.setId(annotation.getId());
        vo.setUserId(annotation.getUserId());
        vo.setUserName(annotation.getUserName());
        vo.setContent(annotation.getContent());
        vo.setType(annotation.getType());
        vo.setCreateTime(annotation.getCreateTime());
        return vo;
    }
}
