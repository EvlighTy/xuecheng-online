package com.xuecheng.ucenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDTO;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.AuthService;
import com.xuecheng.ucenter.service.WechatAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service("wx_auth")
public class WeChatAuthServiceImpl implements AuthService, WechatAuthService {

    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    private XcUserRoleMapper xcUserRoleMapper;

    @Autowired @Lazy
    private WechatAuthService wechatAuthService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${weixin.appid}")
    private String appid;

    @Value("${weixin.secret}")
    private String secret;

    //请求微信获取令牌地址
    private static final String applyAccessTokenURL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";

    //请求微信获取用户信息地址
    private static final String applyUserInfoURL = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";

    @Override
    public XcUserExt execute(AuthParamsDTO authParamsDTO) {
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<XcUser>()
                .eq(XcUser::getUsername, authParamsDTO.getUsername());
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if(xcUser==null) throw new RuntimeException("用户不存在");
        return BeanUtil.copyProperties(xcUser,XcUserExt.class);
    }

    @Override
    public XcUser weChatAuth(String code) {
        Map<String, String> accessToken = getAccessToken(code);
        if(accessToken==null||accessToken.isEmpty()) return null;
        Map<String, String> userInfo = getUserInfo(accessToken.get("access_token"), accessToken.get("openid"));
        if(userInfo==null||userInfo.isEmpty()) return null;
        return wechatAuthService.addXCUSer(userInfo);
    }

    //使用授权码获取令牌
    private Map<String,String> getAccessToken(String code) {
        String wxUrl = String.format(applyAccessTokenURL, appid, secret, code);
        log.info("调用微信接口申请access_token, url:{}", wxUrl);
        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);
        String result = exchange.getBody();
        log.info("调用微信接口申请access_token: 返回值:{}", result);
        return JSON.parseObject(result, Map.class);
    }

    //使用令牌获取用户信息
    private Map<String,String> getUserInfo(String access_token,String openid) {
        //请求微信地址
        String wxUrl = String.format(applyUserInfoURL, access_token,openid);
        log.info("调用微信接口申请access_token, url:{}", wxUrl);
        ResponseEntity<String> exchange = restTemplate.exchange(wxUrl, HttpMethod.POST, null, String.class);
        //防止乱码进行转码
        String result = new String(exchange.getBody().getBytes(StandardCharsets.ISO_8859_1),StandardCharsets.UTF_8);
        log.info("调用微信接口申请access_token: 返回值:{}", result);
        return JSON.parseObject(result, Map.class);
    }

    //添加用户
    @Transactional
    public XcUser addXCUSer(Map<String, String> userInfo){
        String unionid = userInfo.get("unionid");
        /*业务逻辑校验(用户是否已存在)*/
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<XcUser>()
                .eq(XcUser::getWxUnionid, unionid);
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if(xcUser!=null){
            //更新
            xcUser.setNickname(userInfo.get("nickname"));
            xcUser.setUserpic(userInfo.get("headimgurl"));
            xcUserMapper.updateById(xcUser);
        }else {
            //新增
            /*保存信息到用户表*/
            xcUser = new XcUser();
            xcUser.setId(UUID.randomUUID().toString());
            xcUser.setWxUnionid(unionid);
            xcUser.setName(userInfo.get("nickname"));
            xcUser.setNickname(userInfo.get("nickname"));
            xcUser.setUserpic(userInfo.get("headimgurl"));
            xcUser.setUsername(unionid);
            xcUser.setPassword(unionid);
            xcUser.setUtype("101001"); //学生类型
            xcUser.setStatus("1"); //用户状态
            xcUser.setCreateTime(LocalDateTime.now());
            xcUserMapper.insert(xcUser);
            /*保存信息到用户角色表*/
            XcUserRole xcUserRole = new XcUserRole();
            xcUserRole.setId(UUID.randomUUID().toString());
            xcUserRole.setUserId(xcUser.getId());
            xcUserRole.setRoleId("17"); //学生角色
            xcUserRoleMapper.insert(xcUserRole);
        }
        //返回用户信息
        return xcUser;
    }

}