package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentReplyDTO implements Serializable {

    private Long commentId;

    private String replyContent;
}
