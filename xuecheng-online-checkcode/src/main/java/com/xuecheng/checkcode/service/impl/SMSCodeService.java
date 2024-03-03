package com.xuecheng.checkcode.service.impl;

import com.xuecheng.checkcode.model.CheckCodeParamsDTO;
import com.xuecheng.checkcode.model.CheckCodeResultVO;
import com.xuecheng.checkcode.service.AbstractCheckCodeService;
import com.xuecheng.checkcode.service.CheckCodeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service("SMSCodeCheckService")
public class SMSCodeService extends AbstractCheckCodeService implements CheckCodeService {

    @Override
    public void setCheckCodeGenerator(CheckCodeGenerator checkCodeGenerator) {
    }

    @Resource(name="UUIDKeyGenerator")
    @Override
    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    @Resource(name="RedisCheckCodeStore")
    @Override
    public void setCheckCodeStore(CheckCodeStore checkCodeStore) {
        this.checkCodeStore = checkCodeStore;
    }

    @Override
    public CheckCodeResultVO generateVO(CheckCodeParamsDTO checkCodeParamsDTO) {
        GenerateResult generateResult = generateCode(checkCodeParamsDTO, null, "smsCode", 300);
        CheckCodeResultVO checkCodeResultVO = new CheckCodeResultVO();
        checkCodeResultVO.setKey(generateResult.getKey());
        return checkCodeResultVO;
    }


}
