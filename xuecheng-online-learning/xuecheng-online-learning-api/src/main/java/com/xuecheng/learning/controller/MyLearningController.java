package com.xuecheng.learning.controller;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MyLearningController {

    @Autowired
    private LearningService learningService;

    @GetMapping("/open/learn/getvideo/{courseId}/{teachplanId}/{mediaId}")
    public RestResponse<String> getVideo(@PathVariable("courseId") Long courseId,
                                         @PathVariable("teachplanId") Long teachplanId,
                                         @PathVariable("mediaId") String mediaId) {
        log.info("用户查看视频");
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = null;
        if(user!=null) userId=user.getId();
        return learningService.getVideo(userId, courseId, teachplanId, mediaId);
    }

}
