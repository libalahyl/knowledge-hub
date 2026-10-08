package com.knowledgehub.doc.service;

import com.knowledgehub.doc.entity.Category;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 分类服务接口
 */
public interface CategoryService {

    List<Category> listAll();

    Category getById(Long id);

    Long create(String name, Integer sort);

    void update(Long id, String name, Integer sort);

    void delete(Long id);

    /**
     * 批量查分类名
     *
     * @param ids 分类 ID 集合
     * @return id -> name 映射
     */
    Map<Long, String> getNamesByIds(Set<Long> ids);

    /**
     * 创建用户自己的文件夹
     */
    Long createMyFolder(Long userId, String name, Integer isPublic);

    /**
     * 查我的文件夹列表
     */
    List<Category> listMyFolders(Long userId);

    /**
     * 删除我的文件夹（校验归属）
     */
    void deleteMyFolder(Long id, Long userId);

    /**
     * 编辑我的文件夹（改名称/公开性，校验归属）
     */
    void updateMyFolder(Long id, Long userId, String name, Integer isPublic);

    /**
     * 查所有公开的文件夹（给首页展示用）
     */
    List<Category> listPublicFolders();

    /**
     * 查当前用户可见的分类（系统预置 + 公开文件夹 + 自己的文件夹）
     * 用于首页"全部分类"下拉框，避免泄露他人的私有文件夹
     */
    List<Category> listVisibleFolders(Long userId);
}
