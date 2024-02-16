package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.pojo.dto.AddTeachPlanDTO;
import com.xuecheng.content.model.pojo.entity.Teachplan;
import com.xuecheng.content.model.pojo.vo.TeachPlanVO;

import java.util.List;

/**
 * <p>
 * 课程计划 服务类
 * </p>
 *
 * @author itcast
 * @since 2024-02-11
 */
public interface TeachplanService extends IService<Teachplan> {

    List<TeachPlanVO> getList(Long id);

    void add(AddTeachPlanDTO addTeachPlanDTO);
}
