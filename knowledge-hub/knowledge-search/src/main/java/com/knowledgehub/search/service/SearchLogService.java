package com.knowledgehub.search.service;

/**
 * 搜索历史服务接口
 */
public interface SearchLogService {

    /**
     * 记录一次搜索
     *
     * @param keyword 关键词（空则不记录）
     * @param userId  用户ID（可空）
     */
    void record(String keyword, Long userId);
}
