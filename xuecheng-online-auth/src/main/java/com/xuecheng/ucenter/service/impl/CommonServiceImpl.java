package com.xuecheng.ucenter.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuecheng.ucenter.feign.CheckCodeServiceClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.mapper.XcUserRoleMapper;
import com.xuecheng.ucenter.model.dto.FindPasswordDTO;
import com.xuecheng.ucenter.model.dto.RegisterDTO;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.model.po.XcUserRole;
import com.xuecheng.ucenter.service.CommonService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private CheckCodeServiceClient checkCodeServiceClient;

    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    private XcUserRoleMapper xcUserRoleMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public void register(RegisterDTO registerDTO) {
        //校验验证码
        String checkcodekey = registerDTO.getCheckcodekey();
        String checkcode = registerDTO.getCheckcode();
        if(StringUtils.isEmpty(checkcodekey)||StringUtils.isEmpty(checkcode)) throw new RuntimeException("请输入验证码");
        Boolean result = checkCodeServiceClient.verify(checkcodekey, checkcode);
        if(result==null||!result) throw new RuntimeException("请输入正确的验证码");
        //注册用户
        /*业务逻辑校验(用户名是否已存在)*/
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<XcUser>()
                .eq(XcUser::getUsername, registerDTO.getUsername());
        boolean exists = xcUserMapper.exists(queryWrapper);
        if(exists) throw new RuntimeException("用户名已存在");
        /*保存用户信息*/
        XcUser xcUser = BeanUtil.copyProperties(registerDTO, XcUser.class);
        xcUser.setId(UUID.randomUUID().toString());
        xcUser.setName(registerDTO.getNickname());
        xcUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
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

    @Override
    public void findPassword(FindPasswordDTO findPasswordDTO) {
        String cellphone = findPasswordDTO.getCellphone();
        String email = findPasswordDTO.getEmail();
        String password = findPasswordDTO.getPassword();
        String confirmpwd = findPasswordDTO.getConfirmpwd();
        /*业务校验(手机号和邮箱不能同时为空)*/
        if(StringUtils.isEmpty(cellphone)&&StringUtils.isEmpty(email)) throw new RuntimeException("请输入手机号码或邮箱");
        /*业务校验两次输入的密码必须相同)*/
        if(!password.equals(confirmpwd)) throw new RuntimeException("两次输入的密码不一致");
        /*业务逻辑校验(用户存在)*/
        LambdaQueryWrapper<XcUser> queryWrapper = new LambdaQueryWrapper<XcUser>()
                .eq(!StringUtils.isEmpty(cellphone), XcUser::getCellphone, cellphone)
                .eq(!StringUtils.isEmpty(email), XcUser::getEmail, email);
        boolean exists = xcUserMapper.exists(queryWrapper);
        if(!exists) throw new RuntimeException("用户不存在");
        //更新用户密码
        LambdaUpdateWrapper<XcUser> updateWrapper = new LambdaUpdateWrapper<XcUser>()
                .eq(!StringUtils.isEmpty(cellphone), XcUser::getCellphone, cellphone)
                .eq(!StringUtils.isEmpty(email), XcUser::getEmail, email)
                .set(XcUser::getPassword, passwordEncoder.encode(password));
        int update = xcUserMapper.update(null, updateWrapper);
        if(update!=1) throw new RuntimeException("更改密码失败");
    }

}
