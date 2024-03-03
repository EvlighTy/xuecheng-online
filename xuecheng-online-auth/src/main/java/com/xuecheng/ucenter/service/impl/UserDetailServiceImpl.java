package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDTO;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private XcUserMapper xcUserMapper;

    //自定义身份认证方式
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        AuthParamsDTO authParamsDTO;
        try {
            authParamsDTO = JSON.parseObject(s, AuthParamsDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("请求认证参数不符合要求");
        }
        //根据认证方式取出对应执行认证的service_bean
        AuthService authService = applicationContext.getBean(authParamsDTO.getAuthType() + "_auth", AuthService.class);
        XcUserExt xcUserExt = authService.execute(authParamsDTO);
        if (xcUserExt==null) return null;
        //查询用户权限
        List<String> userAuthority = xcUserMapper.getUserAuthority(xcUserExt.getId());
        String[] authorityStringArray = new String[0];
        if(userAuthority!=null && !userAuthority.isEmpty()){
            authorityStringArray = userAuthority.toArray(authorityStringArray);
        }
        //拓展用户身份信息(将所有用户信息转为JSON字符串)
        String userInfo = JSON.toJSONString(xcUserExt);
        return User.withUsername(userInfo)
                .password(xcUserExt.getPassword())
                .authorities(authorityStringArray)
                .build();
    }

}