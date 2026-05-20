package com.sky.controller.user;

import com.sky.dto.CommentSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("ClientCommentController")
@RequestMapping("/user/comment")
@Api("C端-评论相关api")
@Slf4j
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/submit")
    @ApiOperation("提交评论")
    public Result submit(@RequestBody CommentSubmitDTO commentSubmitDTO) {
        log.info("用户提交评论：{}", commentSubmitDTO);
        commentService.submit(commentSubmitDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("查询我的评论列表")
    public Result<PageResult> page(int page, int pageSize) {
        PageResult pageResult = commentService.pageQuery4User(page, pageSize);
        return Result.success(pageResult);
    }
}
