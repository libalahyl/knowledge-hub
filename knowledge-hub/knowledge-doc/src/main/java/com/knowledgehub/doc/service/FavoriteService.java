package com.knowledgehub.doc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.doc.vo.DocListVO;

/**
 * 收藏服务接口
 */
public interface FavoriteService {

    void addFavorite(Long docId, Long userId);

    void removeFavorite(Long docId, Long userId);

    boolean isFavorited(Long docId, Long userId);

    Page<DocListVO> pageMyFavorites(Long userId, Integer page, Integer size);
}
