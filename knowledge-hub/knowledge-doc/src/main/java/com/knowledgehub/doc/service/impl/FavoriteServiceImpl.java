package com.knowledgehub.doc.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.knowledgehub.common.constant.DocConst;
import com.knowledgehub.common.exception.BusinessException;
import com.knowledgehub.doc.entity.Doc;
import com.knowledgehub.doc.entity.Favorite;
import com.knowledgehub.doc.mapper.DocMapper;
import com.knowledgehub.doc.mapper.FavoriteMapper;
import com.knowledgehub.doc.service.CategoryService;
import com.knowledgehub.doc.service.FavoriteService;
import com.knowledgehub.doc.vo.DocListVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 收藏服务实现
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private DocMapper docMapper;

    @Resource
    private CategoryService categoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFavorite(Long docId, Long userId) {
        Doc doc = docMapper.selectById(docId);
        if (doc == null || DocConst.STATUS_DELETED.equals(doc.getStatus())) {
            throw new BusinessException("文档不存在");
        }
        // 幂等：已收藏直接返回
        if (isFavorited(docId, userId)) {
            return;
        }
        Favorite favorite = new Favorite();
        favorite.setDocId(docId);
        favorite.setUserId(userId);
        favoriteMapper.insert(favorite);
        // 收藏数 +1（原子）
        docMapper.update(null, Wrappers.<Doc>lambdaUpdate()
                .setSql("favorite_count = favorite_count + 1")
                .eq(Doc::getId, docId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFavorite(Long docId, Long userId) {
        // 删除收藏记录（不存在也不报错）
        favoriteMapper.delete(Wrappers.<Favorite>lambdaQuery()
                .eq(Favorite::getDocId, docId)
                .eq(Favorite::getUserId, userId));
        // 收藏数 -1（不能小于 0）
        docMapper.update(null, Wrappers.<Doc>lambdaUpdate()
                .setSql("favorite_count = GREATEST(favorite_count - 1, 0)")
                .eq(Doc::getId, docId));
    }

    @Override
    public boolean isFavorited(Long docId, Long userId) {
        return favoriteMapper.selectCount(Wrappers.<Favorite>lambdaQuery()
                .eq(Favorite::getDocId, docId)
                .eq(Favorite::getUserId, userId)) > 0;
    }

    @Override
    public Page<DocListVO> pageMyFavorites(Long userId, Integer page, Integer size) {
        IPage<Doc> docPage = favoriteMapper.selectFavoriteDocs(new Page<>(page, size), userId);

        Set<Long> categoryIds = docPage.getRecords().stream()
                .map(Doc::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = categoryService.getNamesByIds(categoryIds);

        List<DocListVO> voList = docPage.getRecords().stream().map(doc -> {
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

        Page<DocListVO> voPage = new Page<>(docPage.getCurrent(), docPage.getSize(), docPage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }
}
