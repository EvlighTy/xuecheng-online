package com.xuecheng.ucenter.feign;

import com.xuecheng.ucenter.fallbackFacory.CheckCodeFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping("/checkcode")
@FeignClient(value = "checkcode",fallbackFactory = CheckCodeFallbackFactory.class)
public interface CheckCodeServiceClient {

    @PostMapping(value = "/verify")
    Boolean verify(@RequestParam("key") String key, @RequestParam("code") String code);

}
