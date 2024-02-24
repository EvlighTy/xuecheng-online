package com.xuecheng.content.controller;

import com.xuecheng.content.model.pojo.dto.BindTeachPlanMediaDTO;
import com.xuecheng.content.service.TeachplanMediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author itcast
 */
@Slf4j
@RestController
@RequestMapping("teachplanMedia")
public class TeachplanMediaController {

    @Autowired
    private TeachplanMediaService  teachplanMediaService;

}
