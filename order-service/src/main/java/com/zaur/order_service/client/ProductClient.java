package com.zaur.order_service.client;

import com.zaur.order_service.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "product-service", url = "${product.service.url}")
public interface ProductClient {

    @GetMapping("/product/{id}")
    ProductResponse getProductById(@PathVariable UUID id);

    @PatchMapping("/product/{id}/reduce-stock")
    ProductResponse reduceStock(@PathVariable UUID id, @RequestParam int quantity);

    @PostMapping("/product/products/by-ids")
    List<ProductResponse> getProductsByIds(@RequestBody List<UUID> ids);
}
