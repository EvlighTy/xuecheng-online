package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.constant.ExMsgConstant;
import com.xuecheng.base.enumeration.CourseAuditStatus;
import com.xuecheng.base.enumeration.CourseStatus;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.result.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.pojo.dto.AddCourseDTO;
import com.xuecheng.content.model.pojo.dto.CourseQueryDTO;
import com.xuecheng.content.model.pojo.dto.EditCourseDTO;
import com.xuecheng.content.model.pojo.entity.CourseBase;
import com.xuecheng.content.model.pojo.entity.CourseMarket;
import com.xuecheng.content.model.pojo.vo.CourseBaseInfoVO;
import com.xuecheng.content.service.CourseBaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    //分页查询课程
    @Override
    public PageResult<CourseBase> getList(PageParams pageParams,CourseQueryDTO courseQueryDTO) {
        String courseName = courseQueryDTO.getCourseName();
        String auditStatus = courseQueryDTO.getAuditStatus();
        String publishStatus = courseQueryDTO.getPublishStatus();
        Page<CourseBase> page = Page.of(pageParams.getPageNo(), pageParams.getPageSize());
        Page<CourseBase> result = lambdaQuery()
                .like(courseName != null && !courseName.isEmpty(), CourseBase::getName, courseName)
                .eq(auditStatus != null && !auditStatus.isEmpty(), CourseBase::getAuditStatus, auditStatus)
                .eq(publishStatus != null && !publishStatus.isEmpty(), CourseBase::getStatus, publishStatus)
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
        courseBase.setStatus(CourseStatus.UNPUBLISHED.getValue());
        //插入课程表
        boolean save = save(courseBase);
        if(!save) throw new RuntimeException(ExMsgConstant.INSERT_FAILED);
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
        Long companyId=123L;
        //业务逻辑校验
        CourseBase courseBase = getById(editCourseDTO.getId());
        if(courseBase==null) throw new CustomException(ExMsgConstant.COURSE_IS_NULL);
        if(!courseBase.getCompanyId().equals(companyId)) throw new CustomException(ExMsgConstant.AUTHORITY_LIMIT);
        //封装数据
        BeanUtil.copyProperties(editCourseDTO,courseBase);
        CourseMarket courseMarket = BeanUtil.copyProperties(editCourseDTO, CourseMarket.class);
        //修改课程表
        boolean update = updateById(courseBase);
        if(!update) throw new CustomException(ExMsgConstant.UPDATE_FAILED);
        //修改课程营销表
        int updateMarket = courseMarketMapper.updateById(courseMarket);
        if(updateMarket==0) throw new CustomException(ExMsgConstant.UPDATE_FAILED);
        return get(editCourseDTO.getId());
    }


    //参数合法性校验
    private static void paramsCheck(AddCourseDTO addCourseDTO) {
        if(StringUtils.isEmpty(addCourseDTO.getName())) throw new CustomException(ExMsgConstant.EMPTY_COURSE_NAME);
        if(addCourseDTO.getPrice()==null) throw new CustomException(ExMsgConstant.EMPTY_COURSE_PRICE);
        if(StringUtils.isEmpty(addCourseDTO.getCharge())) throw new CustomException(ExMsgConstant.EMPTY_CHARGE_RULE);
        if(addCourseDTO.getPrice()<0) throw new CustomException(ExMsgConstant.ERROR_COURSE_PRICE);
        if(StringUtils.isEmpty(addCourseDTO.getUsers())) throw new CustomException(ExMsgConstant.EMPTY_SUIT_PEOPLE);
        if(StringUtils.isEmpty(addCourseDTO.getSt())||StringUtils.isEmpty(addCourseDTO.getMt())) throw new CustomException(ExMsgConstant.EMPTY_COURSE_CATEGORY);
        if(StringUtils.isEmpty(addCourseDTO.getGrade())) throw new CustomException(ExMsgConstant.EMPTY_COURSE_RANK);
        if(StringUtils.isEmpty(addCourseDTO.getPic())) throw new CustomException(ExMsgConstant.EMPTY_COURSE_PICTURE);
        if(StringUtils.isEmpty(addCourseDTO.getTeachmode())) throw new CustomException(ExMsgConstant.EMPTY_COURSE_TEACH_MODE);
    }

}
