package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentAdminVO implements Serializable {

    private Long id;

    private String orderNumber;

    private String userName;

    private String userAvatar;

    private Integer score;

    private String content;

    private LocalDateTime createTime;

    private String replyContent;

    private LocalDateTime replyTime;

    private List<String> images;
}
