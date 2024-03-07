package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.enumeration.CourseAuditStatus;
import com.xuecheng.base.enumeration.CoursePublishStatus;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.base.exmsg.CommonExMsg;
import com.xuecheng.base.exmsg.CourseExMsg;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.model.result.PageResult;
import com.xuecheng.base.utils.SecurityUtil;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.pojo.dto.AddCourseDTO;
import com.xuecheng.content.model.pojo.dto.CourseQueryDTO;
import com.xuecheng.content.model.pojo.dto.EditCourseDTO;
import com.xuecheng.content.model.pojo.entity.*;
import com.xuecheng.content.model.pojo.vo.CourseBaseInfoVO;
import com.xuecheng.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 课程基本信息 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseBaseServiceImpl extends ServiceImpl<CourseBaseMapper, CourseBase> implements CourseBaseService {

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Autowired
    private TeachplanMediaMapper teachplanMediaMapper;

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Autowired
    private CoursePublishMapper coursePublishMapper;

    //分页查询课程
    @Override
    public PageResult<CourseBase> getList(PageParams pageParams,CourseQueryDTO courseQueryDTO) {
        /*业务逻辑校验(只能查询本机构课程)*/
        String companyId = Objects.requireNonNull(SecurityUtil.getUser()).getCompanyId();
        String courseName = courseQueryDTO.getCourseName();
        String auditStatus = courseQueryDTO.getAuditStatus();
        String publishStatus = courseQueryDTO.getPublishStatus();
        Page<CourseBase> page = Page.of(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> result = lambdaQuery()
                .like(courseName != null && !courseName.isEmpty(), CourseBase::getName, courseName)
                .eq(auditStatus != null && !auditStatus.isEmpty(), CourseBase::getAuditStatus, auditStatus)
                .eq(publishStatus != null && !publishStatus.isEmpty(), CourseBase::getStatus, publishStatus)
                .eq(CourseBase::getCompanyId,companyId)
                .page(page);
        return new PageResult<>(result.getTotal(),result.getRecords());
    }

    //添加课程
    @Transactional
    @Override
    public CourseBaseInfoVO add(AddCourseDTO addCourseDTO) {
        //参数合法性校验
        /*paramsCheck(addCourseDTO);*/
        //获取机构id
        Long companyId=123L;
        //拷贝为课程对象
        CourseBase courseBase = BeanUtil.copyProperties(addCourseDTO, CourseBase.class);
        courseBase.setCompanyId(companyId);
        courseBase.setAuditStatus(CourseAuditStatus.REVIEW_FAILED.getValue());
        courseBase.setStatus(CoursePublishStatus.UNPUBLISHED.getValue());
        //插入课程表
        boolean save = save(courseBase);
        if(!save) throw new RuntimeException(CommonExMsg.INSERT_FAILED);
        //拷贝为课程营销对象
        CourseMarket courseMarket = BeanUtil.copyProperties(addCourseDTO, CourseMarket.class);
        courseBase.setId(courseBase.getId());
        if(courseMarketMapper.selectById(courseMarket.getId())!=null){
            //更新
            courseMarketMapper.updateById(courseMarket);
        }else {
            //新增
            courseMarketMapper.insert(courseMarket);
        }
        //返回数据
        return get(courseBase.getId());
    }

    //根据id查询课程
    @Override
    public CourseBaseInfoVO get(Long id) {
        //查询课程
        CourseBase courseBase = getById(id);
        //查询课程营销
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        //查询分类名称
        String mtName = courseCategoryMapper.selectById(courseBase.getMt()).getName();
        String stName = courseCategoryMapper.selectById(courseBase.getSt()).getName();
        //封装数据
        CourseBaseInfoVO courseBaseInfoVO = BeanUtil.copyProperties(courseBase, CourseBaseInfoVO.class);
        BeanUtil.copyProperties(courseMarket,courseBaseInfoVO);
        courseBaseInfoVO.setMtName(mtName);
        courseBaseInfoVO.setStName(stName);
        return courseBaseInfoVO;
    }

    //修改课程
    @Transactional
    @Override
    public CourseBaseInfoVO edit(EditCourseDTO editCourseDTO) {
        //获取机构id
        long companyId = 1232141425L;
        CourseBase courseBase = getById(editCourseDTO.getId());
        /*业务逻辑校验(课程存在)*/
        if(courseBase==null) throw new CustomException(CourseExMsg.COURSE_NO_EXIST);
        /*业务逻辑校验(课程所属机构一致)*/
        if(!courseBase.getCompanyId().equals(companyId)) throw new CustomException(CommonExMsg.AUTHORITY_LIMIT);
        //封装数据
        BeanUtil.copyProperties(editCourseDTO,courseBase);
        CourseMarket courseMarket = BeanUtil.copyProperties(editCourseDTO, CourseMarket.class);
        //修改课程表
        boolean update = updateById(courseBase);
        if(!update) throw new CustomException(CommonExMsg.UPDATE_FAILED);
        //修改课程营销表
        int updateMarket = courseMarketMapper.updateById(courseMarket);
        if(updateMarket==0) throw new CustomException(CommonExMsg.UPDATE_FAILED);
        return get(editCourseDTO.getId());
    }

    //删除课程
    @Transactional
    @Override
    public void delete(Long id) {
        CourseBase courseBase = getById(id);
        //删除基本信息
        boolean removeCourseBase = removeById(id);
        //删除营销信息
        int deleteCourseMarket = courseMarketMapper.deleteById(courseBase.getId());
        //删除课程计划
        LambdaQueryWrapper<Teachplan> queryWrapperTeachPlan = new LambdaQueryWrapper<Teachplan>()
                .eq(Teachplan::getCourseId, id);
        List<Long> teachPlanIds = teachplanMapper.selectList(queryWrapperTeachPlan).stream().map(Teachplan::getId).collect(Collectors.toList());
        int deleteTeachPlan = teachplanMapper.delete(queryWrapperTeachPlan);
        //删除媒资
        LambdaQueryWrapper<TeachplanMedia> queryWrapperTeachPlanMedia = new LambdaQueryWrapper<TeachplanMedia>()
                .in(TeachplanMedia::getTeachplanId, teachPlanIds);
        int deleteTeachPlaneMedia = teachplanMediaMapper.delete(queryWrapperTeachPlanMedia);
        //删除教师
        LambdaQueryWrapper<CourseTeacher> queryWrapperTeacher = new LambdaQueryWrapper<CourseTeacher>()
                .eq(CourseTeacher::getCourseId, id);
        int deleteTeacher = courseTeacherMapper.delete(queryWrapperTeacher);
        if(!(removeCourseBase
                && deleteCourseMarket==1
                && deleteTeachPlan==1
                && deleteTeachPlaneMedia==1
                && deleteTeacher==1)) throw new CustomException(CommonExMsg.DELETE_FAILED);
    }

    //下架课程
    @Transactional
    @Override
    public RestResponse courseOffline(Long courseId) {
        /*业务逻辑校验(课程存在)*/
        CourseBase courseBase = getById(courseId);
        if(courseBase==null) throw new CustomException(CourseExMsg.COURSE_NO_EXIST);
        /*业务逻辑校验(课程已发布)*/
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        if(coursePublish==null) throw new CustomException(CourseExMsg.NOT_PUBLISH);
        //更新课程基本表
        LambdaUpdateWrapper<CourseBase> updateWrapper = new LambdaUpdateWrapper<CourseBase>()
                .eq(CourseBase::getId, courseId)
                .set(CourseBase::getStatus, CoursePublishStatus.OFFLINE.getValue());
        boolean update = update(updateWrapper);
        if(!update) throw new CustomException(CommonExMsg.UPDATE_FAILED);
        //更新课程发布表
        LambdaUpdateWrapper<CoursePublish> updateWrapper1 = new LambdaUpdateWrapper<CoursePublish>()
                .eq(CoursePublish::getId, courseId)
                .set(CoursePublish::getStatus, CoursePublishStatus.OFFLINE.getValue());
        int update1 = coursePublishMapper.update(null, updateWrapper1);
        if(update1<1) throw new CustomException(CommonExMsg.UPDATE_FAILED);
        return RestResponse.success();
    }

    /*//参数合法性校验
    private static void paramsCheck(AddCourseDTO addCourseDTO) {
        if(StringUtils.isEmpty(addCourseDTO.getName())) throw new CustomException(CommonExMsg.EMPTY_COURSE_NAME);
        if(addCourseDTO.getPrice()==null) throw new CustomException(CommonExMsg.EMPTY_COURSE_PRICE);
        if(StringUtils.isEmpty(addCourseDTO.getCharge())) throw new CustomException(CommonExMsg.EMPTY_CHARGE_RULE);
        if(addCourseDTO.getPrice()<0) throw new CustomException(CommonExMsg.ERROR_COURSE_PRICE);
        if(StringUtils.isEmpty(addCourseDTO.getUsers())) throw new CustomException(CommonExMsg.EMPTY_SUIT_PEOPLE);
        if(StringUtils.isEmpty(addCourseDTO.getSt())||StringUtils.isEmpty(addCourseDTO.getMt())) throw new CustomException(CommonExMsg.EMPTY_COURSE_CATEGORY);
        if(StringUtils.isEmpty(addCourseDTO.getGrade())) throw new CustomException(CommonExMsg.EMPTY_COURSE_RANK);
        if(StringUtils.isEmpty(addCourseDTO.getPic())) throw new CustomException(CommonExMsg.EMPTY_COURSE_PICTURE);
        if(StringUtils.isEmpty(addCourseDTO.getTeachmode())) throw new CustomException(CommonExMsg.EMPTY_COURSE_TEACH_MODE);
    }*/

}