package com.xuecheng.checkcode.controller;

import com.xuecheng.checkcode.model.CheckCodeParamsDTO;
import com.xuecheng.checkcode.model.CheckCodeResultVO;
import com.xuecheng.checkcode.service.CheckCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Mr.M
 * @version 1.0
 * @description 验证码服务接口
 * @date 2022/9/29 18:39
 */
@Slf4j
@RestController
public class CheckCodeController {

    @Resource(name = "PicCheckCodeService")
    private CheckCodeService picCheckCodeService;

    @Resource(name = "SMSCodeCheckService")
    private CheckCodeService smsCodeCheckService;

    @PostMapping( "/pic")
    public CheckCodeResultVO pic(CheckCodeParamsDTO checkCodeParamsDTO){
        log.info("生成验证码图片");
        checkCodeParamsDTO.setCheckCodeType("pic");
        return picCheckCodeService.generateVO(checkCodeParamsDTO);
    }

    @PostMapping("/phone")
    public CheckCodeResultVO phone(CheckCodeParamsDTO checkCodeParamsDTO){
        log.info("发送短信验证码");
        checkCodeParamsDTO.setCheckCodeType("sms");
        return smsCodeCheckService.generateVO(checkCodeParamsDTO);
    }

    @PostMapping("/email")
    public CheckCodeResultVO email(CheckCodeParamsDTO checkCodeParamsDTO){
        log.info("发送邮箱验证码");
        checkCodeParamsDTO.setCheckCodeType("email");
        return smsCodeCheckService.generateVO(checkCodeParamsDTO);
    }

    @PostMapping( "/verify")
    public Boolean verify(String key, String code){
        log.info("校验验证码");
        return picCheckCodeService.verify(key,code);
    }

}
