package com.zaur.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserClient {
}
