package com.knowledgehub.doc.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knowledgehub.common.constant.DocConst;
import com.knowledgehub.common.exception.BusinessException;
import com.knowledgehub.common.result.ResultCode;
import com.knowledgehub.doc.entity.Category;
import com.knowledgehub.doc.entity.Doc;
import com.knowledgehub.doc.mapper.CategoryMapper;
import com.knowledgehub.doc.mapper.DocMapper;
import com.knowledgehub.doc.service.CategoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 分类服务实现
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private DocMapper docMapper;

    @Override
    public List<Category> listAll() {
        return categoryMapper.selectList(Wrappers.<Category>lambdaQuery().orderByAsc(Category::getSort));
    }

    @Override
    public Category getById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public Long create(String name, Integer sort) {
        Long count = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery().eq(Category::getName, name));
        if (count > 0) {
            throw new BusinessException("分类名已存在");
        }
        Category category = new Category();
        category.setName(name);
        category.setSort(sort == null ? 0 : sort);
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void update(Long id, String name, Integer sort) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        // 重名校验（排除自己）
        Long count = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery()
                .eq(Category::getName, name)
                .ne(Category::getId, id));
        if (count > 0) {
            throw new BusinessException("分类名已存在");
        }
        category.setName(name);
        category.setSort(sort);
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        // 该分类下还有正常文档则不能删
        Long docCount = docMapper.selectCount(Wrappers.<Doc>lambdaQuery()
                .eq(Doc::getCategoryId, id)
                .eq(Doc::getStatus, DocConst.STATUS_NORMAL));
        if (docCount > 0) {
            throw new BusinessException("该分类下还有文档，不能删除");
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public Map<Long, String> getNamesByIds(Set<Long> ids) {
        Map<Long, String> map = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return map;
        }
        List<Category> categories = categoryMapper.selectBatchIds(ids);
        for (Category c : categories) {
            map.put(c.getId(), c.getName());
        }
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMyFolder(Long userId, String name, Integer isPublic) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("文件夹名不能为空");
        }
        // 重名校验（同一用户下不能有同名文件夹）
        Long dupCount = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery()
                .eq(Category::getOwnerId, userId)
                .eq(Category::getName, name));
        if (dupCount > 0) {
            throw new BusinessException("你已有同名文件夹");
        }
        // 空文件夹上限 3 个
        List<Category> myFolders = categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .eq(Category::getOwnerId, userId));
        long emptyCount = 0;
        for (Category folder : myFolders) {
            Long docCount = docMapper.selectCount(Wrappers.<Doc>lambdaQuery()
                    .eq(Doc::getCategoryId, folder.getId())
                    .eq(Doc::getStatus, DocConst.STATUS_NORMAL));
            if (docCount == 0) {
                emptyCount++;
            }
        }
        if (emptyCount >= 3) {
            throw new BusinessException("空文件夹最多 3 个，请先往已有文件夹添加文档");
        }
        Category category = new Category();
        category.setName(name);
        category.setOwnerId(userId);
        category.setIsPublic(isPublic == null ? 1 : isPublic);
        category.setSort(0);
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public List<Category> listMyFolders(Long userId) {
        return categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .eq(Category::getOwnerId, userId)
                .orderByAsc(Category::getSort)
                .orderByDesc(Category::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMyFolder(Long id, Long userId) {
        Category category = categoryMapper.selectById(id);
        if (category == null || !userId.equals(category.getOwnerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权删除该文件夹");
        }
        Long docCount = docMapper.selectCount(Wrappers.<Doc>lambdaQuery()
                .eq(Doc::getCategoryId, id)
                .eq(Doc::getStatus, DocConst.STATUS_NORMAL));
        if (docCount > 0) {
            throw new BusinessException("文件夹下还有文档，请先移除或移动文档");
        }
        categoryMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMyFolder(Long id, Long userId, String name, Integer isPublic) {
        Category category = categoryMapper.selectById(id);
        if (category == null || !userId.equals(category.getOwnerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权修改该文件夹");
        }
        if (name != null && !name.isBlank()) {
            Long dupCount = categoryMapper.selectCount(Wrappers.<Category>lambdaQuery()
                    .eq(Category::getOwnerId, userId)
                    .eq(Category::getName, name)
                    .ne(Category::getId, id));
            if (dupCount > 0) {
                throw new BusinessException("你已有同名文件夹");
            }
            category.setName(name);
        }
        if (isPublic != null) {
            category.setIsPublic(isPublic);
        }
        categoryMapper.updateById(category);
    }

    @Override
    public List<Category> listPublicFolders() {
        return categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .eq(Category::getIsPublic, 1)
                .orderByAsc(Category::getSort)
                .orderByDesc(Category::getCreateTime));
    }

    @Override
    public List<Category> listVisibleFolders(Long userId) {
        return categoryMapper.selectList(Wrappers.<Category>lambdaQuery()
                .and(w -> w.isNull(Category::getOwnerId)          // 系统预置分类
                        .or().eq(Category::getIsPublic, 1)        // 公开文件夹
                        .or().eq(Category::getOwnerId, userId))   // 自己建的文件夹
                .orderByAsc(Category::getSort)
                .orderByDesc(Category::getCreateTime));
    }
}
