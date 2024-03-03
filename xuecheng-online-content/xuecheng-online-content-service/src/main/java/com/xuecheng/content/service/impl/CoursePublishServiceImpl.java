package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.constant.CourseExMsg;
import com.xuecheng.base.constant.ExMsgConstant;
import com.xuecheng.base.enumeration.CourseAuditStatus;
import com.xuecheng.base.enumeration.CourseStatus;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignClient.MediaServiceClient;
import com.xuecheng.content.feignClient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.pojo.entity.CourseBase;
import com.xuecheng.content.model.pojo.entity.CourseMarket;
import com.xuecheng.content.model.pojo.entity.CoursePublish;
import com.xuecheng.content.model.pojo.entity.CoursePublishPre;
import com.xuecheng.content.model.pojo.feign.CourseIndex;
import com.xuecheng.content.model.pojo.vo.CourseBaseInfoVO;
import com.xuecheng.content.model.pojo.vo.CoursePreviewVO;
import com.xuecheng.content.model.pojo.vo.TeachPlanVO;
import com.xuecheng.content.service.CourseBaseService;
import com.xuecheng.content.service.CourseMarketService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 课程发布 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CoursePublishServiceImpl extends ServiceImpl<CoursePublishMapper, CoursePublish> implements CoursePublishService {

    @Autowired
    private CourseBaseService courseBaseService;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CourseMarketService courseMarketService;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Autowired
    private SearchServiceClient searchServiceClient;

    @Override
    public CoursePreviewVO preview(Long courseId) {
        CourseBaseInfoVO courseBaseInfoVO = courseBaseService.get(courseId);
        List<TeachPlanVO> teachPlanVOS = teachplanService.getList(courseId);
        return CoursePreviewVO.builder()
                .courseBase(courseBaseInfoVO)
                .teachplans(teachPlanVOS)
                .build();
    }

    @Transactional
    @Override
    public void commitAudit(Long courseId) {
        Long companyId = 1232141425L;
        //查询数据
        CourseBaseInfoVO courseBaseInfoVO = courseBaseService.get(courseId);
        /*业务逻辑校验(课程存在)*/
        if(courseBaseInfoVO==null) throw new CustomException(CourseExMsg.COURSE_NO_EXIST);
        /*业务逻辑校验(课程所属机构一致)*/
        if(!courseBaseInfoVO.getCompanyId().equals(companyId)) throw new CustomException(ExMsgConstant.AUTHORITY_LIMIT);
        /*业务逻辑校验(课程状态为未提交)*/
        if(courseBaseInfoVO.getAuditStatus().equals("202003")) throw new CustomException(CourseExMsg.COMMITTED);
        /*业务逻辑校验(课程图片已上传)*/
        if(StringUtils.isEmpty(courseBaseInfoVO.getPic())) throw new CustomException(CourseExMsg.EMPTY_COURSE_PICTURE);
        List<TeachPlanVO> teachPlanVOS = teachplanService.getList(courseId);
        /*业务逻辑校验(课程计划不为空)*/
        if(teachPlanVOS==null||teachPlanVOS.isEmpty()) throw new CustomException(CourseExMsg.EMPTY_COURSE_TEACH_PLAN);
        CourseMarket courseMarket = courseMarketService.getById(courseId);
        //封装数据保存到预发布表
        /*基本信息*/
        CoursePublishPre coursePublishPre = BeanUtil.copyProperties(courseBaseInfoVO, CoursePublishPre.class);
        /*营销信息*/
        String courseMarketJSON = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJSON);
        /*课程计划信息*/
        String teachPlanVOSJSON = JSON.toJSONString(teachPlanVOS);
        coursePublishPre.setTeachplan(teachPlanVOSJSON);
        /*课程状态*/
        coursePublishPre.setStatus(CourseAuditStatus.COMMITTED.getValue());
        /*保存或更新数据*/
        CoursePublishPre old = coursePublishPreMapper.selectById(courseId);
        if(old==null) coursePublishPreMapper.insert(coursePublishPre);
        else coursePublishPreMapper.updateById(coursePublishPre);
        //更新课程基本消息状态
        LambdaUpdateWrapper<CourseBase> updateWrapper = new LambdaUpdateWrapper<CourseBase>()
                .eq(CourseBase::getId, courseId)
                .set(CourseBase::getAuditStatus, CourseAuditStatus.COMMITTED.getValue());
        courseBaseService.update(updateWrapper);
    }

    @Transactional
    @Override
    public void coursePublish(Long courseId) {
        Long companyId = 1232141425L;
        //查询数据
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        /*业务逻辑校验(预发布课程存在)*/
        if(coursePublishPre==null) throw new CustomException(CourseExMsg.UNCOMMITTED);
        /*业务逻辑校验(课程所属机构一致)*/
        if(!coursePublishPre.getCompanyId().equals(companyId)) throw new CustomException(ExMsgConstant.AUTHORITY_LIMIT);
        /*业务逻辑校验(预发布课程已通过审核)*/
        if(!coursePublishPre.getStatus().equals("202004")) throw new CustomException(CourseExMsg.NOT_PASS);
        //向课程发布表更新或插入数据
        CoursePublish coursePublish = BeanUtil.copyProperties(coursePublishPre, CoursePublish.class);
        coursePublish.setStatus(CourseStatus.PUBLISHED.getValue());
        CoursePublish old = getById(courseId);
        if(old==null) save(coursePublish);
        else updateById(coursePublish);
        //向消息表插入数据
        saveCoursePublishMessage(courseId);
        //删除预发布表数据
        int delete = coursePublishPreMapper.deleteById(courseId);
        if(delete!=1) throw new CustomException(ExMsgConstant.DELETE_FAILED);
    }

    private void saveCoursePublishMessage(Long courseId){
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if(mqMessage==null) throw new CustomException(ExMsgConstant.INSERT_FAILED);
    }

    //生成课程静态页面
    @Override
    public File getCourseHtml(Long courseId){
        File result = null;
        try {
            Configuration configuration = new Configuration(Configuration.getVersion());
            String classPath = getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classPath+"/templates/"));
            configuration.setDefaultEncoding("utf-8");
            Template template = configuration.getTemplate("course_template.ftl");
            CoursePreviewVO coursePreviewVO = preview(courseId);
            HashMap<String, CoursePreviewVO> hashMap = new HashMap<>();
            hashMap.put("model",coursePreviewVO);
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, hashMap);
            InputStream inputStream = IOUtils.toInputStream(html, "utf-8");
            result = File.createTempFile("coursePublish", ".html");
            FileOutputStream fileOutputStream = new FileOutputStream(result);
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            log.info("课程页面静态化异常:",e);
            e.printStackTrace();
        }
        if(result==null) throw new CustomException(CourseExMsg.COURSE_HTML_FAILED);
        return result;
    }

    //课程静态页面上传至minio
    @Override
    public void uploadHtmlToMinio(File file, Long courseId){
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String result = mediaServiceClient.uploadFile(multipartFile, "course/" + courseId + ".html");
        if(result==null) throw new CustomException(CourseExMsg.COURSE_HTML_FAILED);
    }

    //添加课程文档
    @Override
    public void addCourseIndex(CourseIndex courseIndex){
        Boolean result = searchServiceClient.add(courseIndex);
        if(!result){
            log.info("添加课程文档失败:[{}]",courseIndex);
            throw new CustomException(CourseExMsg.ADD_COURSE_INDEX_FAILED);
        }
    }

}
