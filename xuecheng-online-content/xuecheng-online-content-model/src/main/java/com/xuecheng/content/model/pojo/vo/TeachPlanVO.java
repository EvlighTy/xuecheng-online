package com.xuecheng.content.model.pojo.vo;

import com.xuecheng.content.model.pojo.entity.Teachplan;
import com.xuecheng.content.model.pojo.entity.TeachplanMedia;
import lombok.Data;

import java.util.List;

@Data
public class TeachPlanVO extends Teachplan {
    private TeachplanMedia teachplanMedia;
    private List<TeachPlanVO> teachPlanTreeNodes;
}
