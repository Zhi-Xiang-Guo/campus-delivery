package com.sky.service;

import com.sky.dto.CommentPageQueryDTO;
import com.sky.dto.CommentReplyDTO;
import com.sky.dto.CommentSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.CommentVO;

public interface CommentService {

    void submit(CommentSubmitDTO commentSubmitDTO);

    PageResult pageQuery4User(int page, int pageSize);

    PageResult pageQuery4Admin(CommentPageQueryDTO commentPageQueryDTO);

    void reply(CommentReplyDTO commentReplyDTO);
}
