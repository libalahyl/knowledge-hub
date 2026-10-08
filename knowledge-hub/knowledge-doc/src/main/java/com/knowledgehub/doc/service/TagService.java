package com.knowledgehub.doc.service;

import com.knowledgehub.doc.entity.Tag;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService {

    List<Tag> listAll();

    List<Tag> listByDocId(Long docId);

    void bindTags(Long docId, List<Long> tagIds);
}
