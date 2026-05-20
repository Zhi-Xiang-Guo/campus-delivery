package com.sky.mapper;

import com.sky.dto.CommentPageQueryDTO;
import com.sky.entity.Comment;
import com.sky.vo.CommentAdminVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper {

    @Insert("INSERT into comment (order_id, user_id, score, content, status, create_time) " +
            "VALUES (#{orderId}, #{userId}, #{score}, #{content}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Comment comment);

    @Select("select * from comment where order_id = #{orderId}")
    Comment getByOrderId(Long orderId);

    @Select("select * from comment where id = #{id}")
    Comment getById(Long id);

    @Select("select * from comment where user_id = #{userId} order by create_time desc")
    List<Comment> pageQueryByUserId(Long userId);

    List<CommentAdminVO> pageQuery(CommentPageQueryDTO commentPageQueryDTO);

    void updateReply(Comment comment);
}
