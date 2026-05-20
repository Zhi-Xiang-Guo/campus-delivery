package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.utils.LocalFileUploadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController("ClientCommonController")
@RequestMapping("/user/common")
@Api(tags = "C端-通用接口")
@Slf4j
public class CommonController {

    @Value("${file.upload-path}")
    private String imgUrl;
    @Value("${requestHeader}")
    private String requestHeader;

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("用户文件上传：{}",file);
        return Result.success(LocalFileUploadUtil.upload(file, imgUrl, requestHeader));
    }
}
