package com.xuecheng.content.feign;

import com.xuecheng.content.feign.fallbackFactory.SearchServiceFallback;
import com.xuecheng.content.model.pojo.feign.CourseIndex;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "search",fallbackFactory = SearchServiceFallback.class)
public interface SearchServiceClient {

    @PostMapping("/search/index/course")
    Boolean add(@RequestBody CourseIndex courseIndex);
}
