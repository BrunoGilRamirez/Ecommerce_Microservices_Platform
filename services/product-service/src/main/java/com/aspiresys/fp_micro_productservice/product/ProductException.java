package com.aspiresys.fp_micro_productservice.product;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProductException extends RuntimeException {
    public ProductException(String message) {
        super(message);
    }
    public ProductException duplicateProduct(String productName) {
        return new ProductException("Duplicated product: " + productName);
    }

    public ProductException invalidProduct(String reason) {
        return new ProductException("Invalid product: " + reason);
    }
}
