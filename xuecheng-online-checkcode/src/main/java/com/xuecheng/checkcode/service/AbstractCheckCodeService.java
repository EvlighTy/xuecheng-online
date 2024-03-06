package com.xuecheng.checkcode.service;

import com.xuecheng.base.exmsg.AuthExMsg;
import com.xuecheng.base.exception.CustomException;
import com.xuecheng.checkcode.model.CheckCodeParamsDTO;
import com.xuecheng.checkcode.model.CheckCodeResultVO;
import com.xuecheng.checkcode.utils.AliyunSMSUtil;
import com.xuecheng.checkcode.utils.EmailCodeUtil;
import com.xuecheng.checkcode.utils.RegExpUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Mr.M
 * @version 1.0
 * @description 验证码接口
 * @date 2022/9/29 15:59
 */
@Slf4j
public abstract class AbstractCheckCodeService implements CheckCodeService {

    protected CheckCodeGenerator checkCodeGenerator;

    protected KeyGenerator keyGenerator;

    protected CheckCodeStore checkCodeStore;

    public abstract void  setCheckCodeGenerator(CheckCodeGenerator checkCodeGenerator);

    public abstract void  setKeyGenerator(KeyGenerator keyGenerator);

    public abstract void  setCheckCodeStore(CheckCodeStore CheckCodeStore);

    /**
     * @description 生成验证公用方法
     * @param checkCodeParamsDto 生成验证码参数
     * @param code_length 验证码长度
     * @param keyPrefix key的前缀
     * @param expire 过期时间
     * @return com.xuecheng.checkcode.service.AbstractCheckCodeService.GenerateResult 生成结果
     * @author Mr.M
     * @date 2022/9/30 6:07
    */

    @Autowired
    private AliyunSMSUtil aliyunSMSUtil;

    @Autowired
    private EmailCodeUtil emailCodeUtil;

    public GenerateResult generateCode(CheckCodeParamsDTO checkCodeParamsDTO, Integer codeLength, String keyPrefix, Integer expire){
        String checkCodeType = checkCodeParamsDTO.getCheckCodeType();
        //key
        String key;
        //value(验证码)
        String code = null;
        if(checkCodeType.equals("pic")){
            //图形验证码
            code = checkCodeGenerator.generate(codeLength);
            key = keyGenerator.generate(keyPrefix);
        }else if(checkCodeType.equals("sms")){
            //手机验证码
            String phoneNumber = checkCodeParamsDTO.getParam1();
            /*业务逻辑校验(手机号码格式正确)*/
            if(!RegExpUtil.checkPhone(phoneNumber)) throw new CustomException(AuthExMsg.ILLEGAL_PHONE);
            code = aliyunSMSUtil.sendSmsCode(phoneNumber);
            key = phoneNumber;
        }else {
            //邮箱验证码
            String email = checkCodeParamsDTO.getParam1();
            /*业务逻辑校验(邮箱格式正确)*/
            if(!RegExpUtil.checkEmail(email)) throw new CustomException(AuthExMsg.ILLEGAL_EMAIL);
            code = emailCodeUtil.sendEmailCode(email);
            key = email;
        }
        log.debug("生成的验证码为:[{}]",code);
        //存储 key value 到 Redis
        checkCodeStore.set(key,code,expire);
        //返回验证码生成结果
        GenerateResult generateResult = new GenerateResult();
        generateResult.setKey(key);
        generateResult.setCode(code);
        return generateResult;
    }

    @Data
    protected static class GenerateResult{
        String key;
        String code;
    }

    public abstract CheckCodeResultVO generateVO(CheckCodeParamsDTO checkCodeParamsDTO);

    public boolean verify(String key, String code){
        if (StringUtils.isBlank(key) || StringUtils.isBlank(code)){
            return false;
        }
        String code_l = checkCodeStore.get(key);
        if (code_l == null){
            return false;
        }
        boolean result = code_l.equalsIgnoreCase(code);
        if(result){
            //删除验证码
            checkCodeStore.remove(key);
        }
        return result;
    }

}
