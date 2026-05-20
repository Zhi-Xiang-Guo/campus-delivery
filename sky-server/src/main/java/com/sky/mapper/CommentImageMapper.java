package com.sky.mapper;

import com.sky.entity.CommentImage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentImageMapper {

    @Insert("INSERT into comment_image (comment_id, url) VALUES (#{commentId}, #{url})")
    void insert(CommentImage commentImage);

    @Select("select * from comment_image where comment_id = #{commentId}")
    List<CommentImage> getByCommentId(Long commentId);
}
