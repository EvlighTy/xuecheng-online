package com.xuecheng.learning.controller;

import com.xuecheng.base.exception.CustomException;
import com.xuecheng.base.exmsg.AuthExMsg;
import com.xuecheng.base.model.result.PageResult;
import com.xuecheng.learning.model.dto.MyCourseTableDTO;
import com.xuecheng.learning.model.po.XcCourseTables;
import com.xuecheng.learning.model.vo.XcChooseCourseVO;
import com.xuecheng.learning.model.vo.XcCourseTablesVO;
import com.xuecheng.learning.service.ChooseCourseService;
import com.xuecheng.learning.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class MyCourseTablesController {

    @Autowired
    private ChooseCourseService chooseCourseService;

    @PostMapping("/choosecourse/{courseId}")
    public XcChooseCourseVO addChooseCourse(@PathVariable("courseId") Long courseId) {
        log.info("用户选课");
        com.xuecheng.learning.util.SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = null;
        if(user!=null) userId=user.getId();
        return chooseCourseService.addChooseCourse(userId,courseId);
    }

    @PostMapping("/choosecourse/learnstatus/{courseId}")
    public XcCourseTablesVO getLearnStatus(@PathVariable("courseId") Long courseId) {
        log.info("用户查询课程学习资格");
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        String userId = null;
        if(user!=null) userId=user.getId();
        return chooseCourseService.getLearnStatus(userId, courseId);
    }

    @GetMapping("/mycoursetable")
    public PageResult<XcCourseTables> getMyCourseTable(MyCourseTableDTO myCourseTableDTO) {
        log.info("用户查询我的课程");
        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user==null) throw new CustomException(AuthExMsg.LOGIN_FIRST);
        myCourseTableDTO.setUserId(user.getId());
        return chooseCourseService.getMyCourseTable(myCourseTableDTO);
    }

}
