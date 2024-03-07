package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.enumeration.CourseChargeType;
import com.xuecheng.base.enumeration.LearnStatus;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.base.exmsg.AuthExMsg;
import com.xuecheng.base.exmsg.CourseExMsg;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.pojo.vo.TeachPlanVO;
import com.xuecheng.learning.feign.ContentServiceClient;
import com.xuecheng.learning.feign.MediaServiceClient;
import com.xuecheng.learning.feign.model.CoursePublish;
import com.xuecheng.learning.model.vo.XcCourseTablesVO;
import com.xuecheng.learning.service.ChooseCourseService;
import com.xuecheng.learning.service.LearningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Autowired
    private ChooseCourseService chooseCourseService;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {
        /*业务逻辑校验(课程存在)*/
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);
        if (coursepublish==null) throw new CustomException(CourseExMsg.COURSE_NO_EXIST);
        /*判断课程收费情况*/
        if (coursepublish.getCharge().equals(CourseChargeType.FREE.getValue())){
            //免费
            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
        }else {
            //收费
            /*判断章节收费情况*/
            List<TeachPlanVO> teachPlanVOS = JSON.parseArray(coursepublish.getTeachplan(), TeachPlanVO.class);
            List<TeachPlanVO> collect = teachPlanVOS.stream()
                    .flatMap(teachPlanVO -> teachPlanVO.getTeachPlanTreeNodes().stream())
                    .filter(teachPlanVO -> teachPlanVO.getId().equals(teachplanId))
                    .collect(Collectors.toList());
            if (collect.get(0).getIsPreview().equals("1")){
                //支持试看
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            }else {
                //不支持试看
                /*用户不合法*/
                if(userId==null) return RestResponse.validfail(AuthExMsg.LOGIN_FIRST);
                /*查询学习资格*/
                XcCourseTablesVO xcCourseTablesVO = chooseCourseService.getLearnStatus(userId, courseId);
                if (xcCourseTablesVO.getLearnStatus().equals(LearnStatus.NORMAL.getValue())){
                    //有资格
                    return mediaServiceClient.getPlayUrlByMediaId(mediaId);
                }else {
                    //没资格
                    if(xcCourseTablesVO.getLearnStatus().equals(LearnStatus.ABNORMAL.getValue())) return RestResponse.validfail("未选择该门课程");
                    else return RestResponse.validfail("课程观看权限已过期");
                }
            }

        }
    }

}
