package com.knowledgehub.search.service.impl;

import com.knowledgehub.search.entity.SearchLog;
import com.knowledgehub.search.mapper.SearchLogMapper;
import com.knowledgehub.search.service.SearchLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 搜索历史服务实现
 */
@Service
public class SearchLogServiceImpl implements SearchLogService {

    @Resource
    private SearchLogMapper searchLogMapper;

    @Override
    public void record(String keyword, Long userId) {
        // 空关键词不记录
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        SearchLog log = new SearchLog();
        log.setKeyword(keyword);
        log.setUserId(userId);
        searchLogMapper.insert(log);
    }
}
