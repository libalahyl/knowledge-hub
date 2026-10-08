package com.knowledgehub.doc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.doc.dto.DocQueryDTO;
import com.knowledgehub.doc.dto.DocUpdateDTO;
import com.knowledgehub.doc.dto.DocUploadDTO;
import com.knowledgehub.doc.entity.Doc;
import com.knowledgehub.doc.vo.BatchUploadResultVO;
import com.knowledgehub.doc.vo.DocDetailVO;
import com.knowledgehub.doc.vo.DocListVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档服务接口
 */
public interface DocService {

    /**
     * 分页查询文档列表
     */
    Page<DocListVO> pageDocs(DocQueryDTO dto);

    /**
     * 公开文档列表（公开的 + 自己的私有文档），给首页用
     */
    Page<DocListVO> pagePublicDocs(DocQueryDTO dto, Long currentUserId);

    /**
     * 查询文档详情（浏览量 +1）
     */
    DocDetailVO getDocDetail(Long id, Long currentUserId);

    /**
     * 根据 ID 查询文档（不增加浏览量，供下载等场景使用）
     */
    Doc getById(Long id);

    /**
     * 上传文档（写库 + 切片）
     */
    Long uploadDoc(DocUploadDTO dto, Long userId);

    /**
     * 批量上传文档（每个文件标题=文件名去 .md 后缀，摘要留空）
     */
    BatchUploadResultVO uploadDocsBatch(MultipartFile[] files, Long categoryId, Long userId);

    /**
     * 更新文档（仅上传者或管理员）
     */
    void updateDoc(Long id, DocUpdateDTO dto, Long userId);

    /**
     * 删除文档（软删除，仅上传者或管理员）
     */
    void deleteDoc(Long id, Long userId);

    /**
     * 下载文档（返回原始 .md 字节）
     */
    byte[] downloadDoc(Long id, Long userId, String ip);

    /**
     * 关键词搜索（标题 + 内容，标题命中优先）
     */
    Page<DocListVO> searchDocs(String keyword, Integer page, Integer size);

    /**
     * 获取文档原文（供 ai 模块调用，不存在返回空串）
     */
    String getDocContent(Long docId);

    /**
     * 获取文档切片（供 ai 模块调用）
     */
    List<String> getDocChunks(Long docId);
}
