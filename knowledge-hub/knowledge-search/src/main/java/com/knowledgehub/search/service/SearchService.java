package com.knowledgehub.search.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.doc.vo.DocListVO;

/**
 * 搜索服务接口
 */
public interface SearchService {

    /**
     * 关键词搜索（转发给 doc 模块 + 记录搜索历史）
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    每页条数
     * @param userId  用户ID（可空，游客也能搜）
     */
    Page<DocListVO> search(String keyword, Integer page, Integer size, Long userId);
}
