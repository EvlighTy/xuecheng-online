package com.xuecheng.content.feign.fallbackFactory;

import com.xuecheng.content.feign.SearchServiceClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SearchServiceFallback implements FallbackFactory<SearchServiceClient> {
    @Override
    public SearchServiceClient create(Throwable throwable) {
        return courseIndex -> {
            log.info("添加课程索引异常,触发降级:[{}],异常信息:[{}]",courseIndex,throwable.toString(),throwable);
            return null;
        };
    }
}
