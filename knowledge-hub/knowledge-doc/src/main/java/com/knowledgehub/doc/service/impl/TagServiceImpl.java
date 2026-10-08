package com.knowledgehub.doc.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.knowledgehub.doc.entity.DocTag;
import com.knowledgehub.doc.entity.Tag;
import com.knowledgehub.doc.mapper.DocTagMapper;
import com.knowledgehub.doc.mapper.TagMapper;
import com.knowledgehub.doc.service.TagService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务实现
 */
@Service
public class TagServiceImpl implements TagService {

    @Resource
    private TagMapper tagMapper;

    @Resource
    private DocTagMapper docTagMapper;

    @Override
    public List<Tag> listAll() {
        return tagMapper.selectList(Wrappers.<Tag>lambdaQuery().orderByDesc(Tag::getCreateTime));
    }

    @Override
    public List<Tag> listByDocId(Long docId) {
        List<Long> tagIds = docTagMapper.selectList(Wrappers.<DocTag>lambdaQuery().eq(DocTag::getDocId, docId))
                .stream().map(DocTag::getTagId).collect(Collectors.toList());
        if (tagIds.isEmpty()) {
            return List.of();
        }
        return tagMapper.selectBatchIds(tagIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindTags(Long docId, List<Long> tagIds) {
        // 先删该文档所有旧标签
        docTagMapper.delete(Wrappers.<DocTag>lambdaQuery().eq(DocTag::getDocId, docId));
        // 再批量插入新标签
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            DocTag docTag = new DocTag();
            docTag.setDocId(docId);
            docTag.setTagId(tagId);
            docTagMapper.insert(docTag);
        }
    }
}
