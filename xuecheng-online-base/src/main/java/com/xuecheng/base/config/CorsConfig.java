package com.xuecheng.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter(){
        System.err.println("跨域请求配置...");
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true); //允许跨域发送cookie
        corsConfiguration.addAllowedOrigin("*"); //允许跨域的域名
        corsConfiguration.addAllowedHeader("*"); //允许放行的原始请求头
        corsConfiguration.addAllowedMethod("*"); //允许跨域的方法
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsFilter(source);
    }

}
