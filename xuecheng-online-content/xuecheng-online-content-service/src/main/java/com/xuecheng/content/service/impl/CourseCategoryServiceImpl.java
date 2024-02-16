package com.xuecheng.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.pojo.entity.CourseCategory;
import com.xuecheng.content.model.pojo.vo.CourseCategoryTreeVO;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>
 * 课程分类 服务实现类
 * </p>
 *
 * @author itcast
 */
@Slf4j
@Service
public class CourseCategoryServiceImpl extends ServiceImpl<CourseCategoryMapper, CourseCategory> implements CourseCategoryService {

    @Override
    public List<CourseCategoryTreeVO> courseCategoryTreeVO(String id) {
        List<CourseCategory> courseCategories = list();
        ArrayList<CourseCategoryTreeVO> courseCategoryTreeVOS = new ArrayList<>();
        buildTree(id, courseCategories, courseCategoryTreeVOS);
        return courseCategoryTreeVOS;
    }

    private static void buildTree(String id, List<CourseCategory> courseCategories, List<CourseCategoryTreeVO> courseCategoryTreeVOS) {
        for (CourseCategory courseCategory : courseCategories) {
            if(courseCategory.getParentid().equals(id)){
                CourseCategoryTreeVO courseCategoryTreeVO = BeanUtil.copyProperties(courseCategory, CourseCategoryTreeVO.class);
                courseCategoryTreeVOS.add(courseCategoryTreeVO);
                buildTree(courseCategory.getId(),courseCategories,courseCategoryTreeVO.getChildrenTreeNodes());
                if(courseCategoryTreeVO.getChildrenTreeNodes().isEmpty()) courseCategoryTreeVO.setChildrenTreeNodes(null);
            }
        }
    }

}
