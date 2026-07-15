package com.zaur.product_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    String name;

    String description;

    @Min(value = 0, message = "Price can't be negative")
    @NotNull(message = "Price is required")
    BigDecimal price;

    @Min(value = 1, message = "Quantity must be  more than 0")
    int quantity;
}
