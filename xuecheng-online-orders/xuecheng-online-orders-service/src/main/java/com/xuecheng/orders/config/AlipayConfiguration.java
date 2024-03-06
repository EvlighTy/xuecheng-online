package com.xuecheng.orders.config;

import com.alipay.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description 支付宝配置参数
 * @author Mr.M
 * @date 2022/10/20 22:45
 * @version 1.0
 */

@Configuration
public class AlipayConfiguration {
     
     @Value("${alipay.appid}")
     public String APPID; //appid

     @Value("${alipay.public_key}")
     public String PUBLIC_KEY; //公钥

     @Value("${alipay.private_key}")
	 public String PRIVATE_KEY; //私钥
     
     public String gatewayURL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do"; //请求网关地址

     @Bean
     public AlipayClient AlipayClient()  throws AlipayApiException {
          System.err.println("初始化支付宝客户端");
          AlipayConfig alipayConfig = new AlipayConfig();
          alipayConfig.setAppId(APPID); //appid
          alipayConfig.setAlipayPublicKey(PUBLIC_KEY); //公钥
          alipayConfig.setPrivateKey(PRIVATE_KEY); //私钥
          alipayConfig.setServerUrl(gatewayURL); //网关地址
          alipayConfig.setFormat(AlipayConstants.FORMAT_JSON); //请求格式
          alipayConfig.setCharset(AlipayConstants.CHARSET_UTF8); //字符编码
          alipayConfig.setSignType(AlipayConstants.SIGN_TYPE_RSA2); //签名类型
          return new DefaultAlipayClient(alipayConfig);
     }
}
