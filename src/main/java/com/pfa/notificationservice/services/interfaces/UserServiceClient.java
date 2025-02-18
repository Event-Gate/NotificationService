package com.pfa.notificationservice.services.interfaces;

import com.pfa.notificationservice.dtos.DataUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/api/public/users/{sellerId}/followers")
    Set<String> getFollowers(@PathVariable String sellerId);

    @GetMapping("/api/public/users/{userId}")
    DataUserResponse getUserById(@PathVariable String userId);
}
