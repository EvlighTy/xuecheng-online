package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.WechatAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class WeChatController {

    @Autowired
    private WechatAuthService wechatAuthService;

    @GetMapping("/wxLogin")
    public String wxLogin(String code, String state) {
        log.debug("微信扫码回调,code:{},state:{}",code,state);
        //请求微信申请令牌，拿到令牌查询用户信息，将用户信息写入本项目数据库
        XcUser xcUser = wechatAuthService.weChatAuth(code);
        if(xcUser==null) return "redirect:http://www.51xuecheng.cn/error.html";
        else return "redirect:http://www.51xuecheng.cn/sign.html?username="+xcUser.getUsername()+"&authType=wx";
    }

}