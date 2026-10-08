package com.knowledgehub.doc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledgehub.doc.entity.Doc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DocMapper extends BaseMapper<Doc> {

    /**
     * 关键词搜索：标题命中优先，参数化防注入
     */
    @Select("SELECT * FROM knowledge_doc " +
            "WHERE status = 1 " +
            "AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY CASE WHEN title LIKE CONCAT('%', #{keyword}, '%') THEN 0 ELSE 1 END ASC, create_time DESC")
    IPage<Doc> searchDocs(IPage<Doc> page, @Param("keyword") String keyword);
}
