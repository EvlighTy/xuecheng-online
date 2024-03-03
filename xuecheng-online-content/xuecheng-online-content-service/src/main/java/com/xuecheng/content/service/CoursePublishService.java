package com.xuecheng.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuecheng.content.model.pojo.entity.CoursePublish;
import com.xuecheng.content.model.pojo.feign.CourseIndex;
import com.xuecheng.content.model.pojo.vo.CoursePreviewVO;

import java.io.File;

/**
 * <p>
 * 课程发布 服务类
 * </p>
 *
 * @author itcast
 * @since 2024-02-11
 */
public interface CoursePublishService extends IService<CoursePublish> {

    CoursePreviewVO preview(Long courseId);

    void commitAudit(Long courseId);

    void coursePublish(Long courseId);

    //生成课程静态页面
    File getCourseHtml(Long courseId);

    //课程静态页面上传至minio
    void uploadHtmlToMinio(File file, Long courseId);

    void addCourseIndex(CourseIndex courseIndex);
}
