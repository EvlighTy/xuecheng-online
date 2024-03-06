package com.xuecheng.content.feign.fallbackFactory;

import com.xuecheng.content.feign.MediaServiceClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MediaServiceFallback implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return (upload, objectName) -> {
            log.info("课程页面静态化异常,触发降级:[{}],异常信息:[{}]",objectName,throwable.toString(),throwable);
            return null;
        };
    }
}