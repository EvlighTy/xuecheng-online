package com.xuecheng.content.controller;

import com.xuecheng.content.model.pojo.dto.AddTeachPlanDTO;
import com.xuecheng.content.model.pojo.vo.TeachPlanVO;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 课程计划 前端控制器
 * </p>
 *
 * @author itcast
 */
@Slf4j
@RestController
@RequestMapping("teachplan")
public class TeachplanController {

    @Autowired
    private TeachplanService  teachplanService;

    @GetMapping("/{id}/tree-nodes")
    public List<TeachPlanVO> getlist(@PathVariable Long id){
        log.info("查询课程计划");
        List<TeachPlanVO> teachPlanVOS = teachplanService.getList(id);
        return teachPlanVOS;
    }

    @PostMapping
    public void add(@RequestBody AddTeachPlanDTO addTeachPlanDTO){
        log.info("添加课程计划");
        teachplanService.add(addTeachPlanDTO);
    }

}
