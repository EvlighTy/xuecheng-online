package com.xuecheng.learning.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.enumeration.ChooserCourseStatus;
import com.xuecheng.base.enumeration.ChooserCourseType;
import com.xuecheng.base.enumeration.CourseChargeType;
import com.xuecheng.base.enumeration.LearnStatus;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.base.exmsg.AuthExMsg;
import com.xuecheng.base.exmsg.ChooserCourseExMsg;
import com.xuecheng.base.exmsg.CommonExMsg;
import com.xuecheng.base.exmsg.CourseExMsg;
import com.xuecheng.base.model.result.PageResult;
import com.xuecheng.learning.feign.ContentServiceClient;
import com.xuecheng.learning.feign.model.CoursePublish;
import com.xuecheng.learning.mapper.XcChooseCourseMapper;
import com.xuecheng.learning.mapper.XcCourseTablesMapper;
import com.xuecheng.learning.model.dto.MyCourseTableDTO;
import com.xuecheng.learning.model.po.XcChooseCourse;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.model.vo.XcChooseCourseVO;
import com.xuecheng.learning.model.vo.XcCourseTablesVO;
import com.xuecheng.learning.service.ChooseCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ChooseCourseServiceImpl extends ServiceImpl<XcChooseCourseMapper, XcChooseCourse> implements ChooseCourseService {

    @Autowired
    private XcChooseCourseMapper xcChooseCourseMapper;

    @Autowired
    private XcCourseTablesMapper xcCourseTablesMapper;

    @Autowired
    private ContentServiceClient contentServiceClient;

    //用户选课
    @Transactional
    @Override
    public XcChooseCourseVO addChooseCourse(String userId, Long courseId) {
        /*业务逻辑校验(合法用户)*/
        if (userId==null) throw new CustomException(AuthExMsg.LOGIN_FIRST);
        /*业务逻辑校验(课程已发布)*/
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if(coursepublish==null) throw new CustomException(CourseExMsg.COURSE_NO_EXIST);
        String charge = coursepublish.getCharge();
        XcChooseCourse xcChooseCourse;
        if(charge.equals(CourseChargeType.FREE.getValue())){
            //免费课程
            /*业务逻辑校验(用户未选当前课程)*/
            LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>()
                    .eq(XcChooseCourse::getCourseId, courseId)
                    .eq(XcChooseCourse::getUserId, userId)
                    .eq(XcChooseCourse::getStatus, ChooserCourseStatus.SUCCESS.getValue());
            xcChooseCourse = getOne(queryWrapper);
            if(xcChooseCourse!=null) throw new CustomException(ChooserCourseExMsg.REPEAT_CHOOSE);
            //添加选课记录
            xcChooseCourse = add2ChooseCourse(coursepublish, userId,"free");
            //添加到用户的选课
            /*业务逻辑校验(用户未选当前课程)*/
            LambdaQueryWrapper<XcCourseTables> queryWrapper1 = new LambdaQueryWrapper<XcCourseTables>()
                    .eq(XcCourseTables::getCourseId, courseId)
                    .eq(XcCourseTables::getUserId, userId);
            boolean exists = xcCourseTablesMapper.exists(queryWrapper1);
            if(exists) throw new CustomException(ChooserCourseExMsg.REPEAT_CHOOSE);
            add2XcCourseTables(xcChooseCourse);
        }else {
            //付费课程
            /*业务逻辑校验(用户未选当前课程 或 用户已选当前课程且已支付)*/
            LambdaQueryWrapper<XcChooseCourse> queryWrapper = new LambdaQueryWrapper<XcChooseCourse>()
                    .eq(XcChooseCourse::getCourseId, courseId)
                    .eq(XcChooseCourse::getUserId, userId)
                    .eq(XcChooseCourse::getStatus, ChooserCourseStatus.UNPAID.getValue())
                    .eq(XcChooseCourse::getOrderType, ChooserCourseType.CHARGE.getValue());
            xcChooseCourse = getOne(queryWrapper);
            if(xcChooseCourse==null){
                //添加选课记录(不添加到用户的选课)
                xcChooseCourse = add2ChooseCourse(coursepublish, userId,"charge");
            }
        }
        XcChooseCourseVO xcChooseCourseVO = BeanUtil.copyProperties(xcChooseCourse, XcChooseCourseVO.class);
        XcCourseTablesVO xcCourseTablesVO = getLearnStatus(userId,courseId);
        xcChooseCourseVO.setLearnStatus(xcCourseTablesVO.getLearnStatus());
        return xcChooseCourseVO;
    }

    //查询课程学习资格
    @Override
    public XcCourseTablesVO getLearnStatus(String userId, Long courseId) {
        XcCourseTablesVO xcCourseTablesVO = new XcCourseTablesVO();
        if(userId==null){
            xcCourseTablesVO.setLearnStatus(LearnStatus.ABNORMAL.getValue());
            return xcCourseTablesVO;
        }
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getUserId, userId)
                .eq(XcCourseTables::getCourseId, courseId);
        XcCourseTables xcCourseTables = xcCourseTablesMapper.selectOne(queryWrapper);
        if(xcCourseTables==null){
            xcCourseTablesVO.setLearnStatus(LearnStatus.ABNORMAL.getValue());
        }else if(xcCourseTables.getValidtimeEnd().isBefore(LocalDateTime.now())){
            xcCourseTablesVO.setLearnStatus(LearnStatus.EXPIRED.getValue());
        }else {
            xcCourseTablesVO.setLearnStatus(LearnStatus.NORMAL.getValue());
        }
        return xcCourseTablesVO;
    }

    //更新选课表支付状态+添加选课
    @Transactional
    @Override
    public void updateChooseCourseAndSave2CourseTables(String chooserCourseId) {
        /*业务逻辑校验(选课记录存在)*/
        XcChooseCourse xcChooseCourse = xcChooseCourseMapper.selectById(chooserCourseId);
        if(xcChooseCourse==null) throw new CustomException(ChooserCourseExMsg.CHOOSE_COURSE_NOT_EXIST);
        //更新选课记录
        LambdaUpdateWrapper<XcChooseCourse> updateWrapper = new LambdaUpdateWrapper<XcChooseCourse>()
                .eq(XcChooseCourse::getId, chooserCourseId)
                .set(XcChooseCourse::getStatus, ChooserCourseStatus.SUCCESS.getValue());
        boolean update = update(updateWrapper);
        if(!update) throw new CustomException(CommonExMsg.UPDATE_FAILED);
        //添加选课
        xcChooseCourse.setStatus(ChooserCourseStatus.SUCCESS.getValue());
        add2XcCourseTables(xcChooseCourse);
    }

    //用户查询我的课程
    @Override
    public PageResult<XcCourseTables> getMyCourseTable(MyCourseTableDTO myCourseTableDTO) {
        Page<XcCourseTables> page = new Page<>(myCourseTableDTO.getPage(), myCourseTableDTO.getSize());
        LambdaQueryWrapper<XcCourseTables> queryWrapper = new LambdaQueryWrapper<XcCourseTables>()
                .eq(XcCourseTables::getUserId, myCourseTableDTO.getUserId());
        if(myCourseTableDTO.getSortType().equals("1")){
            queryWrapper.orderByAsc(XcCourseTables::getUpdateDate);
        }else {
            queryWrapper.orderByAsc(XcCourseTables::getCreateDate);
        }
        Page<XcCourseTables> result = xcCourseTablesMapper.selectPage(page, queryWrapper);
        return new PageResult<>(result.getTotal(),result.getRecords());
    }

    //添加到用户的选课
    private XcCourseTables add2XcCourseTables(XcChooseCourse xcChooseCourse) {
        /*业务逻辑校验(选课成功)*/
        if(!xcChooseCourse.getStatus().equals(ChooserCourseStatus.SUCCESS.getValue())) throw new CustomException(ChooserCourseExMsg.UNPAID);
        XcCourseTables xcCourseTables = BeanUtil.copyProperties(xcChooseCourse, XcCourseTables.class);
        xcCourseTables.setChooseCourseId(xcChooseCourse.getId());
        xcCourseTables.setCourseType(xcChooseCourse.getOrderType());
        xcCourseTables.setUpdateDate(LocalDateTime.now());
        int insert1 = xcCourseTablesMapper.insert(xcCourseTables);
        if(insert1!=1) throw new CustomException(CommonExMsg.INSERT_FAILED);
        return xcCourseTables;
    }

    //添加选课记录
    private XcChooseCourse add2ChooseCourse(CoursePublish coursepublish, String userId,String chargeType) {
        XcChooseCourse xcChooseCourse;
        xcChooseCourse = new XcChooseCourse();
        xcChooseCourse.setCourseId(coursepublish.getId());
        xcChooseCourse.setCourseName(coursepublish.getName());
        xcChooseCourse.setUserId(userId);
        xcChooseCourse.setCompanyId(coursepublish.getCompanyId());
        xcChooseCourse.setCreateDate(LocalDateTime.now());
        xcChooseCourse.setValidtimeStart(LocalDateTime.now());
        if (chargeType.equals("free")){
            //免费课程
            xcChooseCourse.setOrderType(ChooserCourseType.FREE.getValue()); //免费课程
            xcChooseCourse.setCoursePrice(0f); //免费课程价格为0
            xcChooseCourse.setValidDays(365); //免费课程默认365
            xcChooseCourse.setStatus(ChooserCourseStatus.SUCCESS.getValue()); //选课成功
            xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(365));
        }else {
            //收费课程
            xcChooseCourse.setCoursePrice(coursepublish.getPrice());
            xcChooseCourse.setOrderType(ChooserCourseType.CHARGE.getValue()); //收费课程
            xcChooseCourse.setStatus(ChooserCourseStatus.UNPAID.getValue()); //待支付
            xcChooseCourse.setValidDays(coursepublish.getValidDays());
            xcChooseCourse.setValidtimeEnd(LocalDateTime.now().plusDays(coursepublish.getValidDays()));
        }
        int insert = xcChooseCourseMapper.insert(xcChooseCourse);
        if(insert!=1) throw new CustomException(CommonExMsg.INSERT_FAILED);
        return xcChooseCourse;
    }

}
