package com.xuecheng.base.utils;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Slf4j
public class SecurityUtil {

    public static XcUser getUser() {
        try {
            Object principalObj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principalObj instanceof String) {
                String principal = principalObj.toString(); //取出用户身份信息
                return JSON.parseObject(principal, XcUser.class); //将json转成对象
            }
        } catch (Exception e) {
            log.error("获取当前登录用户身份出错:{}", e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    @Data
    public static class XcUser implements Serializable {
        private static final long serialVersionUID = 1L;
        private String id;
        private String username;
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

}
