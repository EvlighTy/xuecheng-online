package com.xuecheng.learning.feign.fallbackFactory;

import com.xuecheng.learning.feign.MediaServiceClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/10/27 9:05
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {
    @Override
    public MediaServiceClient create(Throwable throwable) {
        return mediaId -> {
            log.error("远程调用媒资管理服务熔断异常：{}", throwable.getMessage());
            return null;
        };
    }
}
