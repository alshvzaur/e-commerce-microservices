package com.zaur.product_service.service;

import com.zaur.product_service.dto.ProductRequest;
import com.zaur.product_service.dto.ProductResponse;
import com.zaur.product_service.exception.InsufficientStockException;
import com.zaur.product_service.exception.ProductNotFoundException;
import com.zaur.product_service.mapper.ProductMapper;
import com.zaur.product_service.model.Product;
import com.zaur.product_service.repo.ProductRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal=true)
public class ProductService {

    ProductMapper productMapper;
    ProductRepository productRepository;

    public Product saveEntity(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(UUID id) {
        return productRepository.findById(id);
    }

    public ProductResponse save(ProductRequest productRequest) {
        Product product = productMapper.toEntity(productRequest);
        Product newProduct = productRepository.save(product);
        return productMapper.toResponse(newProduct);
    }

    public List<ProductResponse> getAll(){

        List<Product> allProducts = productRepository.findAll();
        List<ProductResponse> productResponseList = new ArrayList<>();

        for (Product product : allProducts) {
            ProductResponse productResponse = productMapper.toResponse(product);
            productResponseList.add(productResponse);
        }

        return productResponseList;
    }

    public ProductResponse findById(UUID id) {
        Product product = productRepository.findById(id).orElseThrow(()-> new ProductNotFoundException("Product not found:" + id));
        return productMapper.toResponse(product);
    }

    public boolean deleteById(UUID id) {
        if(productRepository.findById(id).isPresent()) {
            productRepository.deleteById(id);
            return true;
        }else {
            return false;
        }
    }

    public ProductResponse reduceStock(UUID id, int quantity) {
        Product product = getProductById(id).orElseThrow(()-> new ProductNotFoundException("Product not found:" + id));

        if (quantity > product.getQuantity()){
            throw new InsufficientStockException("Out of stock");
        }else{
            product.setQuantity(product.getQuantity() - quantity);
        }

        productRepository.save(product);
        return productMapper.toResponse(product);
    }

    public List<ProductResponse> getProductsById(List<UUID> ids) {
        List<ProductResponse> productResponseList = new ArrayList<>();
        List<Product> products = productRepository.getProductsById(ids);
        for (Product product : products) {
            ProductResponse productResponse = productMapper.toResponse(product);
            productResponseList.add(productResponse);
        }
        return productResponseList;
    }
}
