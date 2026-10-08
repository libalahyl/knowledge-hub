package com.knowledgehub.search.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.doc.service.DocService;
import com.knowledgehub.doc.vo.DocListVO;
import com.knowledgehub.search.service.SearchLogService;
import com.knowledgehub.search.service.SearchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 搜索服务实现
 */
@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private DocService docService;

    @Resource
    private SearchLogService searchLogService;

    @Override
    public Page<DocListVO> search(String keyword, Integer page, Integer size, Long userId) {
        // 记录搜索历史（失败不影响搜索）
        try {
            searchLogService.record(keyword, userId);
        } catch (Exception e) {
            log.error("记录搜索历史失败", e);
        }
        // 转发给 doc 模块
        return docService.searchDocs(keyword, page, size);
    }
}
