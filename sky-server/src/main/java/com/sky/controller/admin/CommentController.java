package com.sky.controller.admin;

import com.sky.dto.CommentPageQueryDTO;
import com.sky.dto.CommentReplyDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/comment")
@Slf4j
@Api(tags = "评论管理接口")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/page")
    @ApiOperation("管理端评论分页查询")
    public Result<PageResult> page(CommentPageQueryDTO commentPageQueryDTO) {
        PageResult pageResult = commentService.pageQuery4Admin(commentPageQueryDTO);
        return Result.success(pageResult);
    }

    @PutMapping("/reply")
    @ApiOperation("商家回复评论")
    public Result reply(@RequestBody CommentReplyDTO commentReplyDTO) {
        log.info("商家回复评论：{}", commentReplyDTO);
        commentService.reply(commentReplyDTO);
        return Result.success();
    }
}
