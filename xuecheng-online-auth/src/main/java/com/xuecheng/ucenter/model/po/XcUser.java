package com.xuecheng.ucenter.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 
 * </p>
 *
 * @author itcast
 */
@Data
@TableName("xc_user")
public class XcUser implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String username;
    @JsonIgnore
    private String password;
    private String salt;
    private String name;
    private String nickname;
    private String wxUnionid;
    private String companyId;
    private String userpic; //头像
    private String utype;
    private LocalDateTime birthday;
    private String sex;
    private String email;
    private String cellphone;
    private String qq;
    private String status; //用户状态
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
