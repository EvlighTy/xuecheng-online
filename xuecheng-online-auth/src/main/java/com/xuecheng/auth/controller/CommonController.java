package com.xuecheng.auth.controller;

import com.xuecheng.ucenter.model.dto.FindPasswordDTO;
import com.xuecheng.ucenter.model.dto.RegisterDTO;
import com.xuecheng.ucenter.service.CommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping
public class CommonController {

    @Autowired
    private CommonService commonService;

    @PostMapping("/register")
    public void register(@RequestBody RegisterDTO registerDTO){
        log.info("用户注册");
        commonService.register(registerDTO);
    }

    @PostMapping("/findpassword")
    public void findPassword(@RequestBody FindPasswordDTO findPasswordDTO){
        log.info("用户找回密码");
        commonService.findPassword(findPasswordDTO);
    }

}
