package com.xuecheng.media.controller;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.service.MediaFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/open")
public class OpenController {

    @Autowired
    private MediaFileService mediaFileService;

    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> preview(@PathVariable String mediaId){
        String url = mediaFileService.preview(mediaId);
        return RestResponse.success(url);
    }

}
