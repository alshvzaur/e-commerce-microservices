package com.zaur.order_service.client;

import com.zaur.order_service.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "product-service", url = "http://localhost:8082")
public interface ProductClient {

    @GetMapping("/product/{id}")
    ProductResponse getProductById(@PathVariable UUID id);

    @PatchMapping("/product/{id}/reduce-stock")
    ProductResponse reduceStock(@PathVariable UUID id, @RequestParam int quantity);
}
