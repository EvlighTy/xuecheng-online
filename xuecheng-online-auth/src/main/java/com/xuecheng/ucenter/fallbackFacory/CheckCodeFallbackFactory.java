package com.xuecheng.ucenter.fallbackFacory;

import com.xuecheng.ucenter.feign.CheckCodeServiceClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class CheckCodeFallbackFactory implements FallbackFactory<CheckCodeServiceClient> {
    @Override
    public CheckCodeServiceClient create(Throwable throwable) {
        return new CheckCodeServiceClient() {
            @Override
            public Boolean verify(String key, String code) {
                log.info("校验验证码触发熔断降级:{}",throwable.toString());
                return null;
            }
        };
    }
}
