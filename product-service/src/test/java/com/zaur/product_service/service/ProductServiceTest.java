package com.zaur.product_service.service;

import com.zaur.product_service.dto.ProductResponse;
import com.zaur.product_service.exception.InsufficientStockException;
import com.zaur.product_service.exception.ProductNotFoundException;
import com.zaur.product_service.mapper.ProductMapper;
import com.zaur.product_service.model.Product;
import com.zaur.product_service.repo.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    ProductRepository productRepository; //фейковый репозиторий

    @Mock
    ProductMapper productMapper; // фейковый маппер

    @InjectMocks
    ProductService productService; // реальный объект, но в конструктор вставляются фейковые моки

    @Test
    void shouldReduceStockWhenEnoughQuantity() {
        //Arrange
        UUID id = UUID.randomUUID();

        Product product = Product.builder()
                .id(id)
                .name("Test Product")
                .description("Test Test Test")
                .price(new BigDecimal("200.00"))
                .quantity(10)
                .build();

        ProductResponse expectedResponse = ProductResponse.builder()
                .id(id)
                .name("Test Product")
                .price(new BigDecimal("200.00"))
                .quantity(7)  // уже после списания, если хочешь проверить именно это
                .build();

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(expectedResponse);

        //Act
        ProductResponse result = productService.reduceStock(id, 3);


        //Assert
        assertEquals(expectedResponse, result);
        assertEquals(7, product.getQuantity());

    }

    @Test
    void shouldNotReduceStockWhenNotEnoughQuantity() {
        //Arrange
        UUID id = UUID.randomUUID();

        Product product = Product.builder()
                .id(id)
                .name("Test Product")
                .description("Test Test Test")
                .price(new BigDecimal("200.00"))
                .quantity(5)
                .build();

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        //Assert
        assertThrows(InsufficientStockException.class, () -> productService.reduceStock(id, 10));

    }

    @Test
    void shouldThrowExceptionWhenProductNotFound(){
        UUID id = UUID.randomUUID();

        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.reduceStock(id, 10));
    }
}
