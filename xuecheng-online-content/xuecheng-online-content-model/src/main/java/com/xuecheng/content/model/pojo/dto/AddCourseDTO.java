package com.xuecheng.content.model.pojo.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 * @description 添加课程dto
 * @author Mr.M
 * @date 2022/9/7 17:40
 * @version 1.0
 */
@Data
@ApiModel(value="AddCourseDTO", description="新增课程基本信息")
public class AddCourseDTO {

    @NotEmpty(message = "课程名称不能为空")
    private String name; //课程名称

    @NotEmpty(message = "适用人群不能为空")
    @Size(message = "适用人群内容过少",min = 10)
    private String users; //适用人群

    private String tags; //课程标签

    @NotEmpty(message = "课程分类不能为空")
    private String mt; //大分类

    @NotEmpty(message = "课程分类不能为空")
    private String st; //小分类

    @NotEmpty(message = "课程等级不能为空")
    private String grade; //课程等级

    @NotEmpty(message = "课程教学模式不能为空")
    private String teachmode; //教学模式

    private String description; //课程介绍

    @NotEmpty
    private String pic; //课程图片

    @NotEmpty(message = "收费规则不能为空")
    private String charge; //收费规则

    private Float price; //价格

    private Float originalPrice; //原价

    private String qq; //qq

    private String wechat; //微信

    private String phone; //电话

    private Integer validDays; //有效期

}
