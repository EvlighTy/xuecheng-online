package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.exmsg.CommonExMsg;
import com.xuecheng.base.exmsg.CourseExMsg;
import com.xuecheng.base.exmsg.TeachPlanExMsg;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.pojo.dto.AddTeachPlanDTO;
import com.xuecheng.content.model.pojo.dto.BindTeachPlanMediaDTO;
import com.xuecheng.content.model.pojo.entity.Teachplan;
import com.xuecheng.content.model.pojo.entity.TeachplanMedia;
import com.xuecheng.content.model.pojo.vo.TeachPlanVO;
import com.xuecheng.content.service.TeachplanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
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

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    //分页查询课程计划
    @Override
    public List<TeachPlanVO> getList(Long id) {
        return teachplanMapper.getList(id);
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

    //删除课程计划
    @Transactional
    @Override
    public void delete(Long id) {
        Teachplan teachplan = getById(id);
        if(teachplan.getParentid()==0){
            //大章节
            //业务逻辑校验（大章节下有小章节无法删除）
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<Teachplan>()
                    .eq(Teachplan::getParentid, id);
            boolean exists = teachplanMapper.exists(queryWrapper);
            if(exists) throw new CustomException(CourseExMsg.CHILD_CHAPTER_EXIST);
            removeById(id);
        }else {
            //小章节
            removeById(id);
            //删除媒资信息
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<TeachplanMedia>()
                    .eq(TeachplanMedia::getTeachplanId, teachplan.getId());
            teachplanMediaMapper.delete(queryWrapper);
        }
    }

    //移动教学计划位置
    @Transactional
    @Override
    public void move(String type, Long id) {
        Teachplan teachplan = getById(id);
        Long parentid = teachplan.getParentid();
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getParentid, parentid);
        List<Teachplan> teachplans = list(queryWrapper);
        //业务逻辑校验（只存在一个子章节不执行任何操作）
        if (teachplans.size()>1){
            teachplans.sort(Comparator.comparingInt(Teachplan::getOrderby));
            int index = binarySearch(teachplans, teachplan.getOrderby());
            Teachplan teachplan1 = teachplans.get(index);
            Teachplan teachplan2;
            if(type.equals("moveup")){
                //业务逻辑校验（子章节为第一个章节无法上移）
                if(index==0) return;
                //向上移动
                teachplan2 = teachplans.get(index - 1);
                swap(teachplan1,teachplan2);
            }else {
                //业务逻辑校验（子章节为最后一个章节无法下移）
                if(index==teachplans.size()-1) return;
                //向下移动
                teachplan2 = teachplans.get(index + 1);
                swap(teachplan1,teachplan2);
            }
            updateBatchById(Arrays.asList(teachplan1,teachplan2));
        }
    }

    //课程计划绑定媒资
    @Transactional
    @Override
    public void bindMedia(BindTeachPlanMediaDTO bindTeachPlanMediaDTO) {
        /*业务逻辑校验(课程存在)*/
        Teachplan teachplan = getById(bindTeachPlanMediaDTO.getTeachplanId());
        if(teachplan==null) throw new CustomException(TeachPlanExMsg.TEACH_PLAN_NO_EXIST);
        /*业务逻辑校验(只允许第二级教学计划绑定媒资文件)*/
        if(teachplan.getGrade()!=2) throw new CustomException(TeachPlanExMsg.BIND_GRADE_2_ONLY);
        //删除原信息
        LambdaQueryWrapper<TeachplanMedia> queryWrapper1 = new LambdaQueryWrapper<TeachplanMedia>()
                .eq(TeachplanMedia::getTeachplanId, bindTeachPlanMediaDTO.getTeachplanId());
        teachplanMediaMapper.delete(queryWrapper1);
//        if(delete!=1) throw new CustomException(CommonExMsg.DELETE_FAILED);
        /*业务逻辑校验教学计划id和媒资id均不能为空*/
        if(bindTeachPlanMediaDTO.getMediaId()==null||bindTeachPlanMediaDTO.getFileName()==null) return;
        /*业务逻辑校验(媒资存在) todo*/
        //新增信息
        TeachplanMedia teachplanMedia = BeanUtil.copyProperties(bindTeachPlanMediaDTO, TeachplanMedia.class);
        teachplanMedia.setMediaFilename(bindTeachPlanMediaDTO.getFileName());
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMediaMapper.insert(teachplanMedia);
    }

    //课程计划解除绑定媒资
    @Override
    public void removeBind(Long teachPlanId, Long mediaId) {
        /*业务逻辑校验(课程存在)*/
        Teachplan teachplan = getById(teachPlanId);
        if(teachplan==null) throw new CustomException(TeachPlanExMsg.TEACH_PLAN_NO_EXIST);
        //删除相关信息
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<TeachplanMedia>()
                .eq(TeachplanMedia::getTeachplanId, teachPlanId)
                .eq(TeachplanMedia::getMediaId, mediaId);
        int delete = teachplanMediaMapper.delete(queryWrapper);
        if(delete!=1) throw new CustomException(CommonExMsg.DELETE_FAILED);
    }

    //交换元素
    private void swap(Teachplan teachplan1, Teachplan teachplan2) {
        int temp=teachplan1.getOrderby();
        teachplan1.setOrderby(teachplan2.getOrderby());
        teachplan2.setOrderby(temp);
    }

    //二分查找
    public static int binarySearch(List<Teachplan> arr, int target) {
        int left = 0;
        int right = arr.size();
        while (left < right) {
            int mid = left + (right - left) / 2;
            if (arr.get(mid).getOrderby() == target) return mid;
            else if (arr.get(mid).getOrderby() < target) left = mid + 1;
            else right = mid;
        }
        return -1;
    }

}
