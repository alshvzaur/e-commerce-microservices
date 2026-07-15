package com.zaur.product_service.controller;


import com.zaur.product_service.dto.ProductRequest;
import com.zaur.product_service.dto.ProductResponse;
import com.zaur.product_service.service.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    ProductService productService;

    @GetMapping("/all")
    public List<ProductResponse> getAll(){
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public ProductResponse getOne(@PathVariable UUID id){
        return  productService.findById(id);
    }

    @PostMapping()
    public ProductResponse create(@RequestBody ProductRequest productRequest){
        return productService.save(productRequest);
    }

    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable UUID id){
        return productService.deleteById(id);
    }


}
