package com.knowledgehub.doc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.knowledgehub.doc.entity.Doc;
import com.knowledgehub.doc.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 联查我的收藏文档（分页）
     */
    @Select("SELECT d.* FROM knowledge_doc d " +
            "INNER JOIN knowledge_favorite f ON d.id = f.doc_id " +
            "WHERE f.user_id = #{userId} AND d.status = 1 " +
            "ORDER BY f.create_time DESC")
    IPage<Doc> selectFavoriteDocs(IPage<Doc> page, @Param("userId") Long userId);
}
