package com.xuecheng.content.controller;

import com.xuecheng.base.model.result.Result;
import com.xuecheng.content.model.pojo.dto.AddTeachPlanDTO;
import com.xuecheng.content.model.pojo.dto.BindTeachPlanMediaDTO;
import com.xuecheng.content.model.pojo.vo.TeachPlanVO;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
    public Result<String> add(@RequestBody AddTeachPlanDTO addTeachPlanDTO){
        log.info("添加课程计划");
        teachplanService.add(addTeachPlanDTO);
        return Result.success("添加教学计划成功",null);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id){
        teachplanService.delete(id);
        return Result.success("删除教学计划成功",null);
    }

    @PostMapping("/{type}/{id}")
    public Result<String> move(@PathVariable String type,@PathVariable Long id){
        log.info("移动教学计划位置");
        teachplanService.move(type,id);
        return Result.success("移动教学计划位置成功",null);
    }

    @PostMapping("/association/media")
    public void bindMedia(@RequestBody @Validated BindTeachPlanMediaDTO bindTeachPlanMediaDTO){
        log.info("教学计划绑定媒资");
        teachplanService.bindMedia(bindTeachPlanMediaDTO);
    }

    @DeleteMapping("/association/media/{teachPlanId}/{mediaId}")
    public void removeBind(@PathVariable Long teachPlanId,@PathVariable Long mediaId){
        log.info("教学计划解除绑定媒资");
        teachplanService.removeBind(teachPlanId,mediaId);
    }

}
