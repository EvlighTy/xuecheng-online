package com.xuecheng.media.feign;

import com.xuecheng.media.feign.fallbackFactory.ContentServiceClientFallbackFactory;
import com.xuecheng.media.feign.model.TeachplanMedia;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "content-api",fallbackFactory = ContentServiceClientFallbackFactory.class)
public interface ContentFeignClient {
    @GetMapping("/media/{mediaId}")
    TeachplanMedia queryTeachPlanByMediaId(@PathVariable("mediaId") String mediaId);
}
