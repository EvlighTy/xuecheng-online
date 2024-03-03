package com.xuecheng.ucenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feign.CheckCodeServiceClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDTO;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service("password_auth")
public class PasswordAuthServiceImpl implements AuthService {

    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private CheckCodeServiceClient checkCodeServiceClient;

    @Override
    public XcUserExt execute(AuthParamsDTO authParamsDTO) {
        //校验验证码
        String checkcodekey = authParamsDTO.getCheckcodekey();
        String checkcode = authParamsDTO.getCheckcode();
        if(StringUtils.isEmpty(checkcodekey)||StringUtils.isEmpty(checkcode)) throw new RuntimeException("请输入验证码");
        Boolean result = checkCodeServiceClient.verify(checkcodekey, checkcode);
        if(result==null||!result) throw new RuntimeException("请输入正确的验证码");
        //校验用户名密码
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<XcUser>()
                .eq(XcUser::getUsername, authParamsDTO.getUsername());
        XcUser xcUser = xcUserMapper.selectOne(queryWrapper);
        if(xcUser==null) throw new RuntimeException("用户不存在");
        boolean matches = passwordEncoder.matches(authParamsDTO.getPassword(), xcUser.getPassword());
        if(!matches) throw new RuntimeException("用户名或密码错误");
        return BeanUtil.copyProperties(xcUser,XcUserExt.class);
    }

}