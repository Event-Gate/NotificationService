package com.pfa.notificationservice.config.feign;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor bearerTokenRequestInterceptor() {
        return requestTemplate -> {
            if (requestTemplate.headers().containsKey("Authorization")) {
                String token = requestTemplate.headers().get("Authorization").iterator().next();
                if (!token.startsWith("Bearer ")) {
                    requestTemplate.header("Authorization", "Bearer " + token);
                }
            }
        };
    }
}
