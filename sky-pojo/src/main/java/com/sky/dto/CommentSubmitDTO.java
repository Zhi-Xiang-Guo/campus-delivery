package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CommentSubmitDTO implements Serializable {

    private Long orderId;

    private Integer score;

    private String content;

    private List<String> images;
}
