package com.xuecheng.content.model.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class BindTeachPlanMediaDTO {
    private String mediaId;
    @NotEmpty(message = "文件名称不能为空")
    private String fileName;
    @NotNull(message = "课程计划id不能为空")
    private Long teachplanId;
}
