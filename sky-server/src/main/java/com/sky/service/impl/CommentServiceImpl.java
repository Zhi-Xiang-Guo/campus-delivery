package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CommentPageQueryDTO;
import com.sky.dto.CommentReplyDTO;
import com.sky.dto.CommentSubmitDTO;
import com.sky.entity.Comment;
import com.sky.entity.CommentImage;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.CommentImageMapper;
import com.sky.mapper.CommentMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.result.PageResult;
import com.sky.service.CommentService;
import com.sky.vo.CommentAdminVO;
import com.sky.vo.CommentVO;
import com.sky.vo.OrderItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Value("${requestHeader}")
    private String requestHeader;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentImageMapper commentImageMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Override
    @Transactional
    public void submit(CommentSubmitDTO commentSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        Long orderId = commentSubmitDTO.getOrderId();

        Orders orders = orderMapper.getByOrderId(orderId);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (orders.getUserId() == null || !orders.getUserId().equals(userId)) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_BELONG_TO_USER);
        }
        if (!Orders.COMPLETED.equals(orders.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_COMPLETED);
        }
        if (orders.getIsCommented() != null && orders.getIsCommented() == 1) {
            throw new OrderBusinessException(MessageConstant.ORDER_ALREADY_COMMENTED);
        }

        Comment comment = Comment.builder()
                .orderId(orderId)
                .userId(userId)
                .score(commentSubmitDTO.getScore())
                .content(commentSubmitDTO.getContent())
                .status(1)
                .createTime(LocalDateTime.now())
                .build();
        commentMapper.insert(comment);

        if (commentSubmitDTO.getImages() != null && !commentSubmitDTO.getImages().isEmpty()) {
            for (String url : commentSubmitDTO.getImages()) {
                CommentImage image = CommentImage.builder()
                        .commentId(comment.getId())
                        .url(normalizeImageUrl(url))
                        .build();
                commentImageMapper.insert(image);
            }
        }

        Orders updateOrder = Orders.builder()
                .id(orderId)
                .isCommented(1)
                .build();
        orderMapper.update(updateOrder);
    }

    @Override
    public PageResult pageQuery4User(int page, int pageSize) {
        Long userId = BaseContext.getCurrentId();
        PageHelper.startPage(page, pageSize);
        Page<Comment> pageResult = (Page<Comment>) commentMapper.pageQueryByUserId(userId);

        List<CommentVO> voList = new ArrayList<>();
        for (Comment comment : pageResult) {
            CommentVO vo = buildCommentVO(comment);
            voList.add(vo);
        }

        return new PageResult(pageResult.getTotal(), voList);
    }

    @Override
    public PageResult pageQuery4Admin(CommentPageQueryDTO commentPageQueryDTO) {
        PageHelper.startPage(commentPageQueryDTO.getPage(), commentPageQueryDTO.getPageSize());
        Page<CommentAdminVO> pageResult = (Page<CommentAdminVO>) commentMapper.pageQuery(commentPageQueryDTO);

        for (CommentAdminVO vo : pageResult) {
            List<CommentImage> images = commentImageMapper.getByCommentId(vo.getId());
            if (images != null && !images.isEmpty()) {
                vo.setImages(images.stream().map(CommentImage::getUrl).map(this::normalizeImageUrl).collect(Collectors.toList()));
            }
        }

        return new PageResult(pageResult.getTotal(), pageResult);
    }

    @Override
    public void reply(CommentReplyDTO commentReplyDTO) {
        Comment comment = Comment.builder()
                .id(commentReplyDTO.getCommentId())
                .replyContent(commentReplyDTO.getReplyContent())
                .replyTime(LocalDateTime.now())
                .build();
        commentMapper.updateReply(comment);
    }

    private CommentVO buildCommentVO(Comment comment) {
        List<CommentImage> images = commentImageMapper.getByCommentId(comment.getId());
        List<String> imageUrls = images != null
                ? images.stream().map(CommentImage::getUrl).map(this::normalizeImageUrl).collect(Collectors.toList())
                : new ArrayList<>();

        List<OrderDetail> orderDetails = orderDetailMapper.getById(comment.getOrderId());
        List<OrderItemVO> orderItems = orderDetails != null
                ? orderDetails.stream()
                        .map(d -> OrderItemVO.builder()
                                .name(d.getName())
                                .image(d.getImage())
                                .number(d.getNumber())
                                .build())
                        .collect(Collectors.toList())
                : new ArrayList<>();

        return CommentVO.builder()
                .id(comment.getId())
                .orderId(comment.getOrderId())
                .score(comment.getScore())
                .content(comment.getContent())
                .createTime(comment.getCreateTime())
                .replyContent(comment.getReplyContent())
                .replyTime(comment.getReplyTime())
                .images(imageUrls)
                .orderItems(orderItems)
                .build();
    }

    private String normalizeImageUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }

        String trimmedUrl = url.trim();
        if (trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://")) {
            if (trimmedUrl.startsWith(getBaseImageUrl())) {
                return trimmedUrl;
            }
            String compatibleImageUrl = extractCompatibleImageUrl(trimmedUrl);
            return compatibleImageUrl != null ? compatibleImageUrl : trimmedUrl;
        }
        return toCompatibleImageUrl(trimmedUrl);
    }

    private String extractCompatibleImageUrl(String url) {
        try {
            URI uri = new URI(url);
            return toCompatibleImageUrl(uri.getPath());
        } catch (URISyntaxException e) {
            log.warn("解析图片地址失败，使用原始地址：{}", url, e);
            return null;
        }
    }

    private String toCompatibleImageUrl(String path) {
        if (path == null || path.trim().isEmpty()) {
            return path;
        }

        String normalizedPath = path.trim();
        int imageIndex = normalizedPath.indexOf("/images/");
        if (imageIndex >= 0) {
            return getBaseImageUrl() + normalizedPath.substring(imageIndex + "/images/".length());
        }
        if (normalizedPath.startsWith("images/")) {
            return getBaseImageUrl() + normalizedPath.substring("images/".length());
        }
        int lastSlashIndex = normalizedPath.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < normalizedPath.length() - 1) {
            normalizedPath = normalizedPath.substring(lastSlashIndex + 1);
        }
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        return getBaseImageUrl() + normalizedPath;
    }

    private String getBaseImageUrl() {
        return requestHeader.endsWith("/") ? requestHeader : requestHeader + "/";
    }
}
