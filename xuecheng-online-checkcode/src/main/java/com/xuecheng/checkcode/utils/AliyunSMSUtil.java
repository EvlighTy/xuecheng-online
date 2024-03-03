package com.xuecheng.checkcode.utils;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AliyunSMSUtil {

    private static final String REGION_ID = "cn-hangzhou";

    private static final String PRODUCT = "Dysmsapi";

    private static final String ENDPOINT = "dysmsapi.aliyuncs.com";

    @Value("${aliyun.access_key_id}")
    private String accessKeyId; //访问密钥id

    @Value("${aliyun.access_key_secret}")
    private String accessKeySecret; //访问密钥密码

    @Value("${aliyun.sign_name}")
    private String signName; //签名

    @Value("${aliyun.template_code}")
    private String templateCode; //模板

    public String sendSmsCode(String phoneNumber){
        try {
            IClientProfile profile = DefaultProfile.getProfile(REGION_ID, accessKeyId, accessKeySecret);
            DefaultProfile.addEndpoint(REGION_ID, REGION_ID, PRODUCT, ENDPOINT);
            IAcsClient acsClient = new DefaultAcsClient(profile);
            SendSmsRequest request = new SendSmsRequest();
            request.setMethod(MethodType.POST);
            request.setPhoneNumbers(phoneNumber);
            request.setSignName(signName);
            request.setTemplateCode(templateCode);
            String smsCode = RandomCodeUtil.getSixFigureCode();
            request.setTemplateParam("{\"code\":\""+ smsCode +"\"}");
            SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
            if (sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) log.info("发送成功,code:" + sendSmsResponse.getMessage());
            else log.info("发送失败,code:" + sendSmsResponse.getMessage());
            return smsCode;
        } catch (ClientException e) {
            throw new RuntimeException("验证码发送失败");
        }
    }

}

