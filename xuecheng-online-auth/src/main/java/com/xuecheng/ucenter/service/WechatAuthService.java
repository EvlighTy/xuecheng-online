package com.xuecheng.ucenter.service;

import com.xuecheng.ucenter.model.po.XcUser;

import java.util.Map;

public interface WechatAuthService {

    XcUser weChatAuth(String code);

    XcUser addXCUSer(Map<String, String> userInfo);

}