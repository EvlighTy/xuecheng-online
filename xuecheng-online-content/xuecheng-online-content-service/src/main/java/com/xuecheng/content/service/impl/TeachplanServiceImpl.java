package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.pojo.dto.AddTeachPlanDTO;
import com.xuecheng.content.model.pojo.entity.Teachplan;
import com.xuecheng.content.model.pojo.vo.TeachPlanVO;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 课程计划 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class TeachplanServiceImpl extends ServiceImpl<TeachplanMapper, Teachplan> implements TeachplanService {

    @Autowired
    private TeachplanMapper teachplanMapper;

    //分页查询课程计划
    @Override
    public List<TeachPlanVO> getList(Long id) {
        List<TeachPlanVO> teachPlanVOS = teachplanMapper.getList(id);
        return teachPlanVOS;
    }

    //添加or修改课程计划
    @Transactional
    @Override
    public void add(AddTeachPlanDTO addTeachPlanDTO) {
        Teachplan teachplan = BeanUtil.copyProperties(addTeachPlanDTO, Teachplan.class);
        if(teachplan.getId()==null){
            //如果是添加则计算排序字段值
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<Teachplan>()
                    .eq(Teachplan::getCourseId, teachplan.getCourseId())
                    .eq(Teachplan::getParentid, teachplan.getParentid());
            List<Teachplan> teachplans = list(queryWrapper);
            int orderBy = teachplans.stream().mapToInt(Teachplan::getOrderby).max().orElse(0)+1;
            teachplan.setOrderby(orderBy);
        }
        saveOrUpdate(teachplan);
    }
}
